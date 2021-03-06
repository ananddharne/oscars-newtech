package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.PortLayer;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.servicetopo.ServiceLayerTopology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 6/22/16.
 */
@Slf4j
@Component
public class NonPalindromicalPCE {
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ServiceLayerTopology serviceLayerTopology;

    /**
     * Depends on DijkstraPCE and ServiceLayerTopology to construct and build the Service-Layer EROs, and then map them to Physical-Layer EROs
     *
     * @param requestPipe  Requested pipe with required reservation parameters
     * @return A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeNonPalindromicERO(RequestedVlanPipeE requestPipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        Topology ethTopo = topoService.layer(Layer.ETHERNET);
        Topology intTopo = topoService.layer(Layer.INTERNAL);
        Topology mplsTopo = topoService.layer(Layer.MPLS);

        // Filter MPLS-ports and MPLS-devices out of ethTopo
        Set<TopoVertex> portsOnly = ethTopo.getVertices().stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        for (TopoEdge intEdge : intTopo.getEdges())
        {
            TopoVertex vertA = intEdge.getA();
            TopoVertex vertZ = intEdge.getZ();

            if (portsOnly.isEmpty())
            {
                break;
            }

            if(portsOnly.contains(vertA) && !vertA.getPortLayer().equals(PortLayer.MPLS))
                portsOnly.remove(vertA);

            if(portsOnly.contains(vertZ) && !vertZ.getPortLayer().equals(PortLayer.MPLS))
                portsOnly.remove(vertZ);
        }

        ethTopo.getVertices().removeIf(v -> v.getVertexType().equals(VertexType.ROUTER) && !topoService.determineIfRouterHasEthernetPorts(v.getUrn()));
        ethTopo.getVertices().removeAll(portsOnly);

        // Filter Devices and Ports out of intTopo
        intTopo.getVertices().removeAll(intTopo.getVertices());

        /* Initialize Service-Layer Topology */
        serviceLayerTopology.setTopology(ethTopo);
        serviceLayerTopology.setTopology(intTopo);
        serviceLayerTopology.setTopology(mplsTopo);

        serviceLayerTopology.createMultilayerTopology();
        serviceLayerTopology.resetLogicalLinks();

        UrnE srcDeviceURN = topoService.getUrn(requestPipe.getAJunction().getDeviceUrn());
        UrnE dstDeviceURN = topoService.getUrn(requestPipe.getZJunction().getDeviceUrn());
        VertexType srcType = topoService.getVertexTypeFromDeviceType(srcDeviceURN.getDeviceType());
        VertexType dstType = topoService.getVertexTypeFromDeviceType(dstDeviceURN.getDeviceType());
        TopoVertex srcDevice = new TopoVertex(srcDeviceURN.getUrn(), srcType);
        TopoVertex dstDevice = new TopoVertex(dstDeviceURN.getUrn(), dstType);
        Set<RequestedVlanFixtureE> srcFixtures = requestPipe.getAJunction().getFixtures();
        Set<RequestedVlanFixtureE> dstFixtures = requestPipe.getZJunction().getFixtures();
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;

        if(srcFixtures.size() > 0)
        {
            RequestedVlanFixtureE srcFix = srcFixtures.iterator().next();
            String srcFixURN = srcFix.getPortUrn();
            PortLayer srcFixLayer = topoService.lookupPortLayer(srcFixURN);

            srcPort = new TopoVertex(srcFixURN, VertexType.PORT, srcFixLayer);
        }
        else
        {
            srcPort = new TopoVertex("fix" + srcDevice.getUrn(), VertexType.PORT, PortLayer.ETHERNET);
        }

        if(dstFixtures.size() > 0)
        {
            RequestedVlanFixtureE dstFix = dstFixtures.iterator().next();
            String dstFixURN = dstFix.getPortUrn();
            PortLayer dstFixLayer = topoService.lookupPortLayer(dstFixURN);

            dstPort = new TopoVertex(dstFixURN, VertexType.PORT, dstFixLayer);
        }
        else
        {
            dstPort = new TopoVertex("fix" + dstDevice.getUrn(), VertexType.PORT, PortLayer.ETHERNET);
        }

        // Handle MPLS-layer source/destination devices
        serviceLayerTopology.buildLogicalLayerSrcNodes(srcDevice, srcPort);
        serviceLayerTopology.buildLogicalLayerDstNodes(dstDevice, dstPort);

        // Add the virtual port to Service Layer Topology's MPLS topology
        // Only do this if the source/dest is a ROUTER and if no fixtures are defined
        if(srcDevice.getVertexType().equals(VertexType.ROUTER) && srcFixtures.size() == 0)
            addPortToServiceMplsTopology(serviceLayerTopology, srcPort, srcDevice);

        if(dstDevice.getVertexType().equals(VertexType.ROUTER) && dstFixtures.size() == 0)
            addPortToServiceMplsTopology(serviceLayerTopology, dstPort, dstDevice);


        // Performs shortest path routing on MPLS-layer to properly assign weights to each logical link on Service-Layer
        serviceLayerTopology.calculateLogicalLinkWeights(requestPipe, urnRepo.findAll(), bwAvailMap, rsvVlanList);

        Topology slTopo = serviceLayerTopology.getSLTopology();
        Topology prunedSlTopo = pruningService.pruneWithPipe(slTopo, requestPipe, bwAvailMap, rsvVlanList);

        TopoVertex serviceLayerSrcNode;
        TopoVertex serviceLayerDstNode;

        if (srcDevice.getVertexType().equals(VertexType.SWITCH) || topoService.determineIfRouterHasEthernetPorts(srcDevice.getUrn()))
            serviceLayerSrcNode = srcPort;
        else
            serviceLayerSrcNode = serviceLayerTopology.getVirtualNode(srcPort);


        if (dstDevice.getVertexType().equals(VertexType.SWITCH) || topoService.determineIfRouterHasEthernetPorts(dstDevice.getUrn()))
            serviceLayerDstNode = dstPort;
        else
            serviceLayerDstNode = serviceLayerTopology.getVirtualNode(dstPort);

        assert (serviceLayerSrcNode != null);
        assert (serviceLayerDstNode != null);

        // Shortest path routing on Service-Layer
        List<TopoEdge> azServiceLayerERO = dijkstraPCE.computeShortestPathEdges(prunedSlTopo, serviceLayerSrcNode, serviceLayerDstNode);

        if (azServiceLayerERO.isEmpty())
            throw new PCEException("Empty path NonPalindromic PCE");

        // Get palindromic Service-Layer path in reverse-direction
        List<TopoEdge> zaServiceLayerERO = new LinkedList<>();

        // 1. Reverse the links
        for (TopoEdge azEdge : azServiceLayerERO) {
            Optional<TopoEdge> reverseEdge = prunedSlTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            reverseEdge.ifPresent(zaServiceLayerERO::add);
        }

        // 2. Reverse the order
        Collections.reverse(zaServiceLayerERO);

        Map<String, List<TopoEdge>> theMap = new HashMap<>();

        if (!(azServiceLayerERO.size() == zaServiceLayerERO.size()))
            return theMap;

        List<TopoEdge> azERO;
        List<TopoEdge> zaERO;

        log.info("SERVICE LAYER ROUTES: ");
        String azPath = "AZ Path: ";
        for(TopoEdge azEdge : azServiceLayerERO)
            azPath += azEdge.getA().getUrn() + " --- ";
        azPath += azServiceLayerERO.get(azServiceLayerERO.size()-1).getZ().getUrn();
        log.info(azPath);

        String zaPath = "ZA Path: ";
        for(TopoEdge zaEdge : zaServiceLayerERO)
            zaPath += zaEdge.getA().getUrn() + " --- ";
        zaPath += zaServiceLayerERO.get(zaServiceLayerERO.size()-1).getZ().getUrn();
        log.info(zaPath);

        // Obtain physical ERO from Service-Layer EROs
        azERO = serviceLayerTopology.getActualEROAZ(azServiceLayerERO);
        zaERO = serviceLayerTopology.getActualEROZA(zaServiceLayerERO);

        log.info("PHYSICAL ROUTES: ");
        azPath = "AZ Path: ";
        for(TopoEdge azEdge : azERO)
            azPath += azEdge.getA().getUrn() + " --- ";
        azPath += azERO.get(azERO.size()-1).getZ().getUrn();
        log.info(azPath);

        zaPath = "ZA Path: ";
        for(TopoEdge zaEdge : zaERO)
            zaPath += zaEdge.getA().getUrn() + " --- ";
        zaPath += zaERO.get(zaERO.size()-1).getZ().getUrn();
        log.info(zaPath);

        // Remove starting and ending ports
        if(azERO.get(0).getA().getVertexType().equals(VertexType.PORT))
            azERO.remove(0);

        if(azERO.get(azERO.size()-1).getZ().getVertexType().equals(VertexType.PORT))
            azERO.remove(azERO.size()-1);

        if(zaERO.get(0).getA().getVertexType().equals(VertexType.PORT))
            zaERO.remove(0);

        if(zaERO.get(zaERO.size()-1).getZ().getVertexType().equals(VertexType.PORT))
            zaERO.remove(zaERO.size()-1);

        theMap.put("az", azERO);
        theMap.put("za", zaERO);

        // TODO: Current implementation only tries the shortest forward-direction route. May result in false-negatives if reverse-direction is unavailable. If unsuccessful, prune out bad ports and try again.

        return theMap;
    }

    private void addPortToServiceMplsTopology(ServiceLayerTopology serviceLayerTopology, TopoVertex port, TopoVertex device)
    {
        serviceLayerTopology.getMplsLayerPorts().add(port);
        TopoEdge portToDeviceEdge = new TopoEdge(port, device, 0L, Layer.MPLS);
        TopoEdge deviceToPortEdge = new TopoEdge(device, port, 0L, Layer.MPLS);
        serviceLayerTopology.getMplsLayerLinks().add(portToDeviceEdge);
        serviceLayerTopology.getMplsLayerLinks().add(deviceToPortEdge);
        serviceLayerTopology.getMplsTopology().getVertices().add(port);
        serviceLayerTopology.getMplsTopology().getEdges().add(portToDeviceEdge);
        serviceLayerTopology.getMplsTopology().getEdges().add(deviceToPortEdge);
    }
}

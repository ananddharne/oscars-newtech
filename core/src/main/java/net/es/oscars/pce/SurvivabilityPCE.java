package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.enums.PortLayer;
import net.es.oscars.resv.ent.*;
import net.es.oscars.servicetopo.SurvivableServiceLayerTopology;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 7/27/16.
 */
@Slf4j
@Component
public class SurvivabilityPCE
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private SurvivableServiceLayerTopology serviceLayerTopology;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private BhandariPCE bhandariPCE;

    /**
     * Depends on BhandariPCE to construct the survivable physical-layer EROs for a request after pruning the topology based on requested parameters
     *
     * @param requestPipe Requested pipe with required reservation parameters
     * @param bwAvailMap
     * @return A four- element Map containing both the primary and secondary link-disjoint forward-direction EROs and the primary and secondary link-disjoint reverse-direction EROs
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeSurvivableERO(RequestedVlanPipeE requestPipe,
                                                            Map<String, Map<String, Integer>> bwAvailMap,
                                                            List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        if(requestPipe.getEroSurvivability().equals(SurvivabilityType.SURVIVABILITY_TOTAL))
        {
            return computeSurvivableEroComplete(requestPipe, bwAvailMap, rsvVlanList);
        }
        else if(requestPipe.getEroSurvivability().equals(SurvivabilityType.SURVIVABILITY_PARTIAL))
        {
            return computeSurvivableEroPartial(requestPipe, bwAvailMap, rsvVlanList);
        }
        else
        {
            throw new PCEException("Unsupported SurvivabilityType");
        }
    }


    private Map<String, List<TopoEdge>> computeSurvivableEroComplete(RequestedVlanPipeE requestPipe,
                                                                     Map<String, Map<String, Integer>> bwAvailMap,
                                                                     List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        String srcDeviceURN = requestPipe.getAJunction().getDeviceUrn();
        String dstDeviceURN = requestPipe.getZJunction().getDeviceUrn();

        UrnE srcDeviceURN_e = urnRepo.findByUrn(requestPipe.getAJunction().getDeviceUrn()).orElseThrow(NoSuchElementException::new);
        UrnE dstDeviceURN_e = urnRepo.findByUrn(requestPipe.getZJunction().getDeviceUrn()).orElseThrow(NoSuchElementException::new);


        VertexType srcType = topoService.getVertexTypeFromDeviceType(srcDeviceURN_e.getDeviceType());
        VertexType dstType = topoService.getVertexTypeFromDeviceType(dstDeviceURN_e.getDeviceType());

        TopoVertex srcDevice = new TopoVertex(srcDeviceURN, srcType);
        TopoVertex dstDevice = new TopoVertex(dstDeviceURN, dstType);

        Topology multiLayerTopo = topoService.getMultilayerTopology();

        // Identify src/dst ports for disjoint routing



        // Bandwidth and Vlan pruning
        Topology prunedTopo = pruningService.pruneWithPipeAZ(multiLayerTopo, requestPipe, bwAvailMap, rsvVlanList);

        // Disjoint shortest-path routing
        List<List<TopoEdge>> azPathSet = bhandariPCE.computeDisjointPaths(prunedTopo, srcDevice, dstDevice, requestPipe.getNumPaths());

        log.info(azPathSet.toString());

        if(azPathSet.isEmpty())
        {
            throw new PCEException("Empty path-set in Survivability PCE");
        }
        else if(azPathSet.size() != requestPipe.getNumPaths())
        {
            throw new PCEException(requestPipe.getNumPaths() + " disjoint paths could not be found in Survivability PCE");
        }


        // Get palindromic paths in reverse-direction //
        List<List<TopoEdge>> zaPathSet = new ArrayList<>();

        // 1. Reverse the links
        for(List<TopoEdge> azERO : azPathSet)
        {
            List<TopoEdge> zaERO = new ArrayList<>();

            for(TopoEdge azEdge : azERO)
            {
                Optional<TopoEdge> reverseEdge = prunedTopo.getEdges().stream()
                        .filter(r -> r.getA().equals(azEdge.getZ()))
                        .filter(r -> r.getZ().equals(azEdge.getA()))
                        .findFirst();

                reverseEdge.ifPresent(zaERO::add);
            }

            zaPathSet.add(zaERO);
        }

        // 2. Reverse the order
        for(List<TopoEdge> zaERO : zaPathSet)
        {
            Collections.reverse(zaERO);
        }

        log.info(zaPathSet.toString());

        assert(azPathSet.size() == requestPipe.getNumPaths());
        assert(azPathSet.size() == zaPathSet.size());


        for(int p = 0; p < azPathSet.size(); p++)
        {
            List<TopoEdge> azERO = azPathSet.get(p);
            List<TopoEdge> zaERO = zaPathSet.get(p);
            assert(azERO.size() == zaERO.size());
        }

        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        Integer numPathsSoFar = 0;
        for(List<TopoEdge> azPath : azPathSet){
            numPathsSoFar += 1;
            theMap.put("az" + numPathsSoFar, azPath);
        }
        numPathsSoFar = 0;
        for(List<TopoEdge> zaPath : zaPathSet){
            numPathsSoFar += 1;
            theMap.put("za" + numPathsSoFar, zaPath);
        }
        return theMap;
    }


    //TODO: Make this work with a set number of disjoint paths
    // Number of disjoint paths requested specified in the requestPipe
    private Map<String, List<TopoEdge>> computeSurvivableEroPartial(RequestedVlanPipeE requestPipe,
                                                                    Map<String, Map<String, Integer>> bwAvailMap,
                                                                    List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        Topology ethTopo = topoService.layer(Layer.ETHERNET);
        Topology intTopo = topoService.layer(Layer.INTERNAL);
        Topology mplsTopo = topoService.layer(Layer.MPLS);


        Topology physTopo = topoService.getMultilayerTopology();

        // Filter MPLS-ports and MPLS-devices out of ethTopo
        Set<TopoVertex> portsOnly = ethTopo.getVertices().stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        for(TopoEdge intEdge : intTopo.getEdges())
        {
            TopoVertex vertA = intEdge.getA();
            TopoVertex vertZ = intEdge.getZ();

            if(portsOnly.isEmpty())
            {
                break;
            }

            if(portsOnly.contains(vertA))
            {
                if(!vertZ.getVertexType().equals(VertexType.ROUTER))
                {
                    portsOnly.remove(vertA);
                }
            }
        }

        ethTopo.getVertices().removeIf(v -> v.getVertexType().equals(VertexType.ROUTER));
        ethTopo.getVertices().removeAll(portsOnly);

        // Filter Devices and Ports out of intTopo
        intTopo.getVertices().removeAll(intTopo.getVertices());

        // Initialize Service-Layer Topology
        serviceLayerTopology.setTopology(ethTopo);
        serviceLayerTopology.setTopology(intTopo);
        serviceLayerTopology.setTopology(mplsTopo);
        serviceLayerTopology.createMultilayerTopology();
        serviceLayerTopology.resetLogicalLinks();

        UrnE srcDeviceURN = urnRepo.findByUrn(requestPipe.getAJunction().getDeviceUrn()).orElseThrow(NoSuchElementException::new);
        UrnE dstDeviceURN = urnRepo.findByUrn(requestPipe.getZJunction().getDeviceUrn()).orElseThrow(NoSuchElementException::new);

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
        // Add the fake port to Service Layer Topology's MPLS topology
        // Only do this if the source/dest is a router and if no fixtures are defined
        if(srcDevice.getVertexType().equals(VertexType.ROUTER) && srcFixtures.size() == 0){
            addPortToServiceMplsTopology(serviceLayerTopology, srcPort, srcDevice);
        }
        if(dstDevice.getVertexType().equals(VertexType.ROUTER) && dstFixtures.size() == 0){
            addPortToServiceMplsTopology(serviceLayerTopology, dstPort, dstDevice);
        }

        // Performs shortest path routing on MPLS-layer to properly assign weights to each logical link on Service-Layer
        serviceLayerTopology.calculateLogicalLinkWeights(requestPipe, urnRepo.findAll(), bwAvailMap, rsvVlanList);

        Topology slTopo;

        slTopo = serviceLayerTopology.getSLTopology();

        Topology prunedSlTopo = pruningService.pruneWithPipe(slTopo, requestPipe, bwAvailMap, rsvVlanList);
        Topology prunedPhysicalTopo = pruningService.pruneWithPipe(physTopo, requestPipe, bwAvailMap, rsvVlanList);

        TopoVertex serviceLayerSrcNode;
        TopoVertex serviceLayerDstNode;

        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerSrcNode = srcPort;
        }
        else
        {
            serviceLayerSrcNode = serviceLayerTopology.getVirtualNode(srcDevice);
            assert(serviceLayerSrcNode != null);
        }

        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerDstNode = dstPort;
        }
        else
        {
            serviceLayerDstNode = serviceLayerTopology.getVirtualNode(dstDevice);
            assert(serviceLayerDstNode != null);
        }

        // Shortest path routing on Service-Layer
        List<TopoEdge> azServiceLayerERO = dijkstraPCE.computeShortestPathEdges(prunedSlTopo, serviceLayerSrcNode, serviceLayerDstNode);

        if (azServiceLayerERO.isEmpty())
        {
            throw new PCEException("Empty path NonPalindromic PCE");
        }

        // Get palindromic Service-Layer path in reverse-direction
        List<TopoEdge> zaServiceLayerERO = new LinkedList<>();

        // 1. Reverse the links
        for(TopoEdge azEdge : azServiceLayerERO)
        {
            Optional<TopoEdge> reverseEdge = prunedSlTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            reverseEdge.ifPresent(zaServiceLayerERO::add);
        }

        // 2. Reverse the order
        Collections.reverse(zaServiceLayerERO);

        Map<String, List<TopoEdge>> theMap = new HashMap<>();

        // AZ Primary paths: Remove starting and ending ports
        if(azServiceLayerERO.get(0).getA().getVertexType().equals(VertexType.PORT)){
            azServiceLayerERO.remove(0);
        }
        if(azServiceLayerERO.get(azServiceLayerERO.size()-1).getZ().getVertexType().equals(VertexType.PORT)){
            azServiceLayerERO.remove(azServiceLayerERO.size()-1);
        }


        // ZA Primary Paths: remove starting and ending ports
        if(zaServiceLayerERO.get(0).getA().getVertexType().equals(VertexType.PORT)){
            zaServiceLayerERO.remove(0);
        }
        if(zaServiceLayerERO.get(zaServiceLayerERO.size()-1).getZ().getVertexType().equals(VertexType.PORT)){
            zaServiceLayerERO.remove(zaServiceLayerERO.size()-1);
        }

        if(!(azServiceLayerERO.size() == zaServiceLayerERO.size()))
            return  theMap;

        // Obtain physical ERO from Service-Layer EROs

        List<TopoEdge> azEROPrimary = serviceLayerTopology.getActualPrimaryERO(azServiceLayerERO);
        List<TopoEdge> azEROSecondary = serviceLayerTopology.getActualSecondaryERO(azServiceLayerERO);

        // AZ Primary paths: Remove starting and ending ports
        if(azEROPrimary.get(0).getA().getVertexType().equals(VertexType.PORT)){
            azEROPrimary.remove(0);
        }
        if(azEROPrimary.get(azEROPrimary.size()-1).getZ().getVertexType().equals(VertexType.PORT)){
            azEROPrimary.remove(azEROPrimary.size()-1);
        }

        // ZA Primary Paths: remove starting and ending ports
        if(azEROSecondary.get(0).getA().getVertexType().equals(VertexType.PORT)){
            azEROSecondary.remove(0);
        }
        if(azEROSecondary.get(azEROSecondary.size()-1).getZ().getVertexType().equals(VertexType.PORT)){
            azEROSecondary.remove(azEROSecondary.size()-1);
        }
        // No logical edges were used in this path - Only a single non-survivable Ethernet-only ERO will be returned
        if(azEROPrimary.equals(azEROSecondary))
        {
            theMap.put("az", azServiceLayerERO);
            theMap.put("za", zaServiceLayerERO);

            return theMap;
        }


        // Get palindromic Physical-Layer path in reverse-direction
        List<TopoEdge> zaEROPrimary = new LinkedList<>();
        List<TopoEdge> zaEROSecondary = new LinkedList<>();

        // 1. Reverse the links
        for(TopoEdge azEdge : azEROPrimary)
        {
            Optional<TopoEdge> reverseEdge = prunedPhysicalTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            reverseEdge.ifPresent(zaEROPrimary::add);
        }

        for(TopoEdge azEdge : azEROSecondary)
        {
            Optional<TopoEdge> reverseEdge = prunedPhysicalTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            reverseEdge.ifPresent(zaEROSecondary::add);
        }

        // 2. Reverse the order
        Collections.reverse(zaEROPrimary);
        Collections.reverse(zaEROSecondary);



        if(!(azEROPrimary.size() == zaEROPrimary.size()))
            return  theMap;
        if(!(azEROSecondary.size() == zaEROSecondary.size()))
            return  theMap;


        theMap.put("az1", azEROPrimary);
        theMap.put("za1", zaEROPrimary);
        theMap.put("az2", azEROSecondary);
        theMap.put("za2", zaEROSecondary);

        return theMap;
    }

    private void addPortToServiceMplsTopology(SurvivableServiceLayerTopology serviceLayerTopology, TopoVertex port, TopoVertex device) {
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

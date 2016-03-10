package net.es.oscars.ds.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.dao.UrnAdjcyRepository;
import net.es.oscars.ds.topo.ent.EDevice;
import net.es.oscars.ds.topo.ent.EUrnAdjcy;
import net.es.oscars.dto.topo.UrnEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Controller
public class TopoController {

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private DeviceRepository devRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn(ex.getMessage(), ex);
    }


    @RequestMapping(value = "/device/{urn}", method = RequestMethod.GET)
    @ResponseBody
    public EDevice device(@PathVariable("urn") String urn) {
        EDevice eDevice = devRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
        log.info(eDevice.toString());
        return eDevice;
    }

    @RequestMapping(value = "/topo/layer/{layer}", method = RequestMethod.GET)
    @ResponseBody
    public Topology layer(@PathVariable("layer") String layer) {

        log.info("topology for layer " + layer);
        Layer eLayer = Layer.get(layer).orElseThrow(NoSuchElementException::new);
        Topology topo = new Topology();
        topo.setLayer(eLayer);
        List<EDevice> devices = devRepo.findAll();
        List<EUrnAdjcy> adjcies = adjcyRepo.findAll();

        devices.stream()
                .filter(d -> d.getCapabilities().contains(eLayer))
                .forEach(d -> {

                    TopoVertex dev = new TopoVertex(d.getUrn());
                    topo.getVertices().add(dev);

                    d.getIfces().stream()
                            .filter(i -> i.getCapabilities().contains(eLayer))
                            .forEach(i -> {
                                TopoVertex ifce = new TopoVertex(i.getUrn());
                                topo.getVertices().add(ifce);

                                UrnEdge edge = new UrnEdge(d.getUrn(), i.getUrn());
                                edge.getMetrics().put(eLayer, 1L);
                                topo.getEdges().add(edge);
                            });
                });

        adjcies.stream()
                .filter(adj -> adj.getMetrics().containsKey(eLayer))
                .forEach(adj -> {
                    Long metric = adj.getMetrics().get(eLayer);
                    UrnEdge edge = new UrnEdge(adj.getA(), adj.getZ());
                    edge.getMetrics().put(eLayer, metric);
                    topo.getEdges().add(edge);
                });


        return topo;
    }


}
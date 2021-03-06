package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.helpers.test.AsymmTopologyBuilder;
import net.es.oscars.helpers.test.TopologyBuilder;
import net.es.oscars.topo.ent.BidirectionalPathE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTestSurvivablePartial
{

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private AsymmTopologyBuilder asymmTopologyBuilder;

    @Test
    public void survivablePceTest1()
    {
        log.info("Initializing test: 'survivablePceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeK";
        String srcPort = "portA";
        String dstPort = "portZ";
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivable = SurvivabilityType.SURVIVABILITY_PARTIAL;

        topologyBuilder.buildTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, dstDevice, srcPort, dstPort, azBW, zaBW, palindrome, survivable, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("Beginning test: 'survivablePceTest1'.");
    }

    @Test
    public void survivablePceTest2()
    {
        log.info("Initializing test: 'survivablePceTest2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest2' passed.");
    }

    @Test
    public void survivablePceTest3()
    {
        log.info("Initializing test: 'survivablePceTest3'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        log.info("ETH:  " + allResEthPipes.size());
        log.info("MPLS: " + allResMplsPipes.size());

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 1);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeP"));

            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);
            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (theFix.getIfceUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            String expectedAzPrimaryERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
            String expectedZaPrimaryERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            String expectedAzSecondaryERO = "nodeP-nodeP:3-nodeR:1-nodeR-nodeR:2-nodeQ:2-nodeQ";
            String expectedZaSecondaryERO = "nodeQ-nodeQ:2-nodeR:2-nodeR-nodeR:1-nodeP:3-nodeP";

            assert (theFix.getIfceUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
            assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

            if(actualAzERO.equals(expectedAzPrimaryERO))
            {
                assert(actualZaERO.equals(expectedZaPrimaryERO));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzSecondaryERO))
            {
                assert(actualZaERO.equals(expectedZaSecondaryERO));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest3' passed.");
    }

    @Test
    public void survivablePceTest4()
    {
        log.info("Initializing test: 'survivablePceTest4'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 2);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeL") || aJunc.getDeviceUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM") || zJunc.getDeviceUrn().equals("nodeP") || zJunc.getDeviceUrn().equals("nodeR"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO1 = "nodeL-nodeL:1-nodeK:1-nodeK";
                String expectedZaERO2 = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
                assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));
            }
            else if(aJunc.getDeviceUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";

                assert (zJunc.getDeviceUrn().equals("nodeP"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeM-nodeM:3-nodeR:1-nodeR";
                String expectedZaERO = "nodeR-nodeR:1-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().equals("nodeR"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeP") || aJunc.getDeviceUrn().equals("nodeR"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

            if(aJunc.getDeviceUrn().equals("nodeP"))
            {
                String expectedAzPrimaryERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaPrimaryERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
                String expectedAzSecondaryERO = "nodeP-nodeP:3-nodeR:2-nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaSecondaryERO = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:2-nodeP:3-nodeP";

                assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
                assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

                if(actualAzERO.equals(expectedAzPrimaryERO))
                {
                    assert(actualZaERO.equals(expectedZaPrimaryERO));
                    usedEro1 = true;
                }

                if(actualAzERO.equals(expectedAzSecondaryERO))
                {
                    assert(actualZaERO.equals(expectedZaSecondaryERO));
                    usedEro2 = true;
                }
            }
            else
            {
                String expectedAzPrimaryERO = "nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaPrimaryERO = "nodeQ-nodeQ:2-nodeR:3-nodeR";
                String expectedAzSecondaryERO = "nodeR-nodeR:2-nodeP:3-nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaSecondaryERO = "nodeQ-nodeQ:1-nodeP:2-nodeP-nodeP:3-nodeR:2-nodeR";

                assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
                assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

                if(actualAzERO.equals(expectedAzPrimaryERO))
                {
                    assert(actualZaERO.equals(expectedZaPrimaryERO));
                    usedEro1 = true;
                }

                if(actualAzERO.equals(expectedAzSecondaryERO))
                {
                    assert(actualZaERO.equals(expectedZaSecondaryERO));
                    usedEro2 = true;
                }
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest4' passed.");
    }

    @Test
    public void survivablePceTest4_2()
    {
        log.info("Initializing test: 'survivablePceTest4_2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo4_2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest4_2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 0);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

        String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL-nodeL:3-nodeP:1-nodeP-nodeP:2-nodeQ:1-nodeQ";
        String expectedZaERO1 = "nodeQ-nodeQ:1-nodeP:2-nodeP-nodeP:1-nodeL:3-nodeL-nodeL:1-nodeK:1-nodeK";
        String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM-nodeM:3-nodeR:1-nodeR-nodeR:3-nodeQ:2-nodeQ";
        String expectedZaERO2 = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:1-nodeM:3-nodeM-nodeM:1-nodeK:2-nodeK";

        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));

            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (zFix.getIfceUrn().equals("portZ"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
            assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));

            if(actualAzERO.equals(expectedAzERO1))
            {
                assert(actualZaERO.equals(expectedZaERO1));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzERO2))
            {
                assert(actualZaERO.equals(expectedZaERO2));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest4_2' passed.");
    }

    @Test
    public void survivablePceTest5()
    {
        log.info("Initializing test: 'survivablePceTest5'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 2);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().equals("nodeP") || zJunc.getDeviceUrn().equals("nodeS"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeP"));
                assert (theFix.getIfceUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                String expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";

                assert (zJunc.getDeviceUrn().equals("nodeS"));
                assert (theFix.getIfceUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzPrimaryERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
            String expectedZaPrimaryERO = "nodeQ-nodeQ:1-nodeP:3-nodeP";
            String expectedAzSecondaryERO = "nodeP-nodeP:4-nodeR:2-nodeR-nodeR:3-nodeQ:2-nodeQ";
            String expectedZaSecondaryERO = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:2-nodeP:4-nodeP";

            assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
            assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

            if(actualAzERO.equals(expectedAzPrimaryERO))
            {
                assert(actualZaERO.equals(expectedZaPrimaryERO));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzSecondaryERO))
            {
                assert(actualZaERO.equals(expectedZaSecondaryERO));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest5' passed.");
    }

    @Test
    public void survivablePceTest6()
    {
        log.info("Initializing test: 'survivablePceTest6'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeP";
        String srcPort = "portA";
        String dstPort = "portZ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivable = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, dstDevice, srcPort, dstPort, azBW, zaBW, palindrome, survivable, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest6' passed.");
    }

    @Test
    public void survivablePceTest7()
    {
        log.info("Initializing test: 'survivablePceTest7'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeL";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 1);
        assert (allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeL"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'survivablePceTest7' passed.");
    }

    @Test
    public void survivablePceTest8()
    {
        log.info("Initializing test: 'survivablePceTest8'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest8' passed.");
    }

    @Test
    public void survivablePceTest9()
    {
        log.info("Initializing test: 'survivablePceTest9'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeP";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest9' passed.");
    }

    @Test
    public void survivablePceTest10()
    {
        log.info("Initializing test: 'survivablePceTest10'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 2);
        assert (allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            log.info(aJunc.getDeviceUrn());
            log.info(zJunc.getDeviceUrn());
            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'survivablePceTest10' passed.");
    }

    @Test
    public void survivablePceTest11()
    {
        log.info("Initializing test: 'survivablePceTest11'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest11' passed.");
    }

    @Test
    public void survivablePceTest12()
    {
        log.info("Initializing test: 'survivablePceTest12'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest12' passed.");
    }

    @Test
    public void survivablePceTest13()
    {
        log.info("Initializing test: 'survivablePceTest13'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo13();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest13'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 0);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeR"));

            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

            String expectedAzERO1 = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
            String expectedZaERO1 = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP";
            String expectedAzERO2 = "nodeP-nodeP:2-nodeS:1-nodeS-nodeS:2-nodeR:2-nodeR";
            String expectedZaERO2 = "nodeR-nodeR:2-nodeS:2-nodeS-nodeS:1-nodeP:2-nodeP";

            assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
            assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));

            if(actualAzERO.equals(expectedAzERO1))
            {
                assert(actualZaERO.equals(expectedZaERO1));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzERO2))
            {
                assert(actualZaERO.equals(expectedZaERO2));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest13' passed.");
    }

    @Test
    public void survivablePceTest14()
    {
        log.info("Initializing test: 'survivablePceTest14'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildMultiMplsTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest14'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest14' passed.");
    }

    @Test
    public void survivablePceTest15()
    {
        log.info("Initializing test: 'survivablePceTest15'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeT";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildMultiMplsTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTest15'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();


        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 3);
        assert (allResMplsPipes.size() == 4);

        boolean usedEro1_1 = false;
        boolean usedEro2_1 = false;
        boolean usedEro1_2 = false;
        boolean usedEro2_2 = false;

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            log.info("Eth Pipe: " + ethPipe.getAJunction().getDeviceUrn() + "----" + ethPipe.getZJunction().getDeviceUrn());

            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().equals("nodeL") || aJunc.getDeviceUrn().equals("nodeP") || aJunc.getDeviceUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().equals("nodeP") || zJunc.getDeviceUrn().equals("nodeQ") || zJunc.getDeviceUrn().equals("nodeS"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzERO;
            String expectedZaERO;

            if(aJunc.getDeviceUrn().equals("nodeL"))
            {
                assert(zJunc.getDeviceUrn().equals("nodeP"));
                expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";
            }
            else if(aJunc.getDeviceUrn().equals("nodeP"))
            {
                assert(zJunc.getDeviceUrn().equals("nodeQ"));
                expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            }
            else
            {
                assert(zJunc.getDeviceUrn().equals("nodeS"));
                expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";
            }

            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert ((aJunc.getDeviceUrn().equals("nodeK") && zJunc.getDeviceUrn().equals("nodeL"))
                    || (aJunc.getDeviceUrn().equals("nodeS") && zJunc.getDeviceUrn().equals("nodeT")));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                assert (theFix.getIfceUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));

                String expectedAzPrimaryERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaPrimaryERO = "nodeL-nodeL:1-nodeK:1-nodeK";
                String expectedAzSecondaryERO = "nodeK-nodeK:2-nodeM:1-nodeM-nodeM:2-nodeL:2-nodeL";
                String expectedZaSecondaryERO = "nodeL-nodeL:2-nodeM:2-nodeM-nodeM:1-nodeK:2-nodeK";

                assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
                assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

                if(actualAzERO.equals(expectedAzPrimaryERO))
                {
                    assert (actualZaERO.equals(expectedZaPrimaryERO));
                    usedEro1_1 = true;
                }

                if(actualAzERO.equals(expectedAzSecondaryERO))
                {
                    assert (actualZaERO.equals(expectedZaSecondaryERO));
                    usedEro2_1 = true;
                }
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                assert (theFix.getIfceUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

                String expectedAzPrimaryERO = "nodeS-nodeS:2-nodeT:1-nodeT";
                String expectedZaPrimaryERO = "nodeT-nodeT:1-nodeS:2-nodeS";
                String expectedAzSecondaryERO = "nodeS-nodeS:3-nodeU:1-nodeU-nodeU:2-nodeT:2-nodeT";
                String expectedZaSecondaryERO = "nodeT-nodeT:2-nodeU:2-nodeU-nodeU:1-nodeS:3-nodeS";

                assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
                assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

                if(actualAzERO.equals(expectedAzPrimaryERO))
                {
                    assert (actualZaERO.equals(expectedZaPrimaryERO));
                    usedEro1_2 = true;
                }

                if(actualAzERO.equals(expectedAzSecondaryERO))
                {
                    assert (actualZaERO.equals(expectedZaSecondaryERO));
                    usedEro2_2 = true;
                }
            }
        }

        assert(usedEro1_1 && usedEro1_2 && usedEro2_1 && usedEro2_2);

        log.info("test 'survivablePceTest15' passed.");
    }

    @Test
    public void survivablePartialPceTestESnet()
    {
        log.info("Initializing test: 'survivablePartialPceTestESnet'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // ISSUE: albq (router) has ports on the Ethernet layer. Those ports don't appear on the MPLS layer, but
        // the edges connecting albq to those ports are included in MPLS.
        // Test passes, but it's kinda weird.
        // Also look into what service layer topology looks like
        // Have seen examples where it's (srcPort) -> (virtual srcPort) |-> (virtual srcDevice)
        //                                                              |-> (rest of network)
        // With the virtual srcDevice not connected to anything else.

        String srcPort = "wash-cr5:10/1/10";
        String srcDevice = "wash-cr5";
        String dstPort = "denv-cr5:10/1/11";
        String dstDevice = "denv-cr5";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopoEsnet();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "survTest");

        log.info("Beginning test: 'survivablePartialPceTestESnet'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());
        Set<BidirectionalPathE> paths = reservedBlueprint.get().getVlanFlow().getAllPaths();
        assert(paths.size() == 2);
        assert(paths.stream().allMatch(path -> path.getAzPath().size() > 0 && path.getZaPath().size() > 0));

        log.info("test 'survivablePartialPceTestESnet' passed.");
    }

    @Test
    public void survivablePceTestNonUniformPorts()
    {
        log.info("Initializing test: 'survivablePceTestNonUniformPorts'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portP:1";
        String srcDevice = "nodeP";
        String dstPort = "portQ:1";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopoWithNonUniformPorts();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "test");

        log.info("Beginning test: 'survivablePceTestNonUniformPorts'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        log.info("test 'survivablePceTestNonUniformPorts' passed.");
    }
}

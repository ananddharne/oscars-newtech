package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.resv.ent.*;
import net.es.oscars.helpers.test.VlanTranslationTopologyBuilder;
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
public class VlanTranslationTest {

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private VlanTranslationTopologyBuilder vlanTopologyBuilder;

    @Test
    /**
     * Same VLAN Reserved everywhere
     */
    public void vlanTransTest1(){
        log.info("Initializing test: 'vlanTransTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "3";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, String> urnMap = new HashMap<>();
        Map<String, Set<Integer>> reservedVlanMap = new HashMap<>();

        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        String a0 = urnMap.getOrDefault("A:0", null);
        String a1 = urnMap.getOrDefault("A:1", null);
        String a2 = urnMap.getOrDefault("A:2", null);
        String b0 = urnMap.getOrDefault("B:0", null);
        String b1 = urnMap.getOrDefault("B:1", null);
        String b2 = urnMap.getOrDefault("B:2", null);
        String c0 = urnMap.getOrDefault("C:0", null);
        String c1 = urnMap.getOrDefault("C:1", null);
        String c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(reservedVlanMap.get(a0).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(a1).contains(3));
        // Src Fixture
        assert(reservedVlanMap.get(a2).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(b0).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(b1).contains(3));
        // Unused
        assert(reservedVlanMap.get(b2).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(c0).contains(3));
        // Unused
        assert(reservedVlanMap.get(c1).contains(3));
        // Src Fixture
        assert(reservedVlanMap.get(c2).contains(3));

        assert(reservedVlanMap.get(a1).equals(reservedVlanMap.get(b0)));
        assert(reservedVlanMap.get(b0).equals(reservedVlanMap.get(b1)));
        assert(reservedVlanMap.get(b1).equals(reservedVlanMap.get(c0)));

        log.info("Finished test: vlanTransTest1");
    }

    @Test
    /**
     * VLAN 2 on src, VLAN 3 on dst, VLAN 2 or 3 In-between
     */
    public void vlanTransTest2(){

        log.info("Initializing test: 'vlanTransTest2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "2";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, String> urnMap = new HashMap<>();
        Map<String, Set<Integer>> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        String a0 = urnMap.getOrDefault("A:0", null);
        String a1 = urnMap.getOrDefault("A:1", null);
        String a2 = urnMap.getOrDefault("A:2", null);
        String b0 = urnMap.getOrDefault("B:0", null);
        String b1 = urnMap.getOrDefault("B:1", null);
        String b2 = urnMap.getOrDefault("B:2", null);
        String c0 = urnMap.getOrDefault("C:0", null);
        String c1 = urnMap.getOrDefault("C:1", null);
        String c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(reservedVlanMap.get(a0).contains(2));
        // Intermediate
        assert(reservedVlanMap.get(a1).contains(2) || reservedVlanMap.get(a1).contains(3));
        // Src Fixture
        assert(reservedVlanMap.get(a2).contains(2));
        // Intermediate
        assert(reservedVlanMap.get(b0).contains(2) || reservedVlanMap.get(b0).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(b1).contains(2) || reservedVlanMap.get(b1).contains(3));
        // Unused
        assert(reservedVlanMap.get(b2).contains(2));
        // Intermediate
        assert(reservedVlanMap.get(c0).contains(2) || reservedVlanMap.get(c0).contains(3));
        // Unused
        assert(reservedVlanMap.get(a1).contains(2));
        // Dst Fixture
        assert(reservedVlanMap.get(c2).contains(2) || reservedVlanMap.get(c2).contains(3));

        assert((reservedVlanMap.get(a1).contains(2) && reservedVlanMap.get(a1).contains(3)) ||
                (reservedVlanMap.get(c0).contains(2) && reservedVlanMap.get(c0).contains(3)));

        log.info("Finished test: vlanTransTest2");
    }

    @Test
    /**
     * VLAN 3 on src, VLAN 2 on dst, VLAN 2 or 3 in-between
     */
    public void vlanTransTest3(){

        log.info("Initializing test: 'vlanTransTest3'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "3";
        String dstVlan = "2";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, String> urnMap = new HashMap<>();
        Map<String, Set<Integer>> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        String a0 = urnMap.getOrDefault("A:0", null);
        String a1 = urnMap.getOrDefault("A:1", null);
        String a2 = urnMap.getOrDefault("A:2", null);
        String b0 = urnMap.getOrDefault("B:0", null);
        String b1 = urnMap.getOrDefault("B:1", null);
        String b2 = urnMap.getOrDefault("B:2", null);
        String c0 = urnMap.getOrDefault("C:0", null);
        String c1 = urnMap.getOrDefault("C:1", null);
        String c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(reservedVlanMap.get(a0).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(a1).contains(2) || reservedVlanMap.get(a1).contains(3));
        // Src Fixture
        assert(reservedVlanMap.get(a2).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(b0).contains(2) || reservedVlanMap.get(b0).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(b1).contains(2) || reservedVlanMap.get(b1).contains(3));
        // Unused
        assert(reservedVlanMap.get(b2).contains(3));
        // Intermediate
        assert(reservedVlanMap.get(c0).contains(2) || reservedVlanMap.get(c0).contains(3));
        // Unused
        assert(reservedVlanMap.get(c1).contains(2));
        // Dst Fixture
        assert(reservedVlanMap.get(c2).contains(2));

        assert((reservedVlanMap.get(a1).contains(2) && reservedVlanMap.get(a1).contains(3)) ||
                (reservedVlanMap.get(c0).contains(2) && reservedVlanMap.get(c0).contains(3)));

        log.info("Finished test: vlanTransTest3");
    }

    @Test
    /**
     * Request VLAN 3 on src and dest, only 4 and 5 available in-between. Fail due to junctions being switches.
     */
    public void vlanTransTest4(){

        log.info("Initializing test: 'vlanTransTest4'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "3";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("Finished test: vlanTransTest4");
    }

    @Test
    /**
     * VLAN 3 requested on src and dest, but only {1-2} available everywhere. Fail.
     */
    public void vlanTransTest5(){

        log.info("Initializing test: 'vlanTransTest5'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "3";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("Finished test: vlanTransTest5");
    }

    @Test
    /**
     * Request VLAN 3 on src, VLAN 2 on dst, only {1,2} available everywhere. Fail.
     */
    public void vlanTransTest6(){

        log.info("Initializing test: 'vlanTransTest6'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "3";
        String dstVlan = "2";

        vlanTopologyBuilder.buildVlanTransTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("Finished test: vlanTransTest6");
    }

    @Test
    /**
     * Request VLAN 2 on src, VLAN 3 on dst, only {1,2} available everywhere. Fail.
     */
    public void vlanTransTest7(){

        log.info("Initializing test: 'vlanTransTest7'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "2";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        log.info("Beginning test: 'vlanTransTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("Finished test: vlanTransTest7");
    }

    @Test
    /**
     * Request junction with VLAN 5 on all fixtures. Request pipe with 5 on src and 5 on dest. Should be forced to
     * use different VLAN elsewhere.
     */
    public void vlanTransTest8(){

        log.info("Initializing test: 'vlanTransTest8'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "5";
        String dstVlan = "5";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, srcVlan, dstVlan, 1, 1, 1, "test");

        // Now add the junction
        RequestedVlanJunctionE reqJunction = testBuilder.buildRequestedJunction("B", Arrays.asList("B:0", "B:1", "B:2"), 25, 25, "5", true);
        Set<RequestedVlanJunctionE> reqJunctions = new HashSet<>();
        reqJunctions.add(reqJunction);
        requestedBlueprint.getVlanFlow().setJunctions(reqJunctions);

        log.info("Beginning test: 'vlanTransTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 1);

        Map<String, String> urnMap = new HashMap<>();
        Map<String, Set<Integer>> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes, junctions);

        String a0 = urnMap.getOrDefault("A:0", null);
        String a1 = urnMap.getOrDefault("A:1", null);
        String a2 = urnMap.getOrDefault("A:2", null);
        String b0 = urnMap.getOrDefault("B:0", null);
        String b1 = urnMap.getOrDefault("B:1", null);
        String b2 = urnMap.getOrDefault("B:2", null);
        String c0 = urnMap.getOrDefault("C:0", null);
        String c1 = urnMap.getOrDefault("C:1", null);
        String c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(reservedVlanMap.get(a0).contains(1) && reservedVlanMap.get(a0).contains(5));
        // Intermediate
        assert(reservedVlanMap.get(a1).contains(5) && reservedVlanMap.get(a1).contains(1));
        // Src Fixture
        assert(reservedVlanMap.get(a2).contains(5) && reservedVlanMap.get(a2).contains(1));
        // Intermediate
        assert(reservedVlanMap.get(b0).contains(1) && reservedVlanMap.get(b0).contains(5));
        // Intermediate
        assert(reservedVlanMap.get(b1).contains(1) && reservedVlanMap.get(b1).contains(5));
        // Unused
        assert(reservedVlanMap.get(b2).contains(1) && reservedVlanMap.get(b2).contains(5));
        // Intermediate
        assert(reservedVlanMap.get(c0).contains(5) && reservedVlanMap.get(c0).contains(1));
        // Unused
        assert(reservedVlanMap.get(c1).contains(5) && reservedVlanMap.get(c1).contains(1));
        // Dst Fixture
        assert(reservedVlanMap.get(c2).contains(5) && reservedVlanMap.get(c2).contains(1));


        // Test the junction
        ReservedVlanJunctionE resJunction = junctions.iterator().next();
        Set<ReservedVlanFixtureE> resJunctionFixtures = resJunction.getFixtures();
        assert(resJunctionFixtures.stream().map(ReservedVlanFixtureE::getReservedVlans).flatMap(Collection::stream).map(ReservedVlanE::getVlan).allMatch(v -> v == 5));

        log.info("Finished test: vlanTransTest8");
    }

    @Test
    /**
     * Request pipe with 3 on src, 4 on dst. Then request pipe with 5 on src, 5 on dst. Then request pipe with
     * 2 on src (B:1), 2 on dst (C:1).
     */
    public void vlanTransTest9(){

        log.info("Initializing test: 'vlanTransTest8'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String srcVlan = "3";
        String dstVlan = "4";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedVlanPipeE pipeA = testBuilder.buildRequestedPipe(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW,
                palindrome, survivability, srcVlan, dstVlan, 1, Integer.MAX_VALUE);
        RequestedVlanPipeE pipeB = testBuilder.buildRequestedPipe(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW,
                palindrome, survivability, "5", "5", 1, Integer.MAX_VALUE);
        RequestedVlanPipeE pipeC = testBuilder.buildRequestedPipe("B:0", "B", "C:1", "C", azBW, zaBW, palindrome,
                survivability, "2", "2", 1, Integer.MAX_VALUE);

        requestedBlueprint = testBuilder.buildRequest(new HashSet<>(Arrays.asList(pipeA, pipeB, pipeC)), 3, 3, "test");

        log.info("Beginning test: 'vlanTransTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 5);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, String> urnMap = new HashMap<>();
        Map<String, Set<Integer>> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        String a0 = urnMap.getOrDefault("A:0", null);
        String a1 = urnMap.getOrDefault("A:1", null);
        String a2 = urnMap.getOrDefault("A:2", null);
        String b0 = urnMap.getOrDefault("B:0", null);
        String b1 = urnMap.getOrDefault("B:1", null);
        String b2 = urnMap.getOrDefault("B:2", null);
        String c0 = urnMap.getOrDefault("C:0", null);
        String c1 = urnMap.getOrDefault("C:1", null);
        String c2 = urnMap.getOrDefault("C:2", null);

        /* Request pipe with 3 on src, 4 on dst. Then request pipe with 5 on src, 5 on dst. Then request pipe with
        * 2 on src (B:1), 2 on dst (C:1).
        * */
        // Unused
        assert(reservedVlanMap.get(a0).contains(3) && reservedVlanMap.get(a0).contains(5));
        // Intermediate
        assert(reservedVlanMap.get(a1).contains(3) && reservedVlanMap.get(a1).contains(5));
        // Src Fixture
        assert(reservedVlanMap.get(a2).contains(3) && reservedVlanMap.get(a2).contains(5));
        // Intermediate
        assert((reservedVlanMap.get(b0).contains(3) || reservedVlanMap.get(b0).contains(4)) && reservedVlanMap.get(b0).contains(2) && reservedVlanMap.get(b0).contains(5));
        // Intermediate
        assert((reservedVlanMap.get(b1).contains(3) || reservedVlanMap.get(b1).contains(4)) && reservedVlanMap.get(b1).contains(2) && reservedVlanMap.get(b1).contains(5));
        // Unused
        assert(reservedVlanMap.get(b2).contains(2) && reservedVlanMap.get(b2).contains(3) && reservedVlanMap.get(b2).contains(5));
        // Intermediate
        assert((reservedVlanMap.get(c0).contains(3) && reservedVlanMap.get(c0).contains(4)) && reservedVlanMap.get(c0).contains(2) && reservedVlanMap.get(c0).contains(5));
        // Unused
        assert(reservedVlanMap.get(c1).contains(2) && reservedVlanMap.get(c1).contains(4) && reservedVlanMap.get(c1).contains(5));
        // Dst Fixture
        assert(reservedVlanMap.get(c2).contains(2) && reservedVlanMap.get(c2).contains(4) && reservedVlanMap.get(c2).contains(5));

        log.info("Finished test: vlanTransTest8");
    }

    private void buildUrnAndReservedVlanMap(Map<String, String> urnMap, Map<String, Set<Integer>> reservedVlanMap,
                                            Set<ReservedEthPipeE> ethPipes){
        for(ReservedEthPipeE pipe : ethPipes){
            //assert(pipe.getReservedVlans().stream().map(ReservedVlanE::getVlan).distinct().count() == 1);

            ReservedVlanJunctionE aJunction = pipe.getAJunction();
            ReservedVlanJunctionE zJunction = pipe.getZJunction();

            for(ReservedVlanE resVlan :  pipe.getReservedVlans()){
                urnMap.putIfAbsent(resVlan.getUrn(), resVlan.getUrn());
                reservedVlanMap.putIfAbsent(resVlan.getUrn(), new HashSet<>());
                reservedVlanMap.get(resVlan.getUrn()).add(resVlan.getVlan());
            }

            for(ReservedVlanE resVlan : aJunction.getReservedVlans()){
                urnMap.putIfAbsent(resVlan.getUrn(), resVlan.getUrn());
                reservedVlanMap.putIfAbsent(resVlan.getUrn(), new HashSet<>());
                reservedVlanMap.get(resVlan.getUrn()).add(resVlan.getVlan());
            }

            for(ReservedVlanE resVlan : zJunction.getReservedVlans()){
                urnMap.putIfAbsent(resVlan.getUrn(), resVlan.getUrn());
                reservedVlanMap.putIfAbsent(resVlan.getUrn(), new HashSet<>());
                reservedVlanMap.get(resVlan.getUrn()).add(resVlan.getVlan());
            }
        }
    }

    private void buildUrnAndReservedVlanMap(Map<String, String> urnMap, Map<String, Set<Integer>> reservedVlanMap,
                                            Set<ReservedEthPipeE> ethPipes, Set<ReservedVlanJunctionE> resJunctions){
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);
        for(ReservedVlanJunctionE resJunction: resJunctions){
            for(ReservedVlanE resVlan : resJunction.getReservedVlans()){
                urnMap.putIfAbsent(resVlan.getUrn(), resVlan.getUrn());
                reservedVlanMap.putIfAbsent(resVlan.getUrn(), new HashSet<>());
                reservedVlanMap.get(resVlan.getUrn()).add(resVlan.getVlan());
            }
        }
    }
}

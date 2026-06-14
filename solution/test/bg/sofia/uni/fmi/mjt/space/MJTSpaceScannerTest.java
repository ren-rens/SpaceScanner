package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Detail;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MJTSpaceScannerTest {

    private final Detail detail1 = new Detail("Falcon 9 Block 5", "Starlink V1 L9 & BlackSky");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd, yyyy", Locale.ENGLISH);
    private final LocalDate date1 = LocalDate.parse("Fri Aug 07, 2020", formatter);
    private final Optional<Double> cost1 = Optional.of(50.0);
    private final Mission mission1 = new Mission("0", "SpaceX",
        "LC-39A, Kennedy Space Center, Florida, USA", date1,
        detail1, RocketStatus.STATUS_ACTIVE, cost1, MissionStatus.SUCCESS
    );

    private final Detail detail2 = new Detail("Starship Prototype", "150 Meter Hop");
    private final LocalDate date2 = LocalDate.parse("Tue Aug 04, 2020", formatter);
    private final Optional<Double> cost2 = Optional.empty();
    private final Mission mission2 = new Mission("2", "SpaceX",
        "Pad A, Boca Chica, Texas, USA", date2,
        detail2, RocketStatus.STATUS_RETIRED, cost2, MissionStatus.FAILURE);


    private final Detail detail3 = new Detail("Proton-M/Briz-M", "kspress-80 & Ekspress-103");
    private final LocalDate date3 = LocalDate.parse("Thu Jul 30, 2020", formatter);
    private final Optional<Double> cost3 = Optional.of(65.0);
    private final Mission mission3 = new Mission("3", "Roscosmos",
        "Site 200/39, Baikonur Cosmodrome, Kazakhstan", date3,
        detail3, RocketStatus.STATUS_ACTIVE, cost3, MissionStatus.SUCCESS);

    private final LocalDate date4 = LocalDate.parse("Tue Jun 30, 2020", formatter);
    private final Detail detail4 = new Detail("Falcon 9 Block 5", "GPS III SV03");
    private final Mission mission4 = new Mission("4", "SpaceX",
        "LC-39A, Kennedy Space Center, Florida, USA", date4,
        detail4, RocketStatus.STATUS_ACTIVE, cost1, MissionStatus.SUCCESS
        );

    // test getAllMissions()
    @Test
    void testGetAllMissionsWithNullMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(List.of(), scanner.getAllMissions(),
            "When testing getAllMissions with NULL missions should return empty collection");
    }

    @Test
    void testGetAllMissionsWithNoMissions() {

        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals(List.of(), scanner.getAllMissions(),
            "When testing getAllMissions with NO missions should return empty collection");
    }

    @Test
    void testGetAllMissionsWithMissionsThatHaveAllNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);
        missions.add(null);
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(List.of(), scanner.getAllMissions(),
            "When testing getAllMissions() with missions list that has some null elements " +
                "should ignore them and return non-null filled empty list");
    }

    @Test
    void testGetAllMissionsWithMissionsThatHaveNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);
        missions.add(mission1);
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(List.of(mission1), scanner.getAllMissions(),
            "When testing getAllMissions() with missions list that has some null elements " +
                "should ignore them and return non-null filled list");
    }

    @Test
    void testGetAllMissionsWithValidMissions() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission2);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(List.of(mission1, mission2), scanner.getAllMissions(),
            "When testing getAllMissions() with valid missions list " +
                "should return unmodifiable list of all elements");
    }

    // test getAllMissions(MissionStatus)
    @Test
    void testGetAllMissionsWithGivenStatusThatIsNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getAllMissions(null),
            "When testing getAllMissions(MissionStatus) with status NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetAllMissionsWithGivenStatusWhenMissionsIsNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(List.of(), scanner.getAllMissions(MissionStatus.SUCCESS),
            "When testing getAllMissions(MissionStatus) with missions NULL" +
                "should return empty list");
    }

    @Test
    void testGetAllMissionsWithGivenStatusWhenMissionsIsEmpty() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals(List.of(), scanner.getAllMissions(MissionStatus.SUCCESS),
            "When testing getAllMissions(MissionStatus) with missions empty" +
                "should return empty list");
    }

    @Test
    void testGetAllMissionsWithGivenStatusWhenMissionsHasNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);
        missions.add(mission1);
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(List.of(mission1), scanner.getAllMissions(MissionStatus.SUCCESS),
            "When testing getAllMissions(MissionStatus) with missions that has NULL elements" +
                "should ignore them and return unmodifiable list with no NULL elements");
    }

    @Test
    void testGetAllMissionsWithGivenStatusWhenMissionsAreValid() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission2);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(List.of(mission1), scanner.getAllMissions(MissionStatus.SUCCESS),
            "When testing getAllMissions(MissionStatus) with valid missions" +
                "should return unmodifiable list with all missions of the given status");
    }

    // test getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to)
    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenToAndFromAreNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(null, null),
            "When testing getCompanyWithMostSuccessfulMissions with to and from NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenFromIsNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(null, date1),
            "When testing getCompanyWithMostSuccessfulMissions with from NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenToIsNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(date1, null),
            "When testing getCompanyWithMostSuccessfulMissions with to NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenToBeforeFrom() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(TimeFrameMismatchException.class,
            () -> scanner.getCompanyWithMostSuccessfulMissions(date1, date2),
            "When testing getCompanyWithMostSuccessfulMissions with to before from" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenMissionsIsNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals("", scanner.getCompanyWithMostSuccessfulMissions(date2, date1),
            "When testing getCompanyWithMostSuccessfulMissions with NULL missions" +
                "should return empty string");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenMissionsIsEmpty() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals("", scanner.getCompanyWithMostSuccessfulMissions(date2, date1),
            "When testing getCompanyWithMostSuccessfulMissions with NO missions" +
                "should return empty string");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenMissionsHasAllNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals("", scanner.getCompanyWithMostSuccessfulMissions(date2, date1),
            "When testing getCompanyWithMostSuccessfulMissions with missions that have ALL null elements" +
                "should ignore them and return empty string");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWhenMissionsHasNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);
        missions.add(mission1);
        missions.add(null);
        missions.add(mission2);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(mission1.company(), scanner.getCompanyWithMostSuccessfulMissions(date2, date1),
            "When testing getCompanyWithMostSuccessfulMissions with missions that have null elements" +
                "should ignore them and return the company with the most successful missions in a given time period");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWithMissionsThatAreNotInTimeline() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission2);
        missions.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(mission1.company(), scanner.getCompanyWithMostSuccessfulMissions(date2, date1),
            "When testing getCompanyWithMostSuccessfulMissions with missions that have elements not within the timeline" +
                "should ignore them and return the company with the most successful missions in a given time period");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsWithValidMissions() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission2);
        missions.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(mission3.company(), scanner.getCompanyWithMostSuccessfulMissions(date3, date2),
            "When testing getCompanyWithMostSuccessfulMissions with valid missions " +
                "should return the company with the most successful missions in a given time period");
    }

    // test getMissionsPerCountry()
    @Test
    void testGetMissionsPerCountryWithNullMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(Map.of(), scanner.getMissionsPerCountry(),
            "When testing getMissionsPerCountry and there are NULL missions" +
                "should return an empty map.");
    }

    @Test
    void testGetMissionsPerCountryWithNoMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals(Map.of(), scanner.getMissionsPerCountry(),
            "When testing getMissionsPerCountry and there are no missions" +
                "should return an empty map.");
    }

    @Test
    void testGetMissionsPerCountryWithMissionsThatHaveAllNullElements() {
        List<Mission> mission = new ArrayList<>();
        mission.add(null);
        mission.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(Map.of(), scanner.getMissionsPerCountry(),
            "When testing getMissionsPerCountry and there are missions with null elements" +
                "should ignore them and return an empty map.");
    }

    @Test
    void testGetMissionsPerCountryWithMissionsThatHaveNullElements() {
        List<Mission> mission = new ArrayList<>();
        mission.add(null);
        mission.add(mission1);
        mission.add(mission2);
        mission.add(mission3);
        mission.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);
        Map<String, Collection<Mission>> result = Map.of("USA", List.of(mission1, mission2),
            "Kazakhstan", List.of(mission3));

        assertEquals(result, scanner.getMissionsPerCountry(),
            "When testing getMissionsPerCountry and there are missions with null elements" +
                "should ignore them and return grouped missions by country");
    }

    @Test
    void testGetMissionsPerCountryWithValidMissions() {
        List<Mission> mission = new ArrayList<>();
        mission.add(mission1);
        mission.add(mission2);
        mission.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);
        Map<String, Collection<Mission>> result = Map.of("USA", List.of(mission1, mission2),
            "Kazakhstan", List.of(mission3));

        assertEquals(result, scanner.getMissionsPerCountry(),
            "When testing getMissionsPerCountry with valid mission" +
                "should return grouped missions by country");
    }

    // test getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus)
    @Test
    void testGetTopNLeastExpensiveMissionsWithNegativeN() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class,
            () -> scanner.getTopNLeastExpensiveMissions(-1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with negative n" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithZeroN() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class,
            () -> scanner.getTopNLeastExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with zero n" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithNullMissionStatus() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class,
            () -> scanner.getTopNLeastExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with mission status NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithNullRocketStatus() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class,
            () -> scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, null),
            "When testing getTopNLeastExpensiveMissions with rocket status NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithNullMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(List.of(),
            scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with missions NULL" +
                "should return empty list");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithEmptyMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(new ArrayList<>(), null);

        assertEquals(List.of(),
            scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with empty missions" +
                "should return empty list");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithMissionsThatHaveAllNullElements() {
        List<Mission> mission = new ArrayList<>();
        mission.add(null);
        mission.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(List.of(),
            scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with missions that have all NULL elements" +
                "should ignore them and return empty list");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithMissionsThatHaveNullElements() {
        List<Mission> mission = new ArrayList<>();
        mission.add(null);
        mission.add(mission1);
        mission.add(mission3);
        mission.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(List.of(mission1, mission3),
            scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with missions that have some NULL elements" +
                "should ignore them and return the top N least expensive missions, ordered from cheapest to more expensive");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithMissionsThaHasElementsNotWithMissionStatus() {
        List<Mission> mission = new ArrayList<>();
        mission.add(mission1);
        mission.add(mission2);
        mission.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(List.of(mission1, mission3),
            scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with missions that have some elements with different mission status" +
                "should ignore them and return the top N least expensive missions, ordered from cheapest to more expensive");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithMissionsThatHasElementsNotWithRocketStatus() {
        List<Mission> mission = new ArrayList<>();
        mission.add(mission1);
        mission.add(mission2);
        mission.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(List.of(mission1, mission3),
            scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with missions that have some elements with different rocket status" +
                "should ignore them and return the top N least expensive missions, ordered from cheapest to more expensive");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithInvalidMissionsStatusAndRocketStatus() {
        List<Mission> mission = new ArrayList<>();
        mission.add(mission1);
        mission.add(mission2);
        mission.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(List.of(),
            scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_RETIRED),
            "When testing getTopNLeastExpensiveMissions with missions that have ALL elements with different rocket and mission status" +
                "should ignore them and return empty list");
    }

    @Test
    void testGetTopNLeastExpensiveMissionsWithValidMissions() {
        List<Mission> mission = new ArrayList<>();
        mission.add(mission2); // should ignore this missions because cost is empty
        mission.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(mission, null);

        assertEquals(List.of(mission3),
            scanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getTopNLeastExpensiveMissions with valid missions" +
                "should return the top N least expensive missions, ordered from cheapest to more expensive");
    }

    // test getMostDesiredLocationForMissionsPerCompany()
    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyWithNullMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(Map.of(), scanner.getMostDesiredLocationForMissionsPerCompany(),
            "When testing getMostDesiredLocationForMissionsPerCompany() with NULL missions" +
                "should return empty map");
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyWithEmptyMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals(Map.of(), scanner.getMostDesiredLocationForMissionsPerCompany(),
            "When testing getMostDesiredLocationForMissionsPerCompany() with empty missions" +
                "should return empty map");
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyWithMissionsThatHasAllNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(), scanner.getMostDesiredLocationForMissionsPerCompany(),
            "When testing getMostDesiredLocationForMissionsPerCompany() with missions that has all null elements" +
                "should ignore them and return empty map");
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyWithMissionsThatHasSomeNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);
        missions.add(mission1);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(mission1.company(), mission1.location()), scanner.getMostDesiredLocationForMissionsPerCompany(),
            "When testing getMostDesiredLocationForMissionsPerCompany() with missions that has all null elements" +
                "should ignore them and return a map where keys are company names and values are their most used mission locations");
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyWithValidMissions() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission2);
        missions.add(mission3);
        missions.add(mission4);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(mission4.company(), mission4.location(),
                mission3.company(), mission3.location()), scanner.getMostDesiredLocationForMissionsPerCompany(),
            "When testing getMostDesiredLocationForMissionsPerCompany() with valid missions" +
                "should return a map where keys are company names and values are their most used mission locations");
    }

    // getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to)
    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithNullFrom() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getLocationWithMostSuccessfulMissionsPerCompany(null, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with NULL from should throw IllegalArgumentException");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithNullTo() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getLocationWithMostSuccessfulMissionsPerCompany(date1, null),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with NULL to should throw IllegalArgumentException");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithToBeforeFrom() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(TimeFrameMismatchException.class, () -> scanner.getLocationWithMostSuccessfulMissionsPerCompany(date1, date2),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with to before from should throw TimeFrameMismatchException");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithNullMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(Map.of(), scanner.getLocationWithMostSuccessfulMissionsPerCompany(date2, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with NULL missions" +
                "should return empty map");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWitheEmptyMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals(Map.of(), scanner.getLocationWithMostSuccessfulMissionsPerCompany(date2, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with empty missions" +
                "should return empty map");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithMissionsThatHaveAllNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(), scanner.getLocationWithMostSuccessfulMissionsPerCompany(date2, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with missions that have all null elements" +
                "should ignore them and return empty map");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithMissionsThatHaveSomeNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(mission1.company(), mission1.location()), scanner.getLocationWithMostSuccessfulMissionsPerCompany(date2, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with missions that have some null elements" +
                "should ignore them and return the location with most successful missions for each company in a given time period");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithMissionsThatHaveElementsNotInTimeframe() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(mission1.company(), mission1.location()), scanner.getLocationWithMostSuccessfulMissionsPerCompany(date2, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with missions that have some elements not in timeframe" +
                "should ignore them and return the location with most successful missions for each company in a given time period");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithMissionsThatHaveElementsNotSuccessfulStatus() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission3);
        missions.add(mission2);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(mission3.company(), mission3.location()),
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(date3, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with missions that have some elements with not successful status" +
                "should ignore them and return the location with most successful missions for each company in a given time period");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithValidMissions() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);
        missions.add(mission3);
        missions.add(mission4);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null);

        assertEquals(Map.of(mission1.company(), mission1.location(),
                mission3.company(), mission3.location()), scanner.getLocationWithMostSuccessfulMissionsPerCompany(date4, date1),
            "When testing getLocationWithMostSuccessfulMissionsPerCompany with missions that have some elements not in timeframe" +
                "should ignore them and return the location with most successful missions for each company in a given time period");
    }

    private final Optional<String> wiki1 = Optional.of("https://en.wikipedia.org/wiki/Tsyklon-3");
    private final  Optional<Double> height1 = Optional.of(39.0);
    private final Rocket rocket1 = new Rocket("0", "Falcon 9 Block 5",
        wiki1, height1);

    private final Optional<String> wiki2 = Optional.of("https://en.wikipedia.org/wiki/Vostok-2_(rocket)");
    private final  Optional<Double> height2 = Optional.empty();
    private final Rocket rocket2 = new Rocket("1", "Starship Prototype",
        wiki2, height2);

    private final Optional<String> wiki3 = Optional.empty();
    private final  Optional<Double> height3 = Optional.of(19.0);
    private final Rocket rocket3 = new Rocket("2", "Proton-M/Briz-M",
        wiki3, height3);

    // test getAllRockets()
    @Test
    void testGetAllRocketsWithNullRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(List.of(), scanner.getAllRockets(),
            "When testing getAllRockets() with NULL rockets" +
                "should return empty list");
    }

    @Test
    void testGetAllRocketsWithEmptyRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, List.of());

        assertEquals(List.of(), scanner.getAllRockets(),
            "When testing getAllRockets() with EMPTY rockets" +
                "should return empty list");
    }

    @Test
    void testGetAllRocketsWithRocketsThatHaveAllNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(List.of(), scanner.getAllRockets(),
            "When testing getAllRockets() with rockets that have ALL NULL elements" +
                "should ignore them and return empty list");
    }

    @Test
    void testGetAllRocketsWithRocketsThatHaveSomeNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);
        rockets.add(rocket1);
        rockets.add(null);
        rockets.add(rocket2);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(List.of(rocket1, rocket2), scanner.getAllRockets(),
            "When testing getAllRockets() with rockets that have some NULL elements" +
                "should ignore them and return all rockets in the dataset");
    }

    // test getTopNTallestRockets(int n)
    @Test
    void testGetTopNTallestRocketsWithNegativeN() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getTopNTallestRockets(-1),
            "When testing getTopNTallestRockets with negative n" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetTopNTallestRocketsWithZeroN() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () -> scanner.getTopNTallestRockets(0),
            "When testing getTopNTallestRockets with zero n" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetTopNTallestRocketsWithNullRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(List.of(), scanner.getTopNTallestRockets(1),
            "When testing getTopNTallestRockets with NULL rockets" +
                "should return empty list");
    }

    @Test
    void testGetTopNTallestRocketsWithNEmptyRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, List.of());

        assertEquals(List.of(), scanner.getTopNTallestRockets(1),
            "When testing getTopNTallestRockets with empty rockets" +
                "should return empty list");
    }

    @Test
    void testGetTopNTallestRocketsWithNRocketsThatHaveAllNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(List.of(), scanner.getTopNTallestRockets(1),
            "When testing getTopNTallestRockets with rockets that have ALL NULL elements" +
                "should ignore them and return empty list");
    }

    @Test
    void testGetTopNTallestRocketsWithNRocketsThatHaveSomeNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);
        rockets.add(rocket1);
        rockets.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(List.of(rocket1), scanner.getTopNTallestRockets(1),
            "When testing getTopNTallestRockets with rockets that have some NULL elements" +
                "should ignore them and return the top N tallest rockets, in decreasing order");
    }

    @Test
    void testGetTopNTallestRocketsWithNRocketsThatHaveSomeElementsWithNoHeight() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(rocket1);
        rockets.add(rocket2); // should ignore this rocket because height is empty

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(List.of(rocket1), scanner.getTopNTallestRockets(2),
            "When testing getTopNTallestRockets with rockets that have some elements with NO height" +
                "should fill the height with minimum int value and return the top N tallest rockets, in decreasing order");
    }

    @Test
    void testGetTopNTallestRocketsWithNValidRockets() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(rocket1);
        rockets.add(rocket3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(List.of(rocket1, rocket3), scanner.getTopNTallestRockets(2),
            "When testing getTopNTallestRockets with rockets that have some elements with NO height" +
                "should fill the height with minimum int value and return the top N tallest rockets, in decreasing order");
    }

    // test getWikiPageForRocket()
    @Test
    void testGetWikiPageForRocketWithNullRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(Map.of(), scanner.getWikiPageForRocket(),
            "When testing getWikiPageForRocket with NULL rockets" +
                "should return empty map");
    }

    @Test
    void testGetWikiPageForRocketWithEmptyRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, List.of());

        assertEquals(Map.of(), scanner.getWikiPageForRocket(),
            "When testing getWikiPageForRocket with EMPTY rockets" +
                "should return empty map");
    }

    @Test
    void testGetWikiPageForRocketWithRocketsThatHaveAllNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(Map.of(), scanner.getWikiPageForRocket(),
            "When testing getWikiPageForRocket with rockets that have ALL NULL elements" +
                "should ignore them and return empty map");
    }

    @Test
    void testGetWikiPageForRocketWithRocketsThatHaveSomeNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);
        rockets.add(rocket1);
        rockets.add(rocket2);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(Map.of(rocket1.name(), rocket1.wiki(),
                rocket2.name(), rocket2.wiki()), scanner.getWikiPageForRocket(),
            "When testing getWikiPageForRocket with rockets that have some NULL elements" +
                "should ignore them and return a mapping of rockets (by name) to their respective wiki page (if present)");
    }

    @Test
    void testGetWikiPageForRocketWithRocketsThatHaveSomeElementsWithNoWikiPage() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(rocket1);
        rockets.add(rocket2);
        rockets.add(rocket3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(null, rockets);

        assertEquals(Map.of(rocket1.name(), rocket1.wiki(),
                rocket2.name(), rocket2.wiki(),
                rocket3.name(), rocket3.wiki()), scanner.getWikiPageForRocket(),
            "When testing getWikiPageForRocket with rockets that have some elements with no wiki page" +
                "should return a mapping of rockets (by name) to their respective wiki page (if present)");
    }

    @Test
    void testGetWikiPageForRocketWithDuplicateNames() {
        Rocket rocket1Duplicate = new Rocket("2", "Falcon 9 Block 5", Optional.of("wiki2"), Optional.of(70.0));
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, List.of(rocket1, rocket1));

        assertDoesNotThrow(() -> scanner.getWikiPageForRocket(),
            "When testing getWikiPage with duplicate rockets names" +
                "should handle duplicate rocket names without throwing");
    }

    // test getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus)
    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNegativeN() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(-1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with negative n" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithZeroN() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () ->
                scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with zero n" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNullMissionStatus() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () ->
                scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with NULL mission status" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNullRocketStatus() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class, () ->
                scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, null),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with NULL rocket status" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNullMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertEquals(List.of(),
                scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with NULL missions" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithEmptyMissions() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with EMPTY missions" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNullRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1), null);

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with NULL rockets" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithEmptyRockets() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1), List.of());

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with EMPTY rockets" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithMissionsThatHaveAllNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(missions, List.of());

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with missions that have ALL NULL elements" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithRocketsThatHaveAllNullElements() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(null);

        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1), rockets);

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with rockets that have ALL NULL elements" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithRocketsThatHaveNoWikiPage() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(rocket1);
        rockets.add(rocket3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1, mission3), rockets);

        assertEquals(List.of(wiki1.get()),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with rockets that have elements with no wiki page" +
                "should ignore them and return the wiki pages for the rockets used in the N most expensive missions");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithRocketsThatAreNotInMostExpensiveOnes() {
        List<Rocket> rockets = new ArrayList<>();
        rockets.add(rocket1);
        rockets.add(rocket3);

        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1, mission3), rockets);

        assertEquals(List.of(rocket1.wiki().get()),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with rockets that have elements Not in most expensive missions" +
                "should still return them if limit is not covered");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithMissionWithIncorrectMissionStatus() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1),
            List.of(rocket1));

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.FAILURE, RocketStatus.STATUS_ACTIVE),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with missions that are not in the correct mission status" +
                "should return empty list");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithMissionWithIncorrectRocketStatus() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1),
            List.of(rocket1));

        assertEquals(List.of(),
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_RETIRED),
            "When testing getWikiPageForRocketsUsedInMostExpensiveMissions with missions that are not in the correct rocket status" +
                "should return empty list");
    }

    // test saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to)
    @Test
    void testSaveMostReliableRocketWithOutputStreamNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        assertThrows(IllegalArgumentException.class,
            () -> scanner.saveMostReliableRocket(null, date2, date1),
            "When testing saveMostReliableRocket with output stream NULL" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testSaveMostReliableRocketWithFromNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile.toFile());

            assertThrows(IllegalArgumentException.class,
                () -> scanner.saveMostReliableRocket(outputStream, null, date2),
                "When testing saveMostReliableRocket with from NULL" +
                    "should throw IllegalArgumentException");

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveMostReliableRocketWithToNull() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile.toFile());

            assertThrows(IllegalArgumentException.class,
                () -> scanner.saveMostReliableRocket(outputStream, date1, null),
                "When testing saveMostReliableRocket with to NULL" +
                    "should throw IllegalArgumentException");

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveMostReliableRocketWithToBeforeFrom() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile.toFile());

            assertThrows(TimeFrameMismatchException.class,
                () -> scanner.saveMostReliableRocket(outputStream, date1, date2),
                "When testing saveMostReliableRocket with to before from" +
                    "should throw TimeFrameMismatchException");

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveMostReliableRocketWhenEncryptOperationIsNotSuccessful() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(mission1), List.of(rocket1), new CipherThrowingExceptionStub());

        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile.toFile());

            assertThrows(CipherException.class,
                () -> scanner.saveMostReliableRocket(outputStream, date2, date1),
                "When testing saveMostReliableRocket when encrypt operation cannot end successfully" +
                    "should throw CipherException");

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveMostReliableRocketWithNullMission() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(null, null);

        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile.toFile());

            assertDoesNotThrow(() -> scanner.saveMostReliableRocket(outputStream, date2, date1),
                "When testing testSaveMostReliableRocket with NULL mission " +
                    "should not throw anything and do anything");

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveMostReliableRocketWithEmptyMission() {
        SpaceScannerAPI scanner = new MJTSpaceScanner(List.of(), null);

        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp");
            OutputStream outputStream = new FileOutputStream(tempFile.toFile());

            assertDoesNotThrow(() -> scanner.saveMostReliableRocket(outputStream, date2, date1),
                "When testing testSaveMostReliableRocket with EMPTY mission " +
                    "should not throw anything and do anything");

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveMostReliableRocketWithMissionThatHaveNullElements() {
        List<Mission> missions = new ArrayList<>();
        missions.add(null);

        try {
            // Generate a secret key for the cipher
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();

            SymmetricBlockCipher cipher = new Rijndael(secretKey);
            SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null, cipher);

            // Create temporary file for output
            Path tempFile = Files.createTempFile("testOutput", ".tmp");

            try (OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {

                assertDoesNotThrow(() -> scanner.saveMostReliableRocket(outputStream, date2, date1),
                    "When testing saveMostReliableRocket with missions that have NULL elements, " +
                        "should ignore them and not throw anything");

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                // Cleanup
                Files.deleteIfExists(tempFile);
            }
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Test
    void testSaveMostReliableRocketWithValidMission() {
        List<Mission> missions = new ArrayList<>();
        missions.add(mission1);  // Falcon 9 Block 5, MissionStatus.SUCCESS, Aug 07
        missions.add(mission2);  // Starship Prototype, MissionStatus.FAILURE, Aug 04
        missions.add(mission4);  // Falcon 9 Block 5, MissionStatus.SUCCESS, Jun 30

        try {
            SecretKey secretKey = generateSecretKey();
            SymmetricBlockCipher cipher = new Rijndael(secretKey);
            SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null, cipher);

            Path encryptedFile = Files.createTempFile("testOutput", ".encrypted");
            Path decryptedFile = Files.createTempFile("testDecrypted", ".txt");

            try {
                encryptMostReliableRocket(scanner, encryptedFile, date4, date1);

                String decryptedRocketName = decryptAndReadRocketName(cipher, encryptedFile, decryptedFile);

                assertEquals("Falcon 9 Block 5", decryptedRocketName,
                    "The most reliable rocket should be 'Falcon 9 Block 5' with 2 successful missions");

            } finally {
                cleanupFiles(encryptedFile, decryptedFile);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private void encryptMostReliableRocket(SpaceScannerAPI scanner, Path encryptedFile,
                                           LocalDate from, LocalDate to) throws IOException {
        try (OutputStream encryptOutputStream = new FileOutputStream(encryptedFile.toFile())) {
            // just in case to see whether saveMostReliableRocket ended successfully
            assertDoesNotThrow(() -> scanner.saveMostReliableRocket(encryptOutputStream, from, to),
                "saveMostReliableRocket should not throw with valid missions");
        }
    }

    private String decryptAndReadRocketName(SymmetricBlockCipher cipher, Path encryptedFile,
                                            Path decryptedFile) throws IOException {
        try (InputStream encryptedInputStream = new FileInputStream(encryptedFile.toFile());
             OutputStream decryptedOutputStream = new FileOutputStream(decryptedFile.toFile())) {
            cipher.decrypt(encryptedInputStream, decryptedOutputStream);
        } catch (CipherException e) {
            fail("Decryption failed: " + e.getMessage());
        }

        return Files.readString(decryptedFile).trim();
    }

    private void cleanupFiles(Path... files) {
        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException _) {

            }
        }
    }

    @Test
    void testSaveMostReliableRocketWithDateRangeFilter() {
        List<Mission> missions = List.of(mission1, mission2, mission3, mission4);

        try {
            SecretKey secretKey = generateSecretKey();
            SymmetricBlockCipher cipher = new Rijndael(secretKey);
            SpaceScannerAPI scanner = new MJTSpaceScanner(missions, null, cipher);

            Path encryptedFile = Files.createTempFile("testEncrypted", ".tmp");
            Path decryptedFile = Files.createTempFile("testDecrypted", ".tmp");

            try {
                // Only include missions from Aug 01 to Aug 07
                // This should only include mission1 (Falcon 9 - SUCCESS) and mission2 (Starship - FAILURE)
                encryptMostReliableRocketWithCipher(scanner, cipher, encryptedFile,
                    LocalDate.parse("Sat Aug 01, 2020", formatter), date1);
                String decryptedRocketName = decryptAndReadRocketNameWithCipher(cipher, encryptedFile, decryptedFile);

                assertEquals("Falcon 9 Block 5", decryptedRocketName,
                    "Falcon 9 Block 5 should be most reliable in Aug 01-07 range");

            } finally {
                cleanupFiles(encryptedFile, decryptedFile);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }


    private void encryptMostReliableRocketWithCipher(SpaceScannerAPI scanner, SymmetricBlockCipher cipher,
                                                     Path encryptedFile, LocalDate from, LocalDate to)
        throws IOException {
        try (OutputStream outputStream = new FileOutputStream(encryptedFile.toFile())) {
            scanner.saveMostReliableRocket(outputStream, from, to);
        } catch (CipherException e) {
            fail("Encryption failed: " + e.getMessage());
        }
    }

    private String decryptAndReadRocketNameWithCipher(SymmetricBlockCipher cipher, Path encryptedFile,
                                                      Path decryptedFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(encryptedFile.toFile());
             OutputStream outputStream = new FileOutputStream(decryptedFile.toFile())) {
            cipher.decrypt(inputStream, outputStream);
        } catch (CipherException e) {
            fail("Decryption failed: " + e.getMessage());
        }

        return Files.readString(decryptedFile);
    }

    // testing loaders using contractor
    // (it is stupid to test constructors but in order to get higher code coverage is necessary)
    @Test
    void testConstructorWithNullSecretKey() {
        assertThrows(IllegalArgumentException.class, () ->
            new MJTSpaceScanner(Reader.of("all-missions-from-1957.csv"),
                Reader.of("all-rockets-from-1957.csv"), null),
            "When trying to create MJTSpaceScanner with NULL secret key" +
                "should throw IllegalArgumentException");
    }

    @Test
    void testConstructorWithValidLoadersAndSecretKey() {
        String missions = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;
        Reader missionReader = new StringReader(missions);

        String rockets = """
            ID|Name|Wiki|Rocket Height
            1|Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            """;
        Reader rocketsReader = new StringReader(rockets);

        try {
            SecretKey key = KeyGenerator.getInstance("AES").generateKey();
            assertDoesNotThrow(() ->
                    new MJTSpaceScanner(missionReader, rocketsReader, key),
                "When trying to create MJTSpaceScanner with valid readers" +
                    "should not throw any exception and success");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        }

}
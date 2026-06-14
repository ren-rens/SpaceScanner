package bg.sofia.uni.fmi.mjt.space.loader;

import bg.sofia.uni.fmi.mjt.space.mission.Detail;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MissionLoaderTest {

    private Loader<Mission> loader = new MissionLoader();

    private final String id1 = "1";
    private final String company1= "CASC";
    private final String location1 = "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China";
    private final LocalDate date1 = LocalDate.of(2020, 8, 6);
    private final Detail detail1 = new Detail("Long March 2D", "Gaofen-9 04 & Q-SAT");
    private final RocketStatus rocketStatus1 = RocketStatus.STATUS_ACTIVE;
    private final Optional<Double> cost1 = Optional.of(29.75);
    private final MissionStatus missionStatus1 = MissionStatus.SUCCESS;

    private final Mission mission1 = new Mission(id1, company1, location1,
        date1, detail1, rocketStatus1, cost1, missionStatus1);

    private final Optional<Double> cost2 = Optional.empty();
    private final Mission mission2 = new Mission(id1, company1, location1,
        date1, detail1, rocketStatus1, cost2, missionStatus1);

    @Test
    void testLoadWithValidMission() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(mission1), loader.load(new StringReader(input)),
            "When testing mission loading with valid input " +
                "should return a list of all missions in the input");
    }

    @Test
    void testLoadWithInputMissingSomeFields() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            ,,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,,Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing some fields" +
                "should ignore that line");
    }

    @Test
    void testLoadWithMissingIdField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            ,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing mandatory fields" +
                "should ignore that");
    }

    @Test
    void testLoadWithMissingCompanyField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing company field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithMissingLocationField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CSCA,,"Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing location field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithMissingDateField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CSCA,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China",,Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing date field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInvalidDateField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CSCA,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","2020-10-10",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is invalid date field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputMissingDetailField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CSCA,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing detail field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputHavingInvalidDetailFieldMissingTheDelimiter() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CSCA,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that has invalid detail field" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputHavingInvalidDetailFieldBlankRocketName() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            ,,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020", | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that has invalid detail field" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputHavingInvalidDetailFieldBlankPayload() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            ,,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020", Long March 2D | ,StatusActive,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that has invalid detail field" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputMissingRocketStatusField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading input that is missing rocket status field (mandatory) " +
                "should ignore that line in the input");
    }

    @Test
    void testLoadWithInputHavingInvalidRocketStatusField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,STATUS_READY,"29.75 ",Success
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading input that has invalid rocket status field (mandatory) " +
                "should ignore that line in the input");
    }

    @Test
    void testLoadWithInputThatMissesCostField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,,Success
        """;

        assertEquals(List.of(mission2), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing a cost field" +
                "should make the cost Optional.empty() and save the mission like that");
    }

    @Test
    void testLoadWithInputMissingMissionStatusField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"50.0",
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input that is missing a mission field" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputHavingIncorrectMissionStatusField() {
        String input = """
            Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
            1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"50.0",SUCCESS
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing mission loading with input having incorrect a mission field" +
                "should ignore that line");
    }

    @Test
    void testLoadWhenReaderThrowsException() {
        assertEquals(List.of(), loader.load(new ReaderThrowingExceptionStub()),
            "When testing load with a Reader that throw exception when reading" +
                "should return the list as far as it has come until the moment of interruption");
    }

    @Test
    void testLoadWithTooFewFields() {
        String input = """
        a,b,c
        1,2,3
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing load with too few fields" +
                "should not include that line in the result");
    }

}
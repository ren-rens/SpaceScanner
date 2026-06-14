package bg.sofia.uni.fmi.mjt.space.loader;

import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocketLoaderTest {

    private final Loader<Rocket> loader = new RocketLoader();

    private final String id1 = "1";
    private final String name1 = "Tsyklon-4M";
    private final Optional<String> wiki1 = Optional.of("https://en.wikipedia.org/wiki/Cyclone-4M");
    private final Optional<Double> height1 = Optional.of(38.7);
    private final Rocket rocket1 = new Rocket(id1, name1, wiki1, height1);

    private final String id2 = "2";
    private final Optional<String> wiki2 = Optional.empty();
    private final Optional<Double> height2 = Optional.empty();
    private final Rocket rocket2 = new Rocket(id2, name1, wiki2, height2);

    @Test
    void testLoadWithValidInput() {
        String input = """
            ID|Name|Wiki|Rocket Height
            1|Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            """;

        assertEquals(List.of(rocket1), loader.load(new StringReader(input)),
            "When testing load with valid input" +
                "should return a list of all rockets");
    }

    @Test
    void testLoadWithInputThatHasMissingOptionalFields() {
        String input = """
            ID|Name|Wiki|Rocket Height
            1|Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            2|Tsyklon-4M||
            """;

        assertEquals(List.of(rocket1, rocket2), loader.load(new StringReader(input)),
            "When testing load with input that has missing optional fields" +
                "should save the missing data as Optional.empty() and save the rockets");
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
        a
        1
        """;

        assertEquals(List.of(), loader.load(new StringReader(input)),
            "When testing load with too few fields" +
                "should not include that line in the result");
    }


    @Test
    void testLoadWithInvalidHeightFieldMissingMeters() {
        String input = """
            ID|Name|Wiki|Rocket Height
            1|Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            2|Tsyklon-4M||40.0 
            """;

        assertEquals(List.of(rocket1, rocket2), loader.load(new StringReader(input)),
            "When testing load with input that has missing meters in height fields" +
                "should save the height as empty");
    }

    @Test
    void testLoadWithInvalidHeightFieldMissingNumber() {
        String input = """
            ID|Name|Wiki|Rocket Height
            1|Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            2|Tsyklon-4M|| m 
            """;

        assertEquals(List.of(rocket1, rocket2), loader.load(new StringReader(input)),
            "When testing load with input that has missing the number of meters in height fields" +
                "should save the height as empty");
    }

    @Test
    void testLoadWithInputMissingIdField() {
        String input = """
            ID|Name|Wiki|Rocket Height
            |Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            2|Tsyklon-4M||
            """;

        assertEquals(List.of(rocket2), loader.load(new StringReader(input)),
            "When testing load with input that has line missing id field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputMissingNameField() {
        String input = """
            ID|Name|Wiki|Rocket Height
            1||https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            2|Tsyklon-4M||
            """;

        assertEquals(List.of(rocket2), loader.load(new StringReader(input)),
            "When testing load with input that has line missing name field (mandatory)" +
                "should ignore that line");
    }

    @Test
    void testLoadWithInputHavingInvalidHeightFieldNotANumberHeight() {
        String input = """
            ID|Name|Wiki|Rocket Height
            1|Tsyklon-4M|https://en.wikipedia.org/wiki/Cyclone-4M|38.7 m
            2|Tsyklon-4M||abc m
            """;

        assertEquals(List.of(rocket1, rocket2), loader.load(new StringReader(input)),
            "When testing load with input that incorrect height field (the height is not a double)" +
                "should ignore that line");
    }

}

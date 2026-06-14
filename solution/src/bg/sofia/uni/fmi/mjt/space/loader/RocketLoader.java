package bg.sofia.uni.fmi.mjt.space.loader;

import bg.sofia.uni.fmi.mjt.space.loader.constants.RocketIndex;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RocketLoader implements Loader<Rocket> {

    @Override
    public List<Rocket> load(Reader reader) {
        List<Rocket> result = new ArrayList<>();

        try (var bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine(); // skip the header

            while ((line = bufferedReader.readLine()) != null) {

                Rocket rocket = parseRocket(line); // return null if line is invalid
                if (rocket == null) {
                    continue;
                }

                result.add(rocket);
            }
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    private static Rocket parseRocket(String line) {
        String[] parts = Loader.parseCSVLine(line, DELIMITER_COMMA, DELIMITER_SLASH);
        if (parts.length < MIN_SPLIT_SIZE) {
            return null;
        }

        try {
            String id = parts[RocketIndex.ID.getIndex()];
            String name = parts[RocketIndex.NAME.getIndex()];
            Optional<String> wiki = parseWiki(parts[RocketIndex.WIKI.getIndex()]);
            Optional<Double> height = parseHeight(parts[RocketIndex.HEIGHT.getIndex()]);

            if (!validateRocket(id, name)) {
                return null;
            }
            return new Rocket(id, name, wiki, height);
        } catch (Exception e) {
            return null;
        }
    }

    private static Optional<String> parseWiki(String wikiStr) {
        return wikiStr.isBlank() ?
            Optional.empty() : Optional.of(wikiStr.trim());
    }

    private static Optional<Double> parseHeight(String heightStr) {
        if (heightStr.isBlank()) {
            return Optional.empty();
        }

        String[] split = heightStr.split(" "); // because it is displayed like 40.0 m
        if (split.length < MIN_SPLIT_SIZE) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(split[0].trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static boolean validateRocket(String id, String name) {
        return !id.isBlank() && !name.isBlank();
    }

    private static final int MIN_SPLIT_SIZE = 2;

    private static final char DELIMITER_COMMA = ',';
    private static final char DELIMITER_SLASH = '|';

}

package bg.sofia.uni.fmi.mjt.space.analyzers;

import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RocketAnalyzer {

    // getAllRockets()
    public static Collection<Rocket> getAllRockets(List<Rocket> rockets) {
        if (rockets == null || rockets.isEmpty()) {
            return List.of();
        }

        return rockets
            .stream()
            .filter(Objects::nonNull)
            .toList();
    }

    // getTopNTallestRockets(int n)
    public static List<Rocket> getTopNTallestRockets(List<Rocket> rockets, int n) {
        if (!validateGetTopNTallestRockets(rockets, n)) {
            return List.of();
        }

        return rockets
            .stream()
            .filter(Objects::nonNull)
            .filter(rocket -> rocket.height().isPresent())
            .sorted(Comparator.comparingDouble((Rocket rocket) ->
                rocket.height().get()).reversed())
            .limit(n)
            .toList();
    }

    private static boolean validateGetTopNTallestRockets(List<Rocket> rockets, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Invalid n: <=0");
        }

        return rockets != null && !rockets.isEmpty();
    }

    // getWikiPageForRocket()
    public static Map<String, Optional<String>> getWikiPageForRocket(List<Rocket> rockets) {
        if (rockets == null || rockets.isEmpty()) {
            return Map.of();
        }

        return rockets
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                Rocket::name,
                Rocket::wiki,
                (exists, _) -> exists  // keep first occurrence just in case
            ));
    }

    // getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus)
    public static List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(
        List<Rocket> rockets, List<Mission> missions, int n,
        MissionStatus missionStatus, RocketStatus rocketStatus) {

        if (!validateGetWikiPagesForRocketsUsedInMostExpensiveMissions(n, missionStatus, rocketStatus, missions)) {
            return List.of();
        }

        List<Mission> mostExpensiveMissions = getMostExpensiveMissions(missions, missionStatus, rocketStatus);
        Map<String, Rocket> rocketMap = getRocketMapRocketNameRocket(rockets);

        return getResultForWikiPagesForRocketsUsedInMostExpensiveMissions(mostExpensiveMissions, rocketMap, n);

    }

    private static boolean validateGetWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                              RocketStatus rocketStatus,
                                                                              List<Mission> missions) {
        if (n <= 0 || missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("Invalid n: <= 0 OR " +
                "missionStatus/rocketStatus: NULL");
        }

        return missions != null && !missions.isEmpty();
    }

    private static List<Mission> getMostExpensiveMissions(List<Mission> missions,
                                                          MissionStatus missionStatus,
                                                          RocketStatus rocketStatus) {
        return missions
            .stream()
            .filter(Objects::nonNull)
            .filter(mission -> mission.cost().isPresent())
            .filter(mission -> mission.missionStatus().equals(missionStatus))
            .filter(mission -> mission.rocketStatus().equals(rocketStatus))
            .sorted((m1, m2) -> Double.compare(m2.cost().get(), m1.cost().get()))
            .toList();

    }

    private static Map<String, Rocket> getRocketMapRocketNameRocket(List<Rocket> rockets) {
        if (rockets == null || rockets.isEmpty()) {
            return Map.of();
        }

        Map<String, Rocket> rocketMap = new HashMap<>(); // <rocketName, Rocket>
        for (Rocket rocket : rockets) {
            if (rocket == null) {
                continue;
            }

            rocketMap.put(rocket.name(), rocket);
        }

        return rocketMap;
    }

    private static List<String> getResultForWikiPagesForRocketsUsedInMostExpensiveMissions(
        List<Mission> mostExpensiveMissions, Map<String, Rocket> rocketMap, int n) {

        List<String> result = new ArrayList<>();
        Set<String> seenRocketNames = new HashSet<>(); // to ensure unique rockets

        for (Mission mission : mostExpensiveMissions) {
            if (result.size() == n) {
                break;
            }

            String rocketName = mission.detail().rocketName();
            if (seenRocketNames.contains(rocketName)) {
                continue;
            }

            Rocket currentRocket = rocketMap.get(rocketName);
            if (currentRocket == null) {
                continue;
            }

            Optional<String> wiki = currentRocket.wiki();
            if (wiki.isPresent()) {
                result.add(wiki.get());
                seenRocketNames.add(rocketName);
            }
        }

        return List.copyOf(result);
    }

    // saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to)
    public static void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to,
                                              SymmetricBlockCipher cipher, List<Mission> missions)
        throws CipherException {
        if (outputStream == null || from == null || to == null) {
            throw new IllegalArgumentException("Invalid outputStream/from/to: NULL");
        }

        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("Invalid to from: TO BEFORE FROM");
        }

        try (outputStream) {
            if (missions == null || missions.isEmpty()) {
                return; // outputStream will be closed when exiting the try-catch block
            }

            Map<String, List<Mission>> rocketCounts = getRocketsFromMissionsInTimeFrame(from, to, missions);
            String rocketName = getMostReliableRocket(rocketCounts);

            encryptMostReliableRocket(rocketName, outputStream, cipher);
        } catch (IOException e) {
            throw new CipherException("Could not close output file", e);
        }
    }

    private static Map<String, List<Mission>> getRocketsFromMissionsInTimeFrame(LocalDate from, LocalDate to,
                                                                                List<Mission> missions) {
        return missions
            .stream()
            .filter(Objects::nonNull)
            .filter(mission ->
                (mission.date().isEqual(from) || mission.date().isAfter(from)) &&
                    (mission.date().isEqual(to) || mission.date().isBefore(to))
            )
            .collect(Collectors.groupingBy(mission -> mission.detail().rocketName()));
    }

    private static String getMostReliableRocket(Map<String, List<Mission>> rocketCounts) {
        // reliability score:
        //(2 * (count of successful missions) + (count of failed missions)) / (2 * (count of all missions))
        if (rocketCounts.isEmpty()) {
            return "";
        }

        String mostReliableRocket = null;
        double mostReliableRocketScore = Integer.MIN_VALUE;

        for (Map.Entry<String, List<Mission>> mission : rocketCounts.entrySet()) {
            String currentRocket = mission.getKey();
            int countAllMissions = mission.getValue().size();
            int countSuccessfulMissions = getCountOfSuccessfulMissions(mission.getValue());
            int countFailedMissions = countAllMissions - countSuccessfulMissions;

            double currentScore = (2.0 * countSuccessfulMissions + countFailedMissions) / (2.0 * countAllMissions);
            if (Double.compare(currentScore, mostReliableRocketScore) > 0) {
                mostReliableRocketScore = currentScore;
                mostReliableRocket = currentRocket;
            }
        }

        return mostReliableRocket;
    }

    private static int getCountOfSuccessfulMissions(List<Mission> currentMissions) {
        int countSuccessfulMissions = 0;
        for (Mission current : currentMissions) {
            if (current.missionStatus().equals(MissionStatus.SUCCESS)) {
                countSuccessfulMissions++;
            }
        }

        return countSuccessfulMissions;
    }

    private static void encryptMostReliableRocket(String rocketName, OutputStream outputStream,
                                                  SymmetricBlockCipher cipher) throws CipherException {
        try {
            Path tempFile = Files.createTempFile("myTempFile", ".tmp"); // creating temp file
            Files.writeString(tempFile, rocketName); // write rocket name in temp file

            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                cipher.encrypt(inputStream, outputStream);
            } finally {
                Files.deleteIfExists(tempFile);  // Clean up temp file
            }

        } catch (IOException e) {
            throw new CipherException("Could not complete encrypt/decrypt operation successfully", e);
        }
    }

}
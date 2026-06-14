package bg.sofia.uni.fmi.mjt.space.analyzers;

import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MissionAnalyzer {

    // getAllMission()
    public static Collection<Mission> getAllMissions(List<Mission> missions) {
        if (missions == null || missions.isEmpty()) {
            return List.of();
        }

        return missions
            .stream()
            .filter(Objects::nonNull)
            .toList();
    }

    // getAllMissions(MissionStatus missionStatus)
    public static Collection<Mission> getAllMissions(List<Mission> missions, MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException("Invalid missionStatus: NULL");
        }

        if (missions == null || missions.isEmpty()) {
            return List.of();
        }

        return missions
            .stream()
            .filter(Objects::nonNull)
            .filter(mission -> mission.missionStatus().equals(missionStatus))
            .toList();
    }

    // getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to)
    public static String getCompanyWithMostSuccessfulMissions(List<Mission> missions,
                                                       LocalDate from, LocalDate to) {
        if (!validateGetCompanyWithMostSuccessfulMissions(missions, from, to)) {
            return "";
        }

        return getCompanyWithMostSuccessfulMissionsImpl(missions, from, to);
    }

    private static boolean validateGetCompanyWithMostSuccessfulMissions(List<Mission> missions,
                                                                 LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Invalid from/to: NULL");
        }

        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("Invalid to and from: TO BEFORE FROM");
        }

        return missions != null && !missions.isEmpty();
    }

    private static String getCompanyWithMostSuccessfulMissionsImpl(List<Mission> missions,
                                                           LocalDate from, LocalDate to) {
        Map<String, Long> companies = missions
            .stream()
            .filter(Objects::nonNull)
            .filter(mission ->
                (mission.date().isEqual(from) || mission.date().isAfter(from)) &&
                    (mission.date().isEqual(to) || mission.date().isBefore(to))
            )
            .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS))
            .collect(Collectors.groupingBy(Mission::company, Collectors.counting()));

        return companies
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
    }

    // getMissionsPerCountry()
    public static Map<String, Collection<Mission>> getMissionsPerCountry(List<Mission> missions) {
        if (missions == null || missions.isEmpty()) {
            return Map.of();
        }

        return getMissionsPerCountryImpl(missions);
    }

    private static Map<String, Collection<Mission>> getMissionsPerCountryImpl(List<Mission> missions) {
        Map<String, List<Mission>> missionsGroupedByLocation = missions
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(mission -> {
                String location = mission.location();
                String[] parts = location.split(",\\s*");
                return parts[parts.length - 1].trim();
            }));

        return missionsGroupedByLocation
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (Collection<Mission>) entry.getValue()
            ));
    }

    // getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus)
    public static List<Mission> getTopNLeastExpensiveMissions(List<Mission> missions, int n,
                                                       MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (!validateGetTopNLeastExpensiveMissions(missions, n, missionStatus, rocketStatus)) {
            return List.of();
        }

        return missions
            .stream()
            .filter(Objects::nonNull)
            .filter(mission -> mission.cost().isPresent())
            .filter(mission -> mission.missionStatus().equals(missionStatus))
            .filter(mission -> mission.rocketStatus().equals(rocketStatus))
            .sorted(Comparator.comparingDouble(mission -> mission.cost().get()))
            .limit(n)
            .toList();
    }

    private static boolean validateGetTopNLeastExpensiveMissions(List<Mission> missions, int n,
                                                          MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (n <= 0 || missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("Invalid n: <=0; " +
                "OR missionStatus/rocketStatus: NULL");
        }

        return missions != null && !missions.isEmpty();
    }

    // getMostDesiredLocationForMissionsPerCompany()
    public static Map<String, String> getMostDesiredLocationForMissionsPerCompany(List<Mission> missions) {
        if (missions == null || missions.isEmpty()) {
            return Map.of();
        }

        return getMostDesiredLocationForMissionsPerCompanyImpl(missions);
    }

    private static Map<String, String> getMostDesiredLocationForMissionsPerCompanyImpl(List<Mission> missions) {
        Map<String, Map<String, Long>> companyLocationsCount = missions
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Mission::company,
                Collectors.groupingBy(Mission::location, Collectors.counting())));

        return getCompanyWithLocationThatHasMostOccurrences(companyLocationsCount);
    }

    private static Map<String, String> getCompanyWithLocationThatHasMostOccurrences(Map<String,
        Map<String, Long>> companyLocationsCount) {

        return companyLocationsCount
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue()
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey()
            ));

    }

    // getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to)
    public static Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(List<Mission> missions,
                                                                               LocalDate from, LocalDate to) {
        if (!validateGetLocationWithMostSuccessfulMissionsPerCompany(missions, from, to)) {
            return Map.of();
        }

        return getLocationWithMostSuccessfulMissionsPerCompanyImpl(missions, from, to);
    }

    private static boolean validateGetLocationWithMostSuccessfulMissionsPerCompany(List<Mission> missions,
                                                                            LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Invalid from/to: NULL");
        }

        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("Invalid to and from data: TO BEFORE FROM");
        }

        return missions != null && !missions.isEmpty();
    }

    private static Map<String, String> getLocationWithMostSuccessfulMissionsPerCompanyImpl(List<Mission> missions,
                                                                                   LocalDate from, LocalDate to) {
        Map<String, Map<String, Long>> companyLocationsCount = missions
            .stream()
            .filter(Objects::nonNull)
            .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS))
            .filter(mission ->
                (mission.date().isEqual(from) || mission.date().isAfter(from)) &&
                    (mission.date().isEqual(to) || mission.date().isBefore(to))
            )
            .collect(Collectors.groupingBy(Mission::company,
                Collectors.groupingBy(Mission::location, Collectors.counting())));

        // reusing the method used for getMostDesiredLocationForMissionsPerCompany
        return getCompanyWithLocationThatHasMostOccurrences(companyLocationsCount);
    }

}
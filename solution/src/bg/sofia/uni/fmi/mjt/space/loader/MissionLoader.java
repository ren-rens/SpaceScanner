package bg.sofia.uni.fmi.mjt.space.loader;

import bg.sofia.uni.fmi.mjt.space.loader.constants.MissionIndex;
import bg.sofia.uni.fmi.mjt.space.mission.Detail;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.io.BufferedReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MissionLoader implements Loader<Mission> {

    @Override
    public List<Mission> load(Reader reader)  {
        List<Mission> result = new ArrayList<>();

        try (var bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine(); // skip the header

            while ((line = bufferedReader.readLine()) != null) {

                Mission mission = parseMission(line);
                if (mission == null) {
                    continue;
                }

                result.add(mission);
            }

        } catch (Exception e) {
            return result;
        }

        return result;
    }

    private static Mission parseMission(String line) {
        String[] parts = Loader.parseCSVLine(line, DELIMITER, DELIMITER);
        if (parts.length < MIN_SPLIT_SIZE) {
            return null;
        }

        try {
            String id = parts[MissionIndex.ID.getIndex()];
            String company = parts[MissionIndex.COMPANY.getIndex()];
            String location = parts[MissionIndex.LOCATION.getIndex()];
            LocalDate date = parseDate(parts[MissionIndex.DATE.getIndex()]);
            Detail detail = parseDetail(parts[MissionIndex.DETAIL.getIndex()]);
            RocketStatus rocketStatus = parseRocketStatus(parts[MissionIndex.ROCKET_STATUS.getIndex()]);
            Optional<Double> cost = parseCost(parts[MissionIndex.COST.getIndex()]);
            MissionStatus missionStatus = parseMissionStatus(parts[MissionIndex.MISSION_STATUS.getIndex()]);

            if (!validateMission(id, company, location, date, detail, rocketStatus, missionStatus)) {
                return null;
            }

            return new Mission(id, company, location, date, detail, rocketStatus, cost, missionStatus);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDate(String dateStr) {
        if (dateStr.isBlank()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd, yyyy", Locale.ENGLISH);
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return null;
        }

    }

    private static Detail parseDetail(String detailStr) {
        if (detailStr.isBlank()) {
            return null;
        }

        // <rocketName>|<payload>
        String[] split = detailStr.split("\\|");
        if (split.length != DETAIL_SPLIT_SIZE
            || split[DETAIL_ROCKET_NAME_IDX].isBlank() || split[DETAIL_PAYLOAD_IDX].isBlank()) {
            return null;
        }

        return new Detail(split[0].trim(), split[1].trim());
    }

    private static RocketStatus parseRocketStatus(String rocketStatusStr) {
        return rocketStatusStr.isBlank() ? null : RocketStatus.fromString(rocketStatusStr);
    }

    private static Optional<Double> parseCost(String costStr) {
        return costStr.isBlank() ?
            Optional.empty() : Optional.of(Double.valueOf(costStr.trim()));
    }

    private static MissionStatus parseMissionStatus(String missionStatusStr) {
        return missionStatusStr.isBlank() ? null : MissionStatus.fromString(missionStatusStr);
    }

    private static boolean validateMission(String id, String company, String location,
                                           LocalDate localDate, Detail detail,
                                           RocketStatus rocketStatus, MissionStatus missionStatus) {
        return !id.isBlank() && !company.isBlank() && !location.isBlank()
            && localDate != null && detail != null && rocketStatus != null && missionStatus != null;
    }

    private static final int MIN_SPLIT_SIZE = 7;
    private static final int DETAIL_ROCKET_NAME_IDX = 0;
    private static final int DETAIL_PAYLOAD_IDX = 1;
    private static final int DETAIL_SPLIT_SIZE = 2;

    private static final char DELIMITER = ',';
}
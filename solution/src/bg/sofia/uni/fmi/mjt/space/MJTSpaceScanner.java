package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.loader.Loader;
import bg.sofia.uni.fmi.mjt.space.loader.MissionLoader;
import bg.sofia.uni.fmi.mjt.space.loader.RocketLoader;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.analyzers.MissionAnalyzer;
import bg.sofia.uni.fmi.mjt.space.analyzers.RocketAnalyzer;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.OutputStream;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MJTSpaceScanner implements SpaceScannerAPI {

    MJTSpaceScanner(List<Mission> missions, List<Rocket> rockets) {
        this.missions = missions;
        this.rockets = rockets;
        this.cipher = null;
    }

    MJTSpaceScanner(List<Mission> missions, List<Rocket> rockets, SymmetricBlockCipher cipher) {
        this.missions = missions;
        this.rockets = rockets;
        this.cipher = cipher;
    }

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        this.cipher = new Rijndael(secretKey);
        this.missions = loadMissions(missionsReader);
        this.rockets = loadRockets(rocketsReader);
    }

    // loading missions
    private List<Mission> loadMissions(Reader reader) {
        Loader<Mission> loader = new MissionLoader();
        return loader.load(reader);
    }

    private List<Rocket> loadRockets(Reader reader) {
        Loader<Rocket> loader = new RocketLoader();
        return loader.load(reader);
    }

    // mission methods
    @Override
    public Collection<Mission> getAllMissions() {
        return MissionAnalyzer.getAllMissions(this.missions);
    }

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        return MissionAnalyzer.getAllMissions(this.missions, missionStatus);
    }

    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        return MissionAnalyzer.getCompanyWithMostSuccessfulMissions(this.missions, from, to);
    }

    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        return MissionAnalyzer.getMissionsPerCountry(this.missions);
    }

    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        return MissionAnalyzer.getTopNLeastExpensiveMissions(this.missions, n, missionStatus, rocketStatus);
    }

    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        return MissionAnalyzer.getMostDesiredLocationForMissionsPerCompany(this.missions);
    }

    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        return MissionAnalyzer.getLocationWithMostSuccessfulMissionsPerCompany(this.missions, from, to);
    }

    // rocket methods
    @Override
    public Collection<Rocket> getAllRockets() {
        return RocketAnalyzer.getAllRockets(this.rockets);
    }

    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        return RocketAnalyzer.getTopNTallestRockets(this.rockets, n);
    }

    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return RocketAnalyzer.getWikiPageForRocket(this.rockets);
    }

    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        return RocketAnalyzer.getWikiPagesForRocketsUsedInMostExpensiveMissions(this.rockets, this.missions,
            n, missionStatus, rocketStatus);
    }

    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        RocketAnalyzer.saveMostReliableRocket(outputStream, from, to, this.cipher, this.missions);
    }

    private final List<Mission> missions; // to add all even duplicated missions we use List
    private final List<Rocket> rockets;
    private final SymmetricBlockCipher cipher;

}

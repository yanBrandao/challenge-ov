package com.ovinto.member_calculator.application.service;

import com.ovinto.member_calculator.adapter.client.OvintoEmployeeAPI;
import com.ovinto.member_calculator.adapter.client.response.OvintoCrewResponse;
import com.ovinto.member_calculator.adapter.mapper.EmployeeMapper;
import com.ovinto.member_calculator.application.domain.Employee;
import com.ovinto.member_calculator.application.port.in.CalculateCrewMemberUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmployeeService implements CalculateCrewMemberUseCase {

    private final OvintoEmployeeAPI ovintoEmployeeAPI;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Controls concurrency
    /**
     * Ovinto HQ
     * Brug Zuid 29, 9880 Aalter, Belgique
     * 51,105766, 3,43172
     * https://www.bing.com/search?q=Brug+Zuid+29%2C+9880+Aalter%2C+Belgium&cvid=3932bef304eb43d08e992d4d56b03a10&gs_lcrp=EgRlZGdlKgYIABBFGDkyBggAEEUYOTIGCAEQABhA0gEHMjExajBqNKgCCLACAQ&FORM=ANAB01&PC=U531
     *
     */
    private static final double HQ_LATITUDE = 51.105766;  // Ovinto HQ Latitude
    private static final double HQ_LONGITUDE = 3.43172;  // Ovinto HQ Longitude
    private static final double MAX_DISTANCE_KM = 30.0;


    public EmployeeService(OvintoEmployeeAPI ovintoEmployeeAPI) {
        this.ovintoEmployeeAPI = ovintoEmployeeAPI;
    }

    Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Override
    public void execute(List<Employee> members) {

        List<CompletableFuture<OvintoCrewResponse>> threads = loadEmployeesCoordinates(members);

        List<OvintoCrewResponse> crewResponses = joinAllResponses(threads);

        List<Employee> membersWithCoordinates = mapEmployeesCoordinates(members, crewResponses);

        log.info("Received {} crew members' data.", membersWithCoordinates.size());

        List<Employee> nearbyMembers = calculateNearbyMembers(membersWithCoordinates);

        log.info("Members within 30km of HQ: {}", nearbyMembers.size());

        nearbyMembers.forEach(member -> log.info("Nearby Member (within 30km): {}", member));

        Employee furthestMember = findFurthestMember(membersWithCoordinates);

        if (furthestMember != null) {
            double maxDistance = calculateDistance(
                    HQ_LATITUDE,
                    HQ_LONGITUDE,
                    furthestMember.coordinates().latitude().doubleValue(),
                    furthestMember.coordinates().longitude().doubleValue()
            );
            log.info("Furthest crew member: {} ({} km away)", furthestMember, String.format("%.2f", maxDistance));
        } else {
            log.info("No crew members found.");
        }
    }

    Employee findFurthestMember(List<Employee> membersWithCoordinates) {
        return membersWithCoordinates.stream()
                .max(Comparator.comparingDouble(member -> calculateDistance(
                        HQ_LATITUDE,
                        HQ_LONGITUDE,
                        member.coordinates().latitude().doubleValue(),
                        member.coordinates().longitude().doubleValue()
                )))
                .orElse(null);
    }

    List<Employee> calculateNearbyMembers(List<Employee> employees) {
        return employees.stream()
                .filter(member -> calculateDistance(
                        HQ_LATITUDE,
                        HQ_LONGITUDE,
                        member.coordinates().latitude().doubleValue(),
                        member.coordinates().longitude().doubleValue()
                ) <= MAX_DISTANCE_KM)
                .toList();
    }

    List<OvintoCrewResponse> joinAllResponses(List<CompletableFuture<OvintoCrewResponse>> threads) {
        return threads.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    List<CompletableFuture<OvintoCrewResponse>> loadEmployeesCoordinates(List<Employee> members) {
        return members.stream()
                .map(member -> CompletableFuture.supplyAsync(() -> {
                    log.info("Fetching data for member ID: {}", member.id());
                    return ovintoEmployeeAPI.getCrewById(member.id());
                }, executorService))
                .toList();
    }

    List<Employee> mapEmployeesCoordinates(List<Employee> members, List<OvintoCrewResponse> crewResponses) {
        return members.stream().map(member -> {
            var crewEmployee = crewResponses.stream().filter(
                    response -> response.id().equals(member.id())).findFirst();
            if (crewEmployee.isPresent()) {
                var employee = EmployeeMapper.mapToEmployee(member, crewEmployee.get());
                log.info("Employee: {}", employee);
                return employee;
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    /*
    *   Haversine formula to calculate distance between two coordinates.
    *   https://www.baeldung.com/java-find-distance-between-points
    * */
    double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        double EARTH_RADIUS = 6371;
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        return EARTH_RADIUS * c;
    }
}

# three

For this task, I decided to create an application using Spring Boot to optimize my productivity by leveraging libraries that simplify building the algorithm.
- FeignClient is used to encapsulate the call to the Ovinto API.
- Spring Web is used for the controller, making it easier to create input for the program.
In addition, I used the MVC architecture, keeping the main logic of the program in the service layer.

All code execution is based on the following steps:

1. Create a list of calls to load the positions of all employees.
2. Wait for all threads to return their responses.
3. Map the API responses to my domain class Employee to preserve the proper names.
4. Calculate the nearest employee first by using the Haversine formula to compute the distance between two points on Earth.
5. Identify which member is the furthest away.
6. Calculate that distance to display the result.

Below is the main method: [EmployeeService](https://github.com/yanBrandao/challenge-ov/blob/main/questions/3_three/member_calculator/src/main/java/com/ovinto/member_calculator/application/service/EmployeeService.java)

```java
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
```

To call the API, simply use the following cURL command:
```bash
curl --request GET \
  --url http://localhost:8080/members \
  --header 'Content-Type: application/xml' \
  --header 'User-Agent: insomnia/10.3.0' \
  --data 'PD94bWwgdmVyc2lvbj0iMS4wIj8+CjxDb21wYW55IG5hbWU9J092aW50byc+CiAgPEVtcGxveWVlIGlkPScxJz4KICAgICAgPEZpcnN0TmFtZT5Kb3JpczwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+Qm9zY2htYW5zPC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmpvcmlzLmJvc2NobWFuc0BvdmludG8uY29tPC9FbWFpbD4KICA8L0VtcGxveWVlPgogIDxFbXBsb3llZSBpZD0nMic+CiAgICAgIDxGaXJzdE5hbWU+SmFzcGVyPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5NYWVzPC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmphc3Blci5tYWVzQG92aW50by5jb208L0VtYWlsPgogIDwvRW1wbG95ZWU+CiAgPEVtcGxveWVlIGlkPSczJz4KICAgICAgPEZpcnN0TmFtZT5MZWFuZGVyPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5Ib2VkdDwvTGFzdE5hbWU+CiAgICAgIDxFbWFpbD5sZWFuZGVyLmhvZWR0QG92aW50by5jb208L0VtYWlsPgogIDwvRW1wbG95ZWU+CiAgPEVtcGxveWVlIGlkPSc0Jz4KICAgICAgPEZpcnN0TmFtZT5TYWx2YXRvcmU8L0ZpcnN0TmFtZT4KICAgICAgPExhc3ROYW1lPkNhc3RlbGxhbm88L0xhc3ROYW1lPgogICAgICA8RW1haWw+c2FsdmF0b3JlLmNhc3RlbGxhbm9Ab3ZpbnRvLmNvbTwvRW1haWw+CiAgPC9FbXBsb3llZT4KICA8RW1wbG95ZWUgaWQ9JzUnPgogICAgICA8Rmlyc3ROYW1lPkh1eWVuPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5OZ3V5ZW4gVGh1PC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmxpbmguY2FzdGVsbGFub0BvdmludG8uY29tPC9FbWFpbD4KICA8L0VtcGxveWVlPgogIDxFbXBsb3llZSBpZD0nNic+CiAgICAgIDxGaXJzdE5hbWU+RnJlZGVyaWNrPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5Sb25zZTwvTGFzdE5hbWU+CiAgICAgIDxFbWFpbD5mcmVkZXJpY2sucm9uc2VAb3ZpbnRvLmNvbTwvRW1haWw+CiAgPC9FbXBsb3llZT4KICA8RW1wbG95ZWUgaWQ9JzcnPgogICAgICA8Rmlyc3ROYW1lPkFtZWxpZTwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+RGUgQ2xlcmNxLVZhbiBIZWNrZTwvTGFzdE5hbWU+CiAgICAgIDxFbWFpbD5hbWVsaWUuZGN2aEBvdmludG8uY29tPC9FbWFpbD4KICA8L0VtcGxveWVlPgogIDxFbXBsb3llZSBpZD0nOCc+CiAgICAgIDxGaXJzdE5hbWU+RmVsaXg8L0ZpcnN0TmFtZT4KICAgICAgPExhc3ROYW1lPkRlbHZhPC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmZlbGl4LmRlbHZhQG92aW50by5jb208L0VtYWlsPgogIDwvRW1wbG95ZWU+CiAgPEVtcGxveWVlIGlkPSc5Jz4KICAgICAgPEZpcnN0TmFtZT5Kb3JpczwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+Q2xhZXM8L0xhc3ROYW1lPgogICAgICA8RW1haWw+am9yaXMuY2xhZXNAb3ZpbnRvLmNvbTwvRW1haWw+CiAgPC9FbXBsb3llZT4KICA8RW1wbG95ZWUgaWQ9JzEwJz4KICAgICAgPEZpcnN0TmFtZT5MdWlnaTwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+Q2xhZXlzPC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmx1aWdpLmNsYWV5c0BvdmludG8uY29tPC9FbWFpbD4KICA8L0VtcGxveWVlPgogIDxFbXBsb3llZSBpZD0nMTEnPgogICAgICA8Rmlyc3ROYW1lPkVsbGVuPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5Cb2dhZXJ0PC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmVsbGVuLmJvZ2FlcnRAb3ZpbnRvLmNvbTwvRW1haWw+CiAgPC9FbXBsb3llZT4KICA8RW1wbG95ZWUgaWQ9JzEyJz4KICAgICAgPEZpcnN0TmFtZT5FbGxlbjwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+R29lbWFlcmU8L0xhc3ROYW1lPgogICAgICA8RW1haWw+ZWxsZW4uZ29lbWFlcmVAb3ZpbnRvLmNvbTwvRW1haWw+CiAgPC9FbXBsb3llZT4KICA8RW1wbG95ZWUgaWQ9JzEzJz4KICAgICAgPEZpcnN0TmFtZT5QYXRyaWNrPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5XdXl0czwvTGFzdE5hbWU+CiAgICAgIDxFbWFpbD5wYXRyaWNrLnd1eXRzQG92aW50by5jb208L0VtYWlsPgogIDwvRW1wbG95ZWU+CiAgPEVtcGxveWVlIGlkPScxNCc+CiAgICAgIDxGaXJzdE5hbWU+QW5uYTwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+UGxvbmthPC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmFubmEucGxvbmthQG92aW50by5jb208L0VtYWlsPgogIDwvRW1wbG95ZWU+CiAgPEVtcGxveWVlIGlkPScxNSc+CiAgICAgIDxGaXJzdE5hbWU+S2VyZW5zYTwvRmlyc3ROYW1lPgogICAgICA8TGFzdE5hbWU+dGliZXJnaGllbjwvTGFzdE5hbWU+CiAgICAgIDxFbWFpbD5rZXJlbnNhLnRpYmVyZ2hpZW5Ab3ZpbnRvLmNvbTwvRW1haWw+CiAgPC9FbXBsb3llZT4KICA8RW1wbG95ZWUgaWQ9JzE2Jz4KICAgICAgPEZpcnN0TmFtZT5IYW5zPC9GaXJzdE5hbWU+CiAgICAgIDxMYXN0TmFtZT5kZSBXaXRoPC9MYXN0TmFtZT4KICAgICAgPEVtYWlsPmhhbnMuZGUud2l0aEBvdmludG8uY29tPC9FbWFpbD4KICA8L0VtcGxveWVlPgo8L0NvbXBhbnk+Cg=='
```
Example of the final output:

```
Received 16 crew members' data.
Members within 30km of HQ: 8
Nearby Member (within 30km): Employee[id=2, firstName=Jasper, lastName=Maes, email=jasper.maes@ovinto.com, coordinates=Coordinates[latitude=51.040707, longitude=3.719554]]
Nearby Member (within 30km): Employee[id=3, firstName=Leander, lastName=Hoedt, email=leander.hoedt@ovinto.com, coordinates=Coordinates[latitude=50.97675, longitude=3.201923]]
Nearby Member (within 30km): Employee[id=6, firstName=Frederick, lastName=Ronse, email=frederick.ronse@ovinto.com, coordinates=Coordinates[latitude=51.31707, longitude=3.306657]]
Nearby Member (within 30km): Employee[id=7, firstName=Amelie, lastName=De Clercq-Van Hecke, email=amelie.dcvh@ovinto.com, coordinates=Coordinates[latitude=51.317066, longitude=3.306658]]
Nearby Member (within 30km): Employee[id=10, firstName=Luigi, lastName=Claeys, email=luigi.claeys@ovinto.com, coordinates=Coordinates[latitude=51.06224, longitude=3.717885]]
Nearby Member (within 30km): Employee[id=12, firstName=Ellen, lastName=Goemaere, email=ellen.goemaere@ovinto.com, coordinates=Coordinates[latitude=51.069187, longitude=3.459673]]
Nearby Member (within 30km): Employee[id=13, firstName=Patrick, lastName=Wuyts, email=patrick.wuyts@ovinto.com, coordinates=Coordinates[latitude=51.0627, longitude=3.572774]]
Nearby Member (within 30km): Employee[id=15, firstName=Kerensa, lastName=tiberghien, email=kerensa.tiberghien@ovinto.com, coordinates=Coordinates[latitude=50.959705, longitude=3.595819]]
Furthest crew member: Employee[id=11, firstName=Ellen, lastName=Bogaert, email=ellen.bogaert@ovinto.com, coordinates=Coordinates[latitude=51.089844, longitude=4.498138]] (74,49 km away)
```

The total processing time for a request to retrieve all members’ data and identify the furthest one is **11 seconds**.
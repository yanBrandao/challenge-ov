package com.ovinto.member_calculator.adapter.web;

import com.ovinto.member_calculator.adapter.mapper.CompanyMapper;
import com.ovinto.member_calculator.adapter.web.request.CompanyRequest;
import com.ovinto.member_calculator.application.port.in.CalculateCrewMemberUseCase;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;

@RestController
public class MembersController {

    private final CalculateCrewMemberUseCase calculateCrewMemberUseCase;

    public MembersController(CalculateCrewMemberUseCase calculateCrewMemberUseCase) {
        this.calculateCrewMemberUseCase = calculateCrewMemberUseCase;
    }

    @GetMapping(consumes = "application/xml", path = "/members")
    public void createGeofence(@RequestBody String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CompanyRequest.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        CompanyRequest request = (CompanyRequest) unmarshaller.unmarshal(new StringReader(xml));
        calculateCrewMemberUseCase.execute(CompanyMapper.mapToEmployees(request));
    }
}
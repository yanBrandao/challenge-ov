package com.ovinto.member_calculator.adapter.client;

import com.ovinto.member_calculator.adapter.client.response.OvintoCrewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ovinto-employee-api", url = "https://client-dev.ovinto.com/api/v1/ovintocrew")
public interface OvintoEmployeeAPI {
    @GetMapping("/{id}")
    OvintoCrewResponse getCrewById(@PathVariable("id") String id);
}

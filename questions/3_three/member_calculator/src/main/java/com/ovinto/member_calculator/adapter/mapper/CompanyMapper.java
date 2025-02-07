package com.ovinto.member_calculator.adapter.mapper;

import com.ovinto.member_calculator.adapter.web.request.CompanyRequest;
import com.ovinto.member_calculator.application.domain.Employee;

import java.util.List;

public class CompanyMapper {

    public static List<Employee> mapToEmployees(CompanyRequest request) {
        return request.getEmployees().stream().map(EmployeeMapper::map).toList();
    }
}

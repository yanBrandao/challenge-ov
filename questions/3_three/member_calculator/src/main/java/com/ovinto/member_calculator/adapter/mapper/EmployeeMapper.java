package com.ovinto.member_calculator.adapter.mapper;

import com.ovinto.member_calculator.adapter.client.response.OvintoCrewResponse;
import com.ovinto.member_calculator.adapter.web.request.EmployeeRequest;
import com.ovinto.member_calculator.application.domain.Employee;

import java.math.BigDecimal;

public class EmployeeMapper {
    public static Employee map(EmployeeRequest request) {
        return new Employee(
                request.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                new Employee.Coordinates(BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }

    public static Employee mapToEmployee(Employee employee, OvintoCrewResponse response) {
     return new Employee(employee.id(), employee.firstName(), employee.lastName(), employee.email(),
             new Employee.Coordinates(
                     response.coordinates().latitude(),
                     response.coordinates().longitude()
             ));
    }
}
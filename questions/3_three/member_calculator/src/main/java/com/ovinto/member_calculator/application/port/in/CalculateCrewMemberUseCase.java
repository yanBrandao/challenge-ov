package com.ovinto.member_calculator.application.port.in;

import com.ovinto.member_calculator.application.domain.Employee;

import java.util.List;

public interface CalculateCrewMemberUseCase {
    public void execute(List<Employee> members);
}

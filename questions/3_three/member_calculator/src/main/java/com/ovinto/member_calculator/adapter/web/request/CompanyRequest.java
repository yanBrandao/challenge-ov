package com.ovinto.member_calculator.adapter.web.request;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "Company")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompanyRequest {
    @XmlAttribute
    private String name;

    @XmlElement(name = "Employee")
    private List<EmployeeRequest> employees;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EmployeeRequest> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeRequest> employees) {
        this.employees = employees;
    }

    @Override
    public String toString() {
        return "Company{name='" + name + "', employees=" + employees + "}";
    }
}

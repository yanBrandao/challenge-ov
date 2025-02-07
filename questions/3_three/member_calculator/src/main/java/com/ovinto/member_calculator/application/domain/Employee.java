package com.ovinto.member_calculator.application.domain;

import java.math.BigDecimal;

public record Employee(String id, String firstName, String lastName, String email, Coordinates coordinates) {
    public record Coordinates(BigDecimal latitude, BigDecimal longitude) {}
}
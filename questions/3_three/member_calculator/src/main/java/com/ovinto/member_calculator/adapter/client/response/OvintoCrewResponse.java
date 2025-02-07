package com.ovinto.member_calculator.adapter.client.response;

import java.math.BigDecimal;

public record OvintoCrewResponse(
    String id,
    CoordinatesResponse coordinates
) {
    public record CoordinatesResponse(BigDecimal latitude, BigDecimal longitude) {}
}

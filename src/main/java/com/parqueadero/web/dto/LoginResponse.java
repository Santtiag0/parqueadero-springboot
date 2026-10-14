package com.parqueadero.web.dto;

public record LoginResponse(
        String token,
        String username
) {
}

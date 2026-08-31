package com.hlima.erp.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {
}

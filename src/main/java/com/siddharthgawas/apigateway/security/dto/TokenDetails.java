package com.siddharthgawas.apigateway.security.dto;

public record TokenDetails(String accessToken, String refreshToken) {
}
package com.siddharthgawas.apigateway.service;

import com.siddharthgawas.apigateway.security.dto.TokenDetails;

public interface AuthenticationService {

    TokenDetails authenticate(String username, String password);

    TokenDetails refreshAccessToken(String refreshToken);
}

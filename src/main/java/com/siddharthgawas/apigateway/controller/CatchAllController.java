package com.siddharthgawas.apigateway.controller;

import com.siddharthgawas.apigateway.dto.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle all unmatched requests.
 * <p>
 * This controller serves as a catch-all for any requests that do not match
 * other defined endpoints, providing a simple response indicating successful
 * reach to the API Gateway.
 */
@RestController
public class CatchAllController {

    /**
     * Handles all requests that do not match any other endpoints.
     * <p>
     * This method returns a simple response indicating that the API Gateway
     * has been successfully reached.
     *
     * @param request the HttpServletRequest object containing request details
     * @return a ResponseEntity with a success message
     */
    @RequestMapping("/**")
    public ResponseEntity<?> handleAll(final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse("Successfully reached the API Gateway"));
    }
}

package com.siddharthgawas.apigateway.controller;

import com.siddharthgawas.apigateway.dto.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatchAllController {

    @RequestMapping("/**")
    public ResponseEntity<?> handleAll(final HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse("Successfully reached the API Gateway"));
    }
}

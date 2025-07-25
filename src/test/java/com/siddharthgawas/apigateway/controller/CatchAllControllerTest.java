package com.siddharthgawas.apigateway.controller;

import com.siddharthgawas.apigateway.dto.APIResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CatchAllControllerTest {

    @Test
    void handleAll_shouldReturnSuccessResponse() {
        CatchAllController controller = new CatchAllController();
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<?> response = controller.handleAll(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(APIResponse.class);
        APIResponse apiResponse = (APIResponse) response.getBody();
        Assertions.assertNotNull(apiResponse);
        assertThat(apiResponse.message()).isEqualTo("Successfully reached the API Gateway");
    }
}


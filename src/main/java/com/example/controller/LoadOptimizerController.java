package com.example.controller;

import com.example.dto.OptimizeRequest;
import com.example.dto.OptimizeResponse;
import com.example.service.LoadOptimizerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/load-optimizer")
public class LoadOptimizerController {

    private static final int MAX_ORDERS = 22;

    private final LoadOptimizerService service;

    public LoadOptimizerController(LoadOptimizerService service) {
        this.service = service;
    }

    @PostMapping("/optimize")
    public ResponseEntity<?> optimize(@RequestBody @Valid OptimizeRequest request) {
        if (request.getOrders() != null && request.getOrders().size() > MAX_ORDERS) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("status", 413, "error",
                            "orders list exceeds maximum of " + MAX_ORDERS));
        }

        OptimizeResponse response = service.optimize(request);
        return ResponseEntity.ok(response);
    }
}

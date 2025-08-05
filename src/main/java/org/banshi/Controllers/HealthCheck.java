package org.banshi.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UP");
    }

    @GetMapping("/")
    public ResponseEntity<String> healthCheck2() {
        return ResponseEntity.ok("UP2");
    }

}

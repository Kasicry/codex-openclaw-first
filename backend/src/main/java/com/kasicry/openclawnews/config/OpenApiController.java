package com.kasicry.openclawnews.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {

    private static final MediaType YAML = MediaType.parseMediaType("application/yaml");

    @GetMapping("/openapi.yaml")
    public ResponseEntity<Resource> openApi() {
        return ResponseEntity
                .ok()
                .contentType(YAML)
                .body(new ClassPathResource("static/openapi.yaml"));
    }
}

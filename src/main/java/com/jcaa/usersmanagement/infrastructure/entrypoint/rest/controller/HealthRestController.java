package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Tag(name = "Health", description = "Verificación de estado y disponibilidad del servicio")
@RestController
public class HealthRestController {

  private static final String STATUS_KEY = "status";
  private static final String STATUS_UP = "UP";
  private static final String SWAGGER_PATH = "/swagger-ui.html";

  @Operation(summary = "Verificar estado del servicio (Health Check)")
  @GetMapping("/api/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of(STATUS_KEY, STATUS_UP));
  }

  @Operation(hidden = true)
  @GetMapping("/")
  public RedirectView redirectToSwagger() {
    return new RedirectView(SWAGGER_PATH);
  }
}

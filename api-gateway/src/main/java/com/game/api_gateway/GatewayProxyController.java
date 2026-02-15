package com.game.api_gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RestController
public class GatewayProxyController {

    private final RestClient restClient;
    private final String userService;
    private final String notificationService;
    private final String groupService;
    private final String gameService;
    private final String logicService;

    public GatewayProxyController(
            @Value("${app.targets.user-service}") String userService,
            @Value("${app.targets.notification-service}") String notificationService,
            @Value("${app.targets.group-service}") String groupService,
            @Value("${app.targets.game-service}") String gameService,
            @Value("${app.targets.logic-service}") String logicService) {
        this.restClient = RestClient.builder().build();
        this.userService = userService;
        this.notificationService = notificationService;
        this.groupService = groupService;
        this.gameService = gameService;
        this.logicService = logicService;
    }

    @RequestMapping({
            "/api/auth/**",
            "/api/users/**",
            "/api/notifications/**",
            "/api/groups/**",
            "/api/games/**",
            "/api/logic/**",
            "/.well-known/jwks.json"
    })
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String targetBase = resolveTarget(path);
        if (targetBase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found".getBytes());
        }

        String query = request.getQueryString();
        String targetUri = targetBase + path + (query == null ? "" : "?" + query);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        RestClient.RequestBodySpec requestSpec = restClient.method(method).uri(targetUri);

        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!HttpHeaders.HOST.equalsIgnoreCase(name) && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                var values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    requestSpec.header(name, values.nextElement());
                }
            }
        }

        ResponseEntity<byte[]> downstream;
        try {
            downstream = ((body == null || body.length == 0)
                    ? requestSpec.retrieve().toEntity(byte[].class)
                    : requestSpec.body(body).retrieve().toEntity(byte[].class));
        } catch (RestClientResponseException ex) {
            HttpHeaders errorHeaders = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                ex.getResponseHeaders().forEach((k, v) -> {
                    if (!HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(k)) {
                        errorHeaders.put(k, v);
                    }
                });
            }
            return ResponseEntity.status(ex.getStatusCode()).headers(errorHeaders).body(ex.getResponseBodyAsByteArray());
        }

        HttpHeaders headers = new HttpHeaders();
        downstream.getHeaders().forEach((k, v) -> {
            if (!HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(k)) {
                headers.put(k, v);
            }
        });

        return ResponseEntity.status(downstream.getStatusCode()).headers(headers).body(downstream.getBody());
    }

    private String resolveTarget(String path) {
        if (path.startsWith("/api/auth/") || path.startsWith("/api/users/") || path.equals("/.well-known/jwks.json")) {
            return userService;
        }
        if (path.startsWith("/api/notifications/")) {
            return notificationService;
        }
        if (path.startsWith("/api/groups/")) {
            return groupService;
        }
        if (path.startsWith("/api/games/")) {
            return gameService;
        }
        if (path.startsWith("/api/logic/")) {
            return logicService;
        }
        return null;
    }
}

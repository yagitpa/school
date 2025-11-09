package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/info")
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final Environment environment;

    @Value("${server.port}")
    private String serverPort;

    public InfoController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/port")
    public String getPort() {
        logger.info("Was invoked method for get server port");

        try {
            String activeProfile = Arrays.toString(environment.getActiveProfiles());
            logger.debug("Current server port: {}, active profile: {}", serverPort, activeProfile);

            return String.format("Port: %s | Profile: %s", serverPort, activeProfile);
        } catch (Exception e) {
            logger.error("Error in getPort method", e);
            return "Port: " + serverPort;
        }
    }
}
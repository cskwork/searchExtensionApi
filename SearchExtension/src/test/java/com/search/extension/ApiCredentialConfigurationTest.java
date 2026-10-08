package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

class ApiCredentialConfigurationTest {
    private StandardEnvironment configuration() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            properties.load(input);
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().addLast(new PropertiesPropertySource("application", properties));
        return environment;
    }

    @Test
    void requiresExternalCredentials() throws IOException {
        StandardEnvironment environment = configuration();
        for (String property : new String[] { "kakao.api.key", "naver.api.client-id", "naver.api.client-secret" }) {
            assertThrows(IllegalArgumentException.class, () -> environment.getRequiredProperty(property));
        }
    }

    @Test
    void resolvesExternalCredentials() throws IOException {
        StandardEnvironment environment = configuration();
        environment.getPropertySources().addFirst(new MapPropertySource("test-credentials", java.util.Map.of(
            "KAKAO_API_KEY", "offline-kakao-fixture",
            "NAVER_CLIENT_ID", "offline-naver-id",
            "NAVER_CLIENT_SECRET", "offline-naver-fixture"
        )));
        assertEquals("offline-kakao-fixture", environment.getRequiredProperty("kakao.api.key"));
        assertEquals("offline-naver-id", environment.getRequiredProperty("naver.api.client-id"));
        assertEquals("offline-naver-fixture", environment.getRequiredProperty("naver.api.client-secret"));
    }
}

package io.hohichh.marketplace.order.integration;

import io.hohichh.marketplace.order.integration.config.TestClockConfiguration;
import io.hohichh.marketplace.order.integration.config.TestContainerConfiguration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.lenient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({
        TestClockConfiguration.class,
        TestContainerConfiguration.class
})
public abstract class AbstractApplicationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected Clock clock;

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @BeforeEach
    void setupClock() {
        if (org.mockito.Mockito.mockingDetails(clock).isMock()) {
            Instant fixedTime = Instant.parse("2025-01-01T12:00:00Z");
            lenient().when(clock.instant()).thenReturn(fixedTime);
            lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        }
    }

    protected String generateToken(UUID userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));

        Instant now = clock.instant();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60)))
                .signWith(key)
                .compact();
    }

    protected HttpEntity<Void> getAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    protected <T> HttpEntity<T> getAuthHeaders(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    static class TestPage<T> {
        public List<T> content;
        public long totalElements;
        public int totalPages;
        public int size;
        public int number;

        public List<T> getContent() { return content; }
        public long getTotalElements() { return totalElements; }
    }
}

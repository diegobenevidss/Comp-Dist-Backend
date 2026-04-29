package com.healthsys.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "jwt.secret=test-secret-key-for-testing-purposes-only-32ch"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}

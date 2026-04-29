package com.healthsys.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false",
    "spring.rabbitmq.host=localhost",
    "jwt.secret=test-secret-key-for-testing-purposes-only-32ch"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

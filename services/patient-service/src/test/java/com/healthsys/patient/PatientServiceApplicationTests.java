package com.healthsys.patient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=none",
    "spring.data.redis.host=localhost"
})
class PatientServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

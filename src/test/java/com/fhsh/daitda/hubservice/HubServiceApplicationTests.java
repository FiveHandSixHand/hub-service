package com.fhsh.daitda.hubservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.config.name=application-test",
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.import-check.enabled=false",
                "eureka.client.enabled=false"
        }
)
class HubServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}

package com.sixgroup.refit.observability.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
)
@ActiveProfiles("test")
@DirtiesContext
public class CucumberSpringConfig {
    @MockBean
    @org.springframework.beans.factory.annotation.Qualifier("impalaJdbcTemplate")
    private JdbcTemplate impalaJdbcTemplate;
}

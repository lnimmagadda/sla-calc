package com.insite360.sitedatalatencycalculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LatencyControllerTest {


    @LocalServerPort
    private int port;


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    LatencyController controller;

    @Test
    public void controllerShouldReturnSites() throws Exception {
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/sla-calculator-service/sites",
                String.class);

        assertThat(response).contains("delay");
    }

}

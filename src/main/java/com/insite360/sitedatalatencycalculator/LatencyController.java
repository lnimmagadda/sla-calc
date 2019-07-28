package com.insite360.sitedatalatencycalculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Duration;
import java.util.stream.Collectors;

@RestController
@Configuration
@EnableSwagger2
public class LatencyController {


    private final LatencyService latencyService;


    @Autowired
    public LatencyController(LatencyService latencyService) {
        this.latencyService = latencyService;
    }


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @RequestMapping("/sites")
    public LatencyResponse sitesWithSpecificDelay(@RequestParam(value = "delay", defaultValue = "PT5m")Duration greaterThan){

        // get the delay as a formatted delay string

        return new LatencyResponse(
                latencyService.getListofSitesWithDelays()
                        .stream().filter(s -> s.isSiteDelayed(greaterThan)).collect(Collectors.toList()), greaterThan);
    }

}

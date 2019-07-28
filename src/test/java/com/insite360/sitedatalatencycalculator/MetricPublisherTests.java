package com.insite360.sitedatalatencycalculator;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MetricPublisherTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);


    private MetricPublisher metricPublisher;

    AmazonCloudWatchAsync async = mock(AmazonCloudWatchAsync.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();


    @Test
    public void shouldProperlyPushToCloudwatch() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setActiveProfiles("dev");

        metricPublisher = new MetricPublisher(async, mockEnvironment);
        metricPublisher.pushPercentageToCloud(100.0);

        ArgumentCaptor<PutMetricDataRequest> argumentCaptor = ArgumentCaptor.forClass(PutMetricDataRequest.class);
        verify(async).putMetricData(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getNamespace()).isEqualTo("PTC_DELAY_DEV");

    }
}

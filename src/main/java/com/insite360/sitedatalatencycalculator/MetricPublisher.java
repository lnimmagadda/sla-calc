package com.insite360.sitedatalatencycalculator;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
class MetricPublisher {

    private final Environment environment;
    private final static Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);

    private final AmazonCloudWatchAsync cw;

    @Autowired
    MetricPublisher(Environment environment) {
        this.environment = environment;
        cw = AmazonCloudWatchAsyncClientBuilder.defaultClient();
    }


    MetricPublisher(AmazonCloudWatchAsync cw, Environment environment) {
        this.cw = cw;
        this.environment = environment;
    }

    void pushPercentageToCloud(Double percentageOfSitesUp) {

        MetricDatum datum = new MetricDatum()
                .withMetricName("PERCENTAGE_SITES_UP")
                .withUnit(StandardUnit.Percent)
                .withValue(percentageOfSitesUp);

        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace("PTC_DELAY_" + getEnv())
                .withMetricData(datum);

        PutMetricDataResult response = cw.putMetricData(request);
    }

    private String getEnv() {
        if (environment.getActiveProfiles().length > 0) {
            return environment.getActiveProfiles()[0].toUpperCase();
        } else {
            return "UNKNOWN";
        }
    }

    void pushSiteDataToCloud(SiteEventDelay siteEventDelay) {

          /*
          pushes SiteId,Brand,delay in seconds for a site
           */

        Dimension dimension = new Dimension()
                .withName("Brand")
                .withValue(siteEventDelay.getBrand());

        MetricDatum datum = new MetricDatum()
                .withMetricName(siteEventDelay.getSiteId())
                .withUnit(StandardUnit.Seconds) // unit of the delay, seconds
                .withValue(new Long(siteEventDelay.getDelay().getSeconds()).doubleValue())
                .withDimensions(dimension);


        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace("PTC_DELAY_" + getEnv())
                .withMetricData(datum);

        PutMetricDataResult response = cw.putMetricData(request);
        LOGGER.debug(response.toString());
    }
}

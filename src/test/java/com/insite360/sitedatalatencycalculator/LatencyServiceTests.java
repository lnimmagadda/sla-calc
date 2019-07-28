package com.insite360.sitedatalatencycalculator;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LatencyServiceTests {

    @Autowired
    private LatencyService latencyService;

    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);


    private EventDAO eventDAO = mock(EventDAO.class);

    private MetricPublisher metricPublisher = mock(MetricPublisher.class);

    @Test
    public void nowInTimezoneMatchesNowUTC() {

        ZonedDateTime convertedChicago = LatencyService.convertFakeUTCToZonedDateTime("2019-06-11T10:00:00Z",
                "America/Chicago");
        ZonedDateTime convertedMoscow = LatencyService.convertFakeUTCToZonedDateTime("2019-06-11T16:00:00Z",
                "Europe/Moscow");

        assertThat(ZonedDateTime.parse("2019-06-11T15:00:00Z").toInstant()).isEqualTo(convertedChicago.toInstant());
        assertThat(ZonedDateTime.parse("2019-06-11T16:00:00Z").toInstant()).isNotEqualTo(convertedChicago.toInstant());
        assertThat(ZonedDateTime.now().toInstant()).isNotEqualTo(convertedChicago.toInstant());

        assertThat(ZonedDateTime.parse("2019-06-11T13:00:00Z").toInstant()).isEqualTo(convertedMoscow.toInstant());
    }

    @Test
    public void americaShouldBeFiveHoursLate() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(5),300000);
        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(new ArrayList<>(Arrays.asList("830023", "830209")));

        when(eventDAO.elasticSearchGetSitesAllAtOnce(Duration.ofMinutes(5))).thenReturn(Arrays.asList(
                new SiteEventDelay("Africa/Abidjan", "2019-06-26T20:53:25Z"
                        , "2019-06-26T20:53:25.518Z", "inperial oil", "830023"),
                new SiteEventDelay("America/Chicago", "2019-06-26T20:51:25Z",
                        "2019-06-26T20:51:25.443Z", "Independent", "830209")));


        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        LOGGER.debug(mostRecentDelayTimeBySiteID.toString());
        for (SiteEventDelay site : mostRecentDelayTimeBySiteID){
            LOGGER.debug("siteId is "+site.getSiteId()+" and timestamp is "+site.getTimestamp());
        }
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        LOGGER.debug(delayedSites.toString());
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(50);
    }

    @Test
    public void shouldReturnAllDownProdValues() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(5),300000);
        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(new ArrayList<>(Arrays.asList("124037", "854526")));
        when(eventDAO.elasticSearchGetSitesAllAtOnce(Duration.ofMinutes(5))).thenReturn(Arrays.asList(new SiteEventDelay("America/Detroit", "2019-06-26T17:13:30.000Z"
                        , "2019-06-26T21:26:17.510Z", "testBrand","124037"),
                new SiteEventDelay("America/New_York", "2019-06-27T17:55:30.000Z"
                        , "2019-06-27T14:42:01.345Z", "testBrand","854526")));

        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(0);
    }

    @Test
    public void sanityTestFakeValues() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(5),300000);
        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(new ArrayList<>(Arrays.asList("1", "124037")));
        when(eventDAO.elasticSearchGetSitesAllAtOnce(Duration.ofMinutes(5))).thenReturn(
                Arrays.asList(new SiteEventDelay("America/Chicago", "2019-06-26T16:40:00.000Z"
                        , "2019-06-26T21:40:01.000Z", "someBrand","1"),
                new SiteEventDelay("America/Detroit", "2019-06-26T12:00:00.000Z"
                        , "2019-06-26T16:00:00.000Z", "someOtherBrand","124037")));

        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(100);
    }

    @Test
    public void shouldReturnListOfSiteDelay() {

    }

    @Test
    public void shouldReturnZero() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(5),300000);
        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(new ArrayList<>(Arrays.asList("124037", "854526")));
        when(eventDAO.elasticSearchGetSitesAllAtOnce(Duration.ofMinutes(5))).thenReturn(
                Arrays.asList(new SiteEventDelay("America/Detroit", "2019-06-26T00:00:00.000Z"
                                , "2019-06-26T00:00:00.000Z", "dummyBrand","124037"),
                        new SiteEventDelay("America/New_York", "2019-06-27T00:00:00.000Z"
                                , "2019-06-26T00:00:00.000Z", "dummyOtherBrand","854526")));

        // should return some number or calculated metric
        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(0);
    }

    @Test
    public void shouldReturnUpProdValues() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(10),300000);
        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(new ArrayList<>(Arrays.asList("209108", "124022")));
        when(eventDAO.elasticSearchGetSitesAllAtOnce(Duration.ofMinutes(10))).thenReturn(
                Arrays.asList(new SiteEventDelay("America/Los_Angeles", "2019-06-27T07:32:28.000Z"
                                , "2019-06-27T14:33:24.023Z", "Inperial oil","209108"),
                        new SiteEventDelay("America/Detroit", "2019-06-27T10:28:00.000Z"
                                , "2019-06-27T14:35:57.734Z", "someBrand","124022")));


        // should return some number or calculated metric
        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(100);
    }

    @Test
    public void noSitesShouldReturnZero() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(5),300000);

        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(new ArrayList<>(Arrays.asList("209108", "124022")));

        when(eventDAO.getElasticSearchSiteEventDelayObject("209108"))
                .thenReturn(Optional.empty());
        when(eventDAO.getElasticSearchSiteEventDelayObject("124022"))
                .thenReturn(Optional.empty());
        // should return some number or calculated metric
        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(0);
    }

    @Test
    public void shouldTolerateEmptyInitialList() {
        latencyService = new LatencyService(eventDAO, metricPublisher,Duration.ofMinutes(5),300000);
        when(eventDAO.getSitesWithProbeSamplesWithinLastDay())
                .thenReturn(Collections.emptyList());
        when(eventDAO.getElasticSearchSiteEventDelayObject("209108"))
                .thenReturn(Optional.empty());
        when(eventDAO.getElasticSearchSiteEventDelayObject("124022"))
                .thenReturn(Optional.empty());
        // should return some number or calculated metric
        List<SiteEventDelay> mostRecentDelayTimeBySiteID = latencyService.getMostRecentDelayTimesBySiteID();
        Map<String, Duration> delayedSites = latencyService.getDelayedSites(mostRecentDelayTimeBySiteID);
        Double percentageOfSitesUp = latencyService.getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        assertThat(percentageOfSitesUp).isEqualTo(0);
    }

}

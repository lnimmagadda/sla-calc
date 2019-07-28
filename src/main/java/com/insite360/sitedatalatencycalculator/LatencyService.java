package com.insite360.sitedatalatencycalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public final class LatencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);

    private final EventDAO eventDAO;
    private final MetricPublisher metricPublisher;
    private List<SiteEventDelay> mostRecentDelayTimeBySiteID = Collections.emptyList();


    private Integer rateInMillis;

    private Duration siteDelayedAfter;


    @Autowired
    public LatencyService(EventDAO eventDAO, MetricPublisher metricPublisher, @Value("${sla.delayTime}") Duration siteDelayedAfter, @Value("${sla.rate}") Integer rateInMillis) {
        this.eventDAO = eventDAO;
        this.metricPublisher = metricPublisher;

        this.rateInMillis = rateInMillis;
        this.siteDelayedAfter = siteDelayedAfter;
    }

    @Scheduled(fixedRateString = "${sla.rate}")
    public void runApp() {
        LOGGER.info("Calculating SLA");
        // siteIDs that have probe samples in the last 5m, their assoc delay
        mostRecentDelayTimeBySiteID = getMostRecentDelayTimesBySiteID();
        LOGGER.debug("most recent delay times "+mostRecentDelayTimeBySiteID.toString());

        // map of only the sites which are delayed past some arbitrary cutoff
        Map<String, Duration> delayedSites = getDelayedSites(mostRecentDelayTimeBySiteID); // push site data to cloudwatch
        LOGGER.debug(delayedSites.toString());

        Double percentageOfSitesUp = getPercentage(mostRecentDelayTimeBySiteID, delayedSites);
        LOGGER.debug("percentageOfSitesUp of up sites is: " + percentageOfSitesUp);


        if (!mostRecentDelayTimeBySiteID.isEmpty()) {
            metricPublisher.pushPercentageToCloud(percentageOfSitesUp);
            LOGGER.info("sent {} to cloudwatch", percentageOfSitesUp);
        } else {
            LOGGER.warn("no sites returned from elastic search");
        }

    }

    Double getPercentage(List<SiteEventDelay> mostRecentDelayTimeBySiteID, Map<String, Duration> delayedSites) {
        Integer numOfSitesTotal = mostRecentDelayTimeBySiteID.size();
        LOGGER.debug("most recent delay time size " + numOfSitesTotal.toString());
        Double percentageOfSitesUp;
        // percentageOfSitesUp of sites up -- not delayed
        if (mostRecentDelayTimeBySiteID.isEmpty()) {
            percentageOfSitesUp = 0.0;
        } else {
            percentageOfSitesUp = 100 * (numOfSitesTotal - delayedSites.size()) / numOfSitesTotal.doubleValue();
        }
        return percentageOfSitesUp;
    }

    Map<String, Duration> getDelayedSites(List<SiteEventDelay> mostRecentDelayTimeBySiteID) {

        return mostRecentDelayTimeBySiteID.stream()
                .peek(metricPublisher::pushSiteDataToCloud) // upload data to cloudwatch including brand name
                .filter(s->s.isSiteDelayed(siteDelayedAfter))
                .collect(Collectors.toMap(SiteEventDelay::getSiteId, SiteEventDelay::getDelay));
    }

    List<SiteEventDelay> getMostRecentDelayTimesBySiteID() {
        List<String> sitesWithProbeSamplesWithinLastDay = eventDAO.getSitesWithProbeSamplesWithinLastDay();

//        get list of site event delays in the last however minutes and loop
//        through them checking if the 24 hour sites dont exist

        List<SiteEventDelay> mostRecentSitesLastXMinutes = eventDAO.elasticSearchGetSitesAllAtOnce(siteDelayedAfter);
        Collections.sort(mostRecentSitesLastXMinutes);

        // remove duplicate if exists in the array iterating over the already sorted side
        Map<String, SiteEventDelay> mappedDelays = mostRecentSitesLastXMinutes.stream().distinct().collect(
                Collectors.toList()).stream().map(this::populateDelayField).collect(
                        Collectors.toMap(SiteEventDelay::getSiteId, Function.identity()));
        // stream these and calculate their respective delays



        for (String siteId : sitesWithProbeSamplesWithinLastDay) {
            if (!mappedDelays.containsKey(siteId)) {

                Optional<SiteEventDelay> missingOpt = siteIDToDelay(siteId);


                if (missingOpt.isPresent()) {
                    SiteEventDelay missing = missingOpt.get();
                    missing.setSiteId(siteId);
                    mappedDelays.put(siteId, missing);
                }
            }
        }

        return new ArrayList<>(mappedDelays.values());
    }


    private Optional<SiteEventDelay> siteIDToDelay(String siteId) {
        // make elasticsearch request for most recent event per siteID
        Optional<SiteEventDelay> responseContents = eventDAO.getElasticSearchSiteEventDelayObject(siteId);

        // process returned contents into a Site Event Delay object with correct delay
        return responseContents
                .map(s ->
                        {
                            ZonedDateTime dateWithinPayload = convertFakeUTCToZonedDateTime(s.getSampleTimestamp()
                                    , s.getTimezone());
                            s.setDelay(Duration.between(dateWithinPayload, ZonedDateTime.parse(s.getTimestamp())));
                            s.setSiteId(siteId);
                            return s;
                        }
                );
    }

    private SiteEventDelay populateDelayField(SiteEventDelay input) {
        ZonedDateTime dateWithinPayload = convertFakeUTCToZonedDateTime(input.getSampleTimestamp()
                , input.getTimezone());
        input.setDelay(Duration.between(dateWithinPayload, ZonedDateTime.parse(input.getTimestamp())));
        return input;
    }

    // takes utc formatted string in some timezone, converts to ZonedDateTime
    /*
    Necessary due to sampleTimestamps in prod probe-sample-events
    payloads being local to that probe despite the string's utc formatting
     */
    static ZonedDateTime convertFakeUTCToZonedDateTime(String sampleTimestamp, String timezone) {
        ZoneId zone = ZoneId.of(timezone);

        ZonedDateTime date = ZonedDateTime.parse(sampleTimestamp);
        date = ZonedDateTime.of(date.getYear(), date
                        .getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond(),
                date.getNano(), zone);

        return date;
    }

    List<SiteEventDelay> getListofSitesWithDelays(){
        return mostRecentDelayTimeBySiteID;
    }

}



package com.insite360.sitedatalatencycalculator;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;


final class SiteEventDelay implements Comparable<SiteEventDelay> {



    private Payload payload;
    private final String timezone;
    private final String sampleTimestamp;
    private final String timestamp;
    private String brand;
    private String siteId;
    private Duration delay;


    public String getSiteId() {
        return siteId;
    }

    void setSiteId(String siteId) {
        this.siteId = siteId;
    }


    public Duration getDelay() {
        return delay;
    }

    void setDelay(Duration delay) {
        this.delay = delay;
    }


    public String getBrand() {
        return brand;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSampleTimestamp() {
        return sampleTimestamp;
    }

    public String getTimezone() {
        return timezone;
    }


    Boolean isSiteDelayed(Duration siteDelayedAfter) {
        return (getDelay().compareTo(siteDelayedAfter) > 0) || (getDelay().compareTo(Duration.ZERO) < 0);
    }


    @JsonCreator
    SiteEventDelay(String timeZone, String siteId, String brand, String timestamp, Payload payload) {
        this.timezone = timeZone;
        this.siteId = siteId;
        this.brand = MoreObjects.firstNonNull(brand,"UNKNOWN");
        this.timestamp = timestamp;
        this.payload = payload;
        this.sampleTimestamp = payload.getSampleTimestamp();
    }

    SiteEventDelay(String timezone, String sampleTimestamp, String timestamp, String brand) {

        this(timezone, "Unknown",brand, timestamp, new Payload(sampleTimestamp));
    }

    SiteEventDelay(String timezone, String sampleTimestamp, String timestamp, String brand, String siteId) {
        this(timezone, siteId, brand, timestamp, new Payload(sampleTimestamp));
    }

    SiteEventDelay(){
        this("America", "1", "empty", "empty", new Payload("empty"));
    }

    @Override
    public int compareTo(SiteEventDelay o) {

        // NOTE:   flipping these values around from what they would be in order to have sort naturally sort descending
        //         which saves us a reverse operation on 100k entries
        return ZonedDateTime.parse(o.getTimestamp()).compareTo(ZonedDateTime.parse(getTimestamp()));
    }

    @Override
    public boolean equals(Object o) {
        // if it's not a SiteEventDelay
        if (!(o.getClass() == SiteEventDelay.class)) {
            return false;
        }
        SiteEventDelay castO = (SiteEventDelay) o;
        // if it has the same siteId, it's the same object for our purposes
        return (castO.getSiteId().compareTo(getSiteId())) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSiteId());
    }
}

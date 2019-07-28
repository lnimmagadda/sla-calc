package com.insite360.sitedatalatencycalculator;

import java.time.Duration;
import java.util.List;

public class LatencyResponse {
    private List<SiteEventDelay> sites;
    private Duration delayGreaterThan;

    public List<SiteEventDelay> getSites() {
        return sites;
    }

    public void setSites(List<SiteEventDelay> sites) {
        this.sites = sites;
    }

    public Duration getDelayGreaterThan() {
        return delayGreaterThan;
    }

    public void setDelayGreaterThan(Duration delayGreaterThan) {
        this.delayGreaterThan = delayGreaterThan;
    }

     LatencyResponse(List<SiteEventDelay> sites, Duration delayGreaterThan) {
        this.sites = sites;
        this.delayGreaterThan = delayGreaterThan;
    }
}

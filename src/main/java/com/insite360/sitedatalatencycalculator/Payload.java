package com.insite360.sitedatalatencycalculator;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Payload {
    public void setSampleTimestamp(String sampleTimestamp) {
        this.sampleTimestamp = sampleTimestamp;
    }

    private String sampleTimestamp;

    public String getSampleTimestamp() {
        return sampleTimestamp;
    }

    @JsonCreator
    public Payload(String sampleTimestamp) {
        this.sampleTimestamp = sampleTimestamp;
    }
}

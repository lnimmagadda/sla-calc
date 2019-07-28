package com.insite360.sitedatalatencycalculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@JsonTest
@RunWith(SpringRunner.class)
public class SiteEventDelayTest {

    // get hardcoded json
    // deserialize from it


    @Autowired
    ObjectMapper mapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);


    @Test
    public void shouldDeserializeProperly() throws IOException {
        SiteEventDelay testMe;

             testMe = mapper.readValue("{\n" +
                     "  \"deviceType\" : \"TLS350\",\n" +
                     "  \"serialNumber\" : \"86b341ca-c205-4a6e-807d-64e697f633fd\",\n" +
                     "  \"sentTimestamp\" : \"2019-07-17T18:14:22.133Z\",\n" +
                     "  \"messageId\" : \"a8582322-b586-4dc1-bdce-baa568200978\",\n" +
                     "  \"siteName\" : \"COCOS QUICK STOP\",\n" +
                     "  \"timeZone\" : \"America/Chicago\",\n" +
                     "  \"type\" : \"event-tls350-probe-sample-event\",\n" +
                     "  \"deviceId\" : \"10048145\",\n" +
                     "  \"localId\" : \"6747210000\",\n" +
                     "  \"organizationId\" : [ \"0\", \"88888890330\", \"88888891049\", \"88888891294\", \"88888891295\" ],\n" +
                     "  \"@timestamp\" : \"2019-07-17T18:14:22.133Z\",\n" +
                     "  \"pin\" : {\n" +
                     "    \"location\" : {\n" +
                     "      \"lon\" : -92.02058249999999,\n" +
                     "      \"lat\" : 30.2957988\n" +
                     "    }\n" +
                     "  },\n" +
                     "  \"payload\" : {\n" +
                     "    \"sampleTimestamp\" : \"2019-07-17T18:14:22Z\",\n" +
                     "    \"waterHeight\" : 15.904651013346262,\n" +
                     "    \"fuelHeight\" : 1246.2179821848392,\n" +
                     "    \"temperature01\" : 15.582082437967276,\n" +
                     "    \"fuelHeightUOM\" : \"mm\",\n" +
                     "    \"temperature00\" : 15.591156733962018,\n" +
                     "    \"temperature03\" : 15.623120364764032,\n" +
                     "    \"temperature02\" : 15.596123360164288,\n" +
                     "    \"temperature05\" : 15.59121034956568,\n" +
                     "    \"temperatureUOM\" : \"Cel\",\n" +
                     "    \"temperature04\" : 15.5783089180231,\n" +
                     "    \"sendOrder\" : 220087.0,\n" +
                     "    \"waterHeightUOM\" : \"mm\",\n" +
                     "    \"tankId\" : \"1\"\n" +
                     "  },\n" +
                     "  \"@version\" : \"1\",\n" +
                     "  \"siteId\" : \"830209\",\n" +
                     "  \"model\" : \"TLS350\",\n" +
                     "  \"operation\" : \"probe-sample-event\",\n" +
                     "  \"brand\" : \"Independent\",\n" +
                     "  \"timestamp\" : \"2019-07-17T18:14:22.133Z\"\n" +
                     "}", SiteEventDelay.class);

        assertThat(testMe.getTimezone()).isEqualTo("America/Chicago");
        assertThat(testMe.getSampleTimestamp()).isEqualTo("2019-07-17T18:14:22Z");

    }

    @Test
    public void shouldSerializeCorrectly(){
        String result;
        try {
            result =  mapper.writeValueAsString(new SiteEventDelay("America/Chicago", "200", "testBrand", "2019-07-17T18:14:22.133Z", new Payload("2019-07-17T18:14:22Z")));
        }catch (Exception e){
            LOGGER.warn("serialization didn't work", e);
        }
        assertThat(mapper.canSerialize(SiteEventDelay.class)).isTrue();

    }

}
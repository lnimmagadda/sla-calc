package com.insite360.sitedatalatencycalculator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RunWith(SpringRunner.class)
public class EventDataAccessTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);

    @Autowired
    ObjectMapper mapper;


    private EventDAO eventDAO;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void shouldReturnPopulatedObjectAgainstRealData() {

        stubFor(post(urlEqualTo("/real_data"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"took\": 29,\n" +
                                "  \"timed_out\": false,\n" +
                                "  \"_shards\": {\n" +
                                "    \"total\": 738,\n" +
                                "    \"successful\": 738,\n" +
                                "    \"skipped\": 685,\n" +
                                "    \"failed\": 0\n" +
                                "  },\n" +
                                "  \"hits\": {\n" +
                                "    \"total\": 11515,\n" +
                                "    \"max_score\": null,\n" +
                                "    \"hits\": [\n" +
                                "      {\n" +
                                "        \"_index\": \"logstash-event-tls350-probe-sample-event-2019.06\",\n" +
                                "        \"_type\": \"event-tls350-probe-sample-event\",\n" +
                                "        \"_id\": \"AWuPIxTjf_l7JhwmFY1Z\",\n" +
                                "        \"_score\": null,\n" +
                                "        \"_source\": {\n" +
                                "          \"deviceType\": \"TLS350\",\n" +
                                "          \"serialNumber\": \"320c2b7c-fcc3-4a50-9763-0fec6a2c4dd1\",\n" +
                                "          \"sentTimestamp\": \"2019-06-25T14:56:09.416Z\",\n" +
                                "          \"messageId\": \"40a0cea3-ab91-4aad-9c32-cc41ca77aac6\",\n" +
                                "          \"siteName\": \"1 WSM Sim\",\n" +
                                "          \"timeZone\": \"Africa/Abidjan\",\n" +
                                "          \"type\": \"event-tls350-probe-sample-event\",\n" +
                                "          \"deviceId\": \"10047783\",\n" +
                                "          \"localId\": \"2194710000\",\n" +
                                "          \"organizationId\": [\n" +
                                "            \"0\",\n" +
                                "            \"88888890330\",\n" +
                                "            \"88888890809\",\n" +
                                "            \"88888891049\",\n" +
                                "            \"88888891295\"\n" +
                                "          ],\n" +
                                "          \"@timestamp\": \"2019-06-25T14:56:09.416Z\",\n" +
                                "          \"pin\": {\n" +
                                "            \"location\": {\n" +
                                "              \"lon\": -4.0082563,\n" +
                                "              \"lat\": 5.3599517\n" +
                                "            }\n" +
                                "          },\n" +
                                "          \"payload\": {\n" +
                                "            \"sampleTimestamp\": \"2019-06-25T14:56:09Z\",\n" +
                                "            \"waterHeight\": 0.11499759105376817,\n" +
                                "            \"fuelHeight\": 34.992770223327426,\n" +
                                "            \"sendOrder\": 42646,\n" +
                                "            \"temperature01\": 60.002705101298524,\n" +
                                "            \"temperature00\": 60.113619934618676,\n" +
                                "            \"temperature03\": 60.132864973073815,\n" +
                                "            \"temperature02\": 60.113906934705334,\n" +
                                "            \"temperature05\": 60.13611399297452,\n" +
                                "            \"temperature04\": 60.002200070611224,\n" +
                                "            \"tankId\": \"4\"\n" +
                                "          },\n" +
                                "          \"@version\": \"1\",\n" +
                                "          \"siteId\": \"830023\",\n" +
                                "          \"model\": \"TLS350\",\n" +
                                "          \"operation\": \"probe-sample-event\",\n" +
                                "          \"brand\": \"inperial oil\",\n" +
                                "          \"timestamp\": \"2019-06-25T14:56:09.416Z\"\n" +
                                "        },\n" +
                                "        \"sort\": [\n" +
                                "          1561474569416\n" +
                                "        ]\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  }\n" +
                                "}")));
        eventDAO = new EventDAO(new RestTemplateBuilder(), wireMockRule.baseUrl() + "/real_data",new ObjectMapper());
        Optional<SiteEventDelay> siteEventDelay = eventDAO.getElasticSearchSiteEventDelayObject("830023");
        assertThat(siteEventDelay.isPresent()).isTrue();
        assertThat(siteEventDelay.get().getSampleTimestamp()).isEqualTo("2019-06-25T14:56:09Z");
        assertThat(siteEventDelay.get().getTimestamp()).isEqualTo("2019-06-25T14:56:09.416Z");
        assertThat(siteEventDelay.get().getTimezone()).isEqualTo("Africa/Abidjan");
    }

    @Test
    public void shouldGetFieldsFromSparseData() {
        stubFor(post(urlEqualTo("/sparse_data"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"hits\": {\n" +
                                "    \"hits\": [\n" +
                                "      {\n" +
                                "        \"_source\": {\n" +
                                "          \"timeZone\": \"Africa/Abidjan\",\n" +
                                "          \"payload\": {\n" +
                                "            \"sampleTimestamp\": \"2019-06-25T14:56:09Z\",\n" +
                                "          },\n" +
                                "          \"siteId\": \"830023\",\n" +
                                "          \"operation\": \"probe-sample-event\",\n" +
                                "            \"brand\": \"inperial oil\",\n" +
                                "          \"timestamp\": \"2019-06-25T14:56:09.416Z\"\n" +
                                "        },\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  }\n" +
                                "}")));
        eventDAO = new EventDAO(new RestTemplateBuilder(), wireMockRule.baseUrl() + "/sparse_data",new ObjectMapper());
        Optional<SiteEventDelay> siteEventDelay = eventDAO.getElasticSearchSiteEventDelayObject("830023");
        assertThat(siteEventDelay.get().getSampleTimestamp()).isEqualTo("2019-06-25T14:56:09Z");
        assertThat(siteEventDelay.get().getTimestamp()).isEqualTo("2019-06-25T14:56:09.416Z");
        assertThat(siteEventDelay.get().getTimezone()).isEqualTo("Africa/Abidjan");
        assertThat(siteEventDelay.get().getBrand()).isEqualTo("inperial oil");
    }

    @Test
    public void absentOnEmptyResponse() {
        stubFor(post(urlEqualTo("/empty_response"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"took\": 39,\n" +
                                "  \"timed_out\": false,\n" +
                                "  \"_shards\": {\n" +
                                "    \"total\": 738,\n" +
                                "    \"successful\": 738,\n" +
                                "    \"skipped\": 680,\n" +
                                "    \"failed\": 0\n" +
                                "  },\n" +
                                "  \"hits\": {\n" +
                                "    \"total\": 0,\n" +
                                "    \"max_score\": null,\n" +
                                "    \"hits\": []\n" +
                                "  }\n" +
                                "}")));
        eventDAO = new EventDAO(new RestTemplateBuilder(), wireMockRule.baseUrl() + "/empty_response", mapper);
        Optional<SiteEventDelay> siteEventDelay = eventDAO.getElasticSearchSiteEventDelayObject("830023");
        assertThat(siteEventDelay.isPresent()).isFalse();
    }


    @Test
    public void shouldReturnEmptyList() {
        stubFor(post(urlEqualTo("/empty_list"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"took\": 43,\n" +
                                "  \"timed_out\": false,\n" +
                                "  \"_shards\": {\n" +
                                "    \"total\": 742,\n" +
                                "    \"successful\": 742,\n" +
                                "    \"skipped\": 741,\n" +
                                "    \"failed\": 0\n" +
                                "  },\n" +
                                "  \"hits\": {\n" +
                                "    \"total\": 0,\n" +
                                "    \"max_score\": 0,\n" +
                                "    \"hits\": []\n" +
                                "  },\n" +
                                "  \"aggregations\": {\n" +
                                "    \"uniq_ids\": {\n" +
                                "      \"doc_count_error_upper_bound\": 0,\n" +
                                "      \"sum_other_doc_count\": 0,\n" +
                                "      \"buckets\": []\n" +
                                "    }\n" +
                                "  }\n" +
                                "}")));
        eventDAO = new EventDAO(new RestTemplateBuilder(), wireMockRule.baseUrl() + "/empty_list", mapper);
        List<String> results = eventDAO.getSitesWithProbeSamplesWithinLastDay();
        assertThat(results.isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnEmptyListOnServerError() {
        stubFor(post(urlEqualTo("/empty_list"))
                .withHeader("Accept", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));

        eventDAO = new EventDAO(new RestTemplateBuilder(), wireMockRule.baseUrl() + "/empty_list", mapper);
        List<String> results = eventDAO.getSitesWithProbeSamplesWithinLastDay();
        assertThat(results.isEmpty()).isTrue();
    }


    @Test
    public void shouldGetMasterListFromElasticSearch() {
        eventDAO = new EventDAO(new RestTemplateBuilder(), "http://es-analytics-int.dev.aws.gilbarco.com:9200/_search", mapper);
        List<SiteEventDelay> result = eventDAO.elasticSearchGetSitesAllAtOnce(Duration.ofMinutes(5));

        assertThat(result).isNotEmpty();

    }

    private SiteEventDelay randomSiteHelperFunction() {
        SiteEventDelay temp = new SiteEventDelay("", "", ZonedDateTime.now().plusSeconds(new Random().nextInt(10)).toString(), "");
        temp.setSiteId(Integer.toString(new Random().nextInt(4)));
        return temp;
    }

    @Test
    public void deDupTests() {
        List<SiteEventDelay> result = Arrays.asList(randomSiteHelperFunction(), randomSiteHelperFunction(), randomSiteHelperFunction(), randomSiteHelperFunction(), randomSiteHelperFunction());
        LOGGER.debug(result.get(0).getSampleTimestamp());
        assertThat(result).isNotEmpty();
        Collections.sort(result);
        assertThat(ZonedDateTime.parse(result.get(result.size() - 1).getTimestamp())).isBefore(ZonedDateTime.parse(result.get(0).getTimestamp()));
    }
}

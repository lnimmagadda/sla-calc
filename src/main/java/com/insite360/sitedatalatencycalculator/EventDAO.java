package com.insite360.sitedatalatencycalculator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;


@Repository
public class EventDAO {

    private final RestTemplateBuilder restTemplateBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyService.class);

    private String url;


    private final ObjectMapper mapper;


    @Autowired
    EventDAO(RestTemplateBuilder restTemplateBuilder, @Value("${url}") String url, ObjectMapper mapper) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.mapper = mapper;
        this.url = url;
    }


    private static String getRawJsonFromResourcesFolder(String classPathFileName) {
//        from https://stackoverflow.com/questions/25869428/classpath-resource-not-found-when-running-as-jar/
//        slightly modified so that dan doesn't have to look at byte arrays

        String data;
        ClassPathResource cpr = new ClassPathResource(classPathFileName);
        try {
            data = FileCopyUtils.copyToString(new InputStreamReader(cpr.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }


    private Optional<String> elasticSearchRawJSONRequest(String jsonAsString, String url) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonAsString, httpHeaders);
        try {
            return Optional.of(restTemplate.postForObject(url, httpEntity, String.class));
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            return empty();
        }
    }


    Optional<SiteEventDelay> getElasticSearchSiteEventDelayObject(String siteId) {
        // get local json request and change string placeholder to be siteId
        String jsonWithSiteInserted = String.format(EventDAO
                .getRawJsonFromResourcesFolder("./OneMostRecentProbeSampleEventForID.json"), siteId);

        // make es request, parse response into json obj for easy reads
        Optional<String> result = elasticSearchRawJSONRequest(jsonWithSiteInserted, url);
        if (!result.isPresent()) {
            return empty();
        }

        Object document = Configuration.defaultConfiguration().jsonProvider()
                .parse(result.get());

        try {
            String timezone = JsonPath.read(document, "$.hits.hits[0]._source.timeZone");
            String sampleTimestamp = JsonPath.read(document, "$.hits.hits[0]._source.payload.sampleTimestamp");
            String timestamp = JsonPath.read(document, "$.hits.hits[0]._source.timestamp");
            String brand = JsonPath.read(document, "$.hits.hits[0]._source.brand");
            return Optional.of(new SiteEventDelay(timezone, sampleTimestamp, timestamp, brand));
        } catch (PathNotFoundException e) {
            return empty();
        }

    }


    List<String> getSitesWithProbeSamplesWithinLastDay() {
        Optional<String> result = elasticSearchRawJSONRequest
                (EventDAO.getRawJsonFromResourcesFolder("./UniqueIds.json"), url);
        if (!result.isPresent()) {
            return Collections.emptyList();
        }
        return JsonPath.read(result.get(),
                "$.aggregations.uniq_ids.buckets[*].key");
    }


    List<SiteEventDelay> elasticSearchGetSitesAllAtOnce(Duration siteDelayedAfter) {
        // gives the first 10k results and  scroll_id in string form
        // holds the results for 5m
        Optional<String> result = elasticSearchRawJSONRequest(
                String.format(getRawJsonFromResourcesFolder(
                        "AllProbeSamplesLastNMinutes.json"),siteDelayedAfter.toMinutes()),
                url + "?scroll=5m");
        if (!result.isPresent()) {
            LOGGER.warn("Result not present");
            return Collections.emptyList();
        }


        JsonNode node;
        try {
            node = mapper.readTree(result.get());
        } catch (Exception e) {
            LOGGER.warn("problem with parsing json", e);
            return null;
        }
        String currentScrollId = node.get("_scroll_id").textValue();
        List<SiteEventDelay> aggregateElasticSearchResponse = new ArrayList<>();


        Integer hitsTotal = node.get("hits").get("hits").size();

        // while hit array has entries in it
        while (hitsTotal != 0) {

            JsonNode jsonNode = node.get("hits").get("hits");
            for (JsonNode root : jsonNode) {
                SiteEventDelay temp;
                try {
                    temp = mapper.treeToValue(root.get("_source"), SiteEventDelay.class);
                } catch (Exception e) {
                    LOGGER.warn("failed to parse json into class with jackson", e);
                    return Collections.emptyList();
                }
                aggregateElasticSearchResponse.add(temp);

            }


            result = elasticSearchRawJSONRequest(
                    String.format(getRawJsonFromResourcesFolder("./ScrollBody.json"),
                            currentScrollId), url + "/scroll");
            try {
                node = mapper.readTree(result.get());
            } catch (Exception e) {
                e.getMessage();
                return null;
            }

            // update the hits value for how many results we got this time
            hitsTotal = node.get("hits").get("hits").size();


            // make a list of siteEventDelays and for each add them to some master list

            //new or the same scrollId for the next go round
            currentScrollId = node.get("_scroll_id").textValue();
            LOGGER.debug(hitsTotal.toString());
        }
        return aggregateElasticSearchResponse;
    }


}

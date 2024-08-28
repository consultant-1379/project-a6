package com.ericsson.graduate.microservices.gerrit.controller;

import com.ericsson.graduate.core.dto.CommitDTO;
import com.ericsson.graduate.microservices.gerrit.data.GitLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/commits")
public class CommitController {
    private static final Logger LOG = LoggerFactory.getLogger(CommitController.class);
    private static final String COMMITS_FETCH_STRING =
            "https://gerrit.ericsson.se/a/plugins/gitiles/%s/+log/master/?format=json&no-merges";

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(value = "/", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<List<CommitDTO>> getCommitsFromProject(
            @RequestBody ObjectNode body,
            @RequestParam(name = "n", required = false) Integer numberOfCommits) throws JsonProcessingException {
        if (!body.has("programName")) {
            return ResponseEntity.badRequest().build();
        }
        String programName = body.get("programName").asText();
        String fetchString = String.format(COMMITS_FETCH_STRING, parseProgramNameForURL(programName));

        String latestCommit = body.has("latestCommit") ? body.get("latestCommit").asText() : null;
        if (numberOfCommits != null) {
            fetchString += "&n=" + numberOfCommits;
        }
        if (latestCommit != null) {
            fetchString += "&reverse&s=" + latestCommit;
        }

        ResponseEntity<String> result;
        try {
            result = restTemplate.getForEntity(fetchString, String.class);
        } catch (RestClientException e) {
            LOG.error("Failed to fetch commits for program {}", programName);
            LOG.debug(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        if (result.getStatusCode() == HttpStatus.OK) {
            if (result.hasBody()) {
                GitLog log = parseLogFromStringBody(result.getBody());
                return ResponseEntity.ok(log.getCommits(latestCommit));
            } else {
                return ResponseEntity.noContent().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private String parseProgramNameForURL(String name) {
        return name.replace("/", "%2F");
    }

    private GitLog parseLogFromStringBody(String body) throws JsonProcessingException {
        if (body == null) {
            return new GitLog();
        }
        body = body.substring(4);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(body, GitLog.class);
    }
}

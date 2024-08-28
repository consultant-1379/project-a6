package com.ericsson.graduate.microservices.jenkins.controller;

import com.ericsson.graduate.core.dto.BuildDTO;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/builds")
public class BuildController {
    private static final Logger LOG = LoggerFactory.getLogger(BuildController.class);
    private static final String PROGRAM_NAME = "programName";
    private static final String LATEST_BUILD = "latestBuild";

    private static final String JENKINS_SERVER = "https://fem142-eiffel004.lmera.ericsson.se:8443/jenkins/";

    @PostMapping(value = "/", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<List<BuildDTO>> getBuildsFromProject(@RequestBody ObjectNode body) {
        if (!body.has(PROGRAM_NAME)) {
            return ResponseEntity.notFound().build();
        }
        String programName = body.get(PROGRAM_NAME).asText();
        try (JenkinsServer jenkins = new JenkinsServer(new URI(JENKINS_SERVER),
                "ebeecon", "c52d31eda1677d8ed6257f10b306dbe6")) {
            Job job = jenkins.getJobs().getOrDefault(programName, null);
            JobWithDetails jobWithDetails;
            if (job == null || (jobWithDetails = job.details()) == null) {
                return ResponseEntity.badRequest().build();
            }
            if (body.has(LATEST_BUILD) &&
                    body.get(LATEST_BUILD).asInt() == jobWithDetails.getNextBuildNumber()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            Range range = Range.build().from(body.has(LATEST_BUILD) ? body.get(LATEST_BUILD).asInt() : 1).build();
            List<Build> listOfBuilds = jobWithDetails.getAllBuilds(range);

            List<BuildDTO> convertBuilds = listOfBuilds.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(convertBuilds);
        } catch (URISyntaxException e) {
            LOG.debug(e.getMessage());
            LOG.error("Failed to make uri for {}", JENKINS_SERVER);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            LOG.debug(e.getMessage());
            LOG.error("Failed to fetch builds for {}", programName);
            return ResponseEntity.badRequest().build();
        }
    }

    private BuildDTO toDTO(Build build) {
        BuildDTO dto = new BuildDTO();
        dto.setBuildNumber(build.getNumber());

        TestResult buildTestResults = null;
        try {
            buildTestResults = build.getTestResult();
        } catch (IOException ignore) {
            LOG.error("Error getting test result for build #{}", build.getNumber());
        }

        dto.setTotalTestsPerBuild(buildTestResults == null ? 0 : buildTestResults.getPassCount() + buildTestResults.getFailCount());
        dto.setTotalPassedTestsPerBuild(buildTestResults == null ? 0 : buildTestResults.getPassCount());
        dto.setTotalFailedTestsPerBuild(buildTestResults == null ? 0 : buildTestResults.getFailCount());

        BuildWithDetails details = null;
        try {
            details = build.details();
        } catch (IOException ignore) {
            LOG.error("Error getting details for build #{}", build.getNumber());
        }

        dto.setBuildTimestamp(details == null ? null :
                LocalDateTime.ofInstant(Instant.ofEpochMilli(details.getTimestamp()), ZoneId.systemDefault()));

        dto.setStatus(details == null || details.getResult() == null ? "UNKNOWN" : details.getResult().toString());
        return dto;
    }
}

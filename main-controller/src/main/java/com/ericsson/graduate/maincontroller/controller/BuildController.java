package com.ericsson.graduate.maincontroller.controller;

import com.ericsson.graduate.core.dto.BuildDTO;
import com.ericsson.graduate.maincontroller.model.Build;
import com.ericsson.graduate.maincontroller.model.Program;
import com.ericsson.graduate.maincontroller.repository.BuildRepository;
import com.ericsson.graduate.maincontroller.repository.ProgramRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/programs")
public class BuildController {
    private static final Logger LOG = LoggerFactory.getLogger(BuildController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BuildRepository buildRepository;
    @Autowired
    private ProgramRepository programRepository;

    @Value("${team-dashboard.jenkins-url}")
    private String jenkinsUrl;

    @GetMapping(value = "/{id}/builds", produces = {"application/json"})
    public ResponseEntity<List<Build>> getAllBuilds(@PathVariable("id") long programId,
                                                    @RequestParam(name = "n", required = false) Integer amount) {
        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isPresent()) {
            Program program = programOptional.get();

            String fetchString = String.format("%s/builds/", jenkinsUrl);

            ResponseEntity<BuildDTO[]> buildsResponse = null;
            try {
                Map<String, JsonNode> nodes = new HashMap<>();
                nodes.put("programName", new TextNode(program.getJenkinsName()));
                Build latestBuild = program.getLatestBuild();
                if (latestBuild != null) {
                    nodes.put("latestBuild", new IntNode(latestBuild.getBuildNumber()));
                }
                buildsResponse = restTemplate.postForEntity(fetchString,
                        new ObjectNode(JsonNodeFactory.instance, nodes), BuildDTO[].class);
            } catch (RestClientException e) {
                LOG.error("Failed to fetch Builds for program {} ({})", programId, program.getJenkinsName());
                LOG.debug(e.getMessage());
            }

            if (buildsResponse != null && buildsResponse.getStatusCode() == HttpStatus.OK &&
                    buildsResponse.getBody() != null) {
                List<Build> builds = Arrays.stream(buildsResponse.getBody())
                        .map(Build::new)
                        .filter(Predicate.not(program::containsBuild))
                        .peek(build -> build.setProgram(program))
                        .collect(Collectors.toList());

                program.getBuilds().addAll(builds);
                buildRepository.saveAll(builds);
                programRepository.save(program);

            }
            if (amount == null) {
                amount = Integer.MAX_VALUE;
            }
            return ResponseEntity.ok(program.getBuilds().stream().sorted(Comparator.comparing(Build::getBuildTimestamp).reversed()).limit(amount).collect(Collectors.toList()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{id}/builds/buildRates", produces = {"application/json"})
    public ResponseEntity<?> getBuildSuccessRates(@PathVariable("id") long programId) {
        AtomicInteger buildSuccessful = new AtomicInteger();
        AtomicInteger buildFailed = new AtomicInteger();
        AtomicInteger buildOther = new AtomicInteger();

        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isPresent()) {
            Set<Build> builds = programOptional.get().getBuilds();

            builds.stream()
                    .map(Build::getStatus)
                    .forEach(status -> {
                        if (status.equals("SUCCESS")) {
                            buildSuccessful.getAndIncrement();
                        } else if (status.equals("FAILURE")) {
                            buildFailed.getAndIncrement();
                        } else {
                            buildOther.getAndIncrement();
                        }
                    });
            Map<String, JsonNode> nodes = new HashMap<>();
            nodes.put("SUCCESS", new IntNode(buildSuccessful.get()));
            nodes.put("FAILURE", new IntNode(buildFailed.get()));
            nodes.put("OTHER", new IntNode(buildOther.get()));
            ObjectNode returnBody = new ObjectNode(JsonNodeFactory.instance, nodes);
            return ResponseEntity.ok(returnBody);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{id}/builds/testRates", produces = {"application/json"})
    public ResponseEntity<?> getBuildTestRate(@PathVariable("id") long programId) {

        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isPresent()) {
            Set<Build> builds = programOptional.get().getBuilds();
            ObjectNode returnBody = new ObjectNode(JsonNodeFactory.instance, new HashMap<>());
            builds.forEach(build -> {
                Map<String, JsonNode> nodes = new HashMap<>();
                nodes.put("TotalTestsPerBuild", new IntNode(build.getTotalTestsPerBuild()));
                nodes.put("TotalPassedTestsPerBuild", new IntNode(build.getTotalPassedTestsPerBuild()));
                nodes.put("TotalFailedTestsPerBuild", new IntNode(build.getTotalFailedTestsPerBuild()));

                ObjectNode testResults = new ObjectNode(JsonNodeFactory.instance, nodes);
                returnBody.set(build.getBuildTimestamp().toString(), testResults);

            });
            return ResponseEntity.ok(returnBody);
        }
        return ResponseEntity.notFound().build();
    }
}

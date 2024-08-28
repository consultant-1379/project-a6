package com.ericsson.graduate.maincontroller.controller;

import com.ericsson.graduate.core.dto.CommitDTO;
import com.ericsson.graduate.maincontroller.model.Commit;
import com.ericsson.graduate.maincontroller.model.Program;
import com.ericsson.graduate.maincontroller.repository.CommitRepository;
import com.ericsson.graduate.maincontroller.repository.ProgramRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/commits")
public class CommitController {
    private static final Logger LOG = LoggerFactory.getLogger(CommitController.class);

    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${team-dashboard.gerrit-url}")
    private String gerritUrl;

    @GetMapping(value = "/author/{email}", produces = {"application/json"})
    public ResponseEntity<List<Commit>> getCommitsByAuthor(@PathVariable("email") String email) {
        List<Commit> commits = commitRepository.findAllCommitByAuthorEmail(email);
        return ResponseEntity.ok(commits);
    }

    @GetMapping(value = "/programs/{pId}", produces = {"application/json"})
    public ResponseEntity<List<Commit>> getCommitsFromProject(@PathVariable("pId") long programId,
                                                              @RequestParam(name = "n", required = false) Integer amount) {
        Optional<Program> programOptional = programRepository.findById(programId);
        if (programOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Program program = programOptional.get();

        String fetchString = String.format("%s/api/commits/", gerritUrl);
        if (amount != null && !program.getCommits().isEmpty()) {
            fetchString += "?n=" + amount;
        }

        ResponseEntity<CommitDTO[]> commitsResponse = null;
        try {
            Map<String, JsonNode> nodes = new HashMap<>();
            nodes.put("programName", new TextNode(program.getGerritName()));
            Commit latestCommit = program.getLatestCommit();
            if (latestCommit != null) {
                nodes.put("latestCommit", new TextNode(latestCommit.getCommitId().substring(0, 7)));
            }
            commitsResponse = restTemplate.postForEntity(fetchString,
                    new ObjectNode(JsonNodeFactory.instance, nodes),
                    CommitDTO[].class);
        } catch (RestClientException e) {
            LOG.error("Failed to fetch commits for program {} ({})", programId, program.getGerritName());
            LOG.debug(e.getMessage());
        }

        if (commitsResponse != null && commitsResponse.getStatusCode() == HttpStatus.OK &&
                commitsResponse.getBody() != null) {
            List<Commit> commits = Arrays.stream(commitsResponse.getBody())
                    .map(Commit::new)
                    .filter(Predicate.not(program::containsCommit))
                    .peek(commit -> commit.setProgram(program))
                    .sorted(Comparator.comparing(Commit::getTime).reversed())
                    .collect(Collectors.toList());
            program.getCommits().addAll(commits);
            commitRepository.saveAll(commits);
            programRepository.save(program);
        }
        if (amount == null) {
            amount = Integer.MAX_VALUE;
        }
        return ResponseEntity.ok(program.getCommits().stream()
                .sorted(Comparator.comparing(Commit::getTime).reversed())
                .distinct()
                .limit(amount)
                .collect(Collectors.toList()));
    }
}

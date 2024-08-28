package com.ericsson.graduate.frontend.controller;

import com.ericsson.graduate.core.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class FrontendController {
    private static final Logger LOG = LoggerFactory.getLogger(FrontendController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${team-dashboard.controller-url}")
    private String controllerUrl;

    @GetMapping("/program")
    public String program(@RequestParam(name = "pid") long programID,
                          @RequestParam(name = "viewall", required = false, defaultValue = "false") boolean viewAll,
                          Model model) {
        ResponseEntity<ProgramDTO> programResponse = restTemplate.getForEntity(
                String.format("%s/programs/%d/", controllerUrl, programID),
                ProgramDTO.class);
        if (programResponse.getStatusCode() != HttpStatus.OK || programResponse.getBody() == null) {
            return "home";
        }
        model.addAttribute("program", programResponse.getBody());

        ResponseEntity<TeamDTO[]> teamsResponse = restTemplate.getForEntity(
                String.format("%s/programs/%d/teams/", controllerUrl, programID),
                TeamDTO[].class);
        if (teamsResponse.getStatusCode() == HttpStatus.OK && teamsResponse.getBody() != null) {
            model.addAttribute("teams", teamsResponse.getBody());
        }

        ResponseEntity<CommitDTO[]> commitsResponse = restTemplate.getForEntity(
                String.format("%s/commits/programs/%d/%s", controllerUrl, programID, viewAll ? "" : "?n=5"),
                CommitDTO[].class);
        if (commitsResponse.getStatusCode() == HttpStatus.OK && commitsResponse.getBody() != null) {
            model.addAttribute("commits", commitsResponse.getBody());
        }

        ResponseEntity<BuildDTO[]> buildsResponse = restTemplate.getForEntity(
                String.format("%s/programs/%d/builds/%s", controllerUrl, programID, viewAll ? "" : "?n=5"),
                BuildDTO[].class);
        if (buildsResponse.getStatusCode() == HttpStatus.OK && buildsResponse.getBody() != null) {
            model.addAttribute("builds", buildsResponse.getBody());
            model.addAttribute("buildRates", getResults(buildsResponse.getBody()));
        }

        ResponseEntity<String> buildTestRates = restTemplate.getForEntity(
                String.format("%s/programs/%d/builds/testRates", controllerUrl, programID),
                String.class);
        if (buildsResponse.getStatusCode() == HttpStatus.OK && buildsResponse.getBody() != null) {
            model.addAttribute("buildTestRates", buildTestRates.getBody());
        }

        return "program";
    }

    private Map<String, Integer> getResults(BuildDTO... builds) {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger unknown = new AtomicInteger();

        for (BuildDTO buildDTO : builds) {
            if (buildDTO.getStatus().equals("SUCCESS")) {
                success.incrementAndGet();
            } else if (buildDTO.getStatus().equals("FAILURE")) {
                failed.incrementAndGet();
            } else {
                unknown.incrementAndGet();
            }
        }

        Map<String, Integer> results = new HashMap<>();
        results.put("SUCCESS", success.intValue());
        results.put("FAILED", failed.intValue());
        results.put("UNKNOWN", unknown.intValue());
        return results;
    }

    @GetMapping("/home")
    public String home(Model model) {
        ResponseEntity<MemberDTO[]> responseEntityForMembers = restTemplate.
                getForEntity(String.format("%s/members/", controllerUrl), MemberDTO[].class);
        ResponseEntity<TeamDTO[]> responseEntityForTeams =
                restTemplate.getForEntity(String.format("%s/teams/", controllerUrl), TeamDTO[].class);
        ResponseEntity<ProgramDTO[]> responseEntityForPrograms =
                restTemplate.getForEntity(String.format("%s/programs/", controllerUrl), ProgramDTO[].class);

        MemberDTO[] members = responseEntityForMembers.getBody();
        TeamDTO[] teams = responseEntityForTeams.getBody();
        ProgramDTO[] programs = responseEntityForPrograms.getBody();

        model.addAttribute("newMember", new MemberDTO());
        model.addAttribute("newTeam", new TeamDTO());
        model.addAttribute("newProgram", new ProgramDTO());
        model.addAttribute("teams", teams);
        model.addAttribute("members", members);
        model.addAttribute("programs", programs);

        return "home";
    }

    @PostMapping(value = "/createMember")
    public String handleCreateMember(@ModelAttribute MemberDTO newMember, Model model) {
        LOG.debug("Adding new member: {}", newMember);
        restTemplate.postForEntity(
                String.format("%s/members/", controllerUrl), newMember, MemberDTO.class);
        return home(model);
    }

    @PostMapping(value = "/createTeam")
    public String handleCreateTeam(@ModelAttribute TeamDTO newTeam, Model model) {
        LOG.debug("Adding new team: {}", newTeam);
        restTemplate.postForEntity(String.format("%s/teams/", controllerUrl), newTeam, TeamDTO.class);
        return home(model);
    }

    @PostMapping(value = "/createProgram")
    public String handleCreateProgram(@ModelAttribute ProgramDTO newProgram, Model model) {
        LOG.debug("Adding new program: {}", newProgram);
        restTemplate.postForEntity(String.format("%s/programs/", controllerUrl), newProgram, ProgramDTO.class);
        return home(model);
    }

    @PostMapping(value = "/addMember")
    public String handleAddMemberToTeam(Model model, @RequestParam(value = "memberId") long memberId,
                                        @RequestParam(value = "teamId") long teamId) {
        LOG.debug("Adding Member {} to Team {}", memberId, teamId);
        restTemplate.put(String.format("%s/teams/%d/members/%d", controllerUrl, teamId, memberId), null);

        return home(model);
    }

    @PostMapping(value = "/addTeam")
    public String handleAddTeamToProgram(Model model, @RequestParam(value = "teamId") long teamId,
                                         @RequestParam(value = "programId") long programId) {
        LOG.debug("Adding Team {} to program {}", teamId, programId);
        restTemplate.put(String.format("%s/programs/%d/teams/%d", controllerUrl, programId, teamId), null);

        return home(model);
    }
}

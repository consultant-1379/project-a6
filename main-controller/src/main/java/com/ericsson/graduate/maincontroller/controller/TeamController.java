package com.ericsson.graduate.maincontroller.controller;

import com.ericsson.graduate.core.dto.TeamDTO;
import com.ericsson.graduate.maincontroller.model.Member;
import com.ericsson.graduate.maincontroller.model.Program;
import com.ericsson.graduate.maincontroller.model.Team;
import com.ericsson.graduate.maincontroller.repository.MemberRepository;
import com.ericsson.graduate.maincontroller.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teams")
public class TeamController {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MemberRepository memberRepository;

    @GetMapping(value = "/", produces = {"application/json"})
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamRepository.findAll());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Team> getTeam(@PathVariable("id") long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);
        return team.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Team> addTeam(@RequestBody TeamDTO teamDTO) {
        Team team = teamRepository.save(new Team(teamDTO));
        return ResponseEntity.created(URI.create("/teams/" + team.getId())).body(team);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeTeam(@PathVariable("id") long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isPresent()) {
            Team team = teamOpt.get();
            team.getMembers().stream().map(Member::getTeams).forEach(teams -> teams.remove(team));
            memberRepository.saveAll(team.getMembers());
            teamRepository.delete(team);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<?> addOrRemoveMemberFromTeam(long teamId, long memberId, boolean add) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        Optional<Member> memberOptional = memberRepository.findById(memberId);

        if (teamOptional.isEmpty() || memberOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Team team = teamOptional.get();
        Member member = memberOptional.get();

        if (add) {
            team.getMembers().add(member);
            member.getTeams().add(team);
        } else {
            team.getMembers().remove(member);
            member.getTeams().remove(team);
        }

        teamRepository.save(team);
        memberRepository.save(member);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{tId}/members/{mId}")
    public ResponseEntity<?> addMemberToTeam(@PathVariable("tId") long teamId,
                                             @PathVariable("mId") long memberId) {
        return addOrRemoveMemberFromTeam(teamId, memberId, true);
    }

    @DeleteMapping(value = "/{tId}/members/{mId}")
    public ResponseEntity<?> removeMemberFromTeam(@PathVariable("tId") long teamId,
                                                  @PathVariable("mId") long memberId) {
        return addOrRemoveMemberFromTeam(teamId, memberId, false);
    }

    @GetMapping(value = "/{id}/members", produces = {"application/json"})
    public ResponseEntity<List<Member>> getMembersFromTeam(@PathVariable("id") long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);
        if (team.isPresent()) {
            return ResponseEntity.ok(new ArrayList<>(team.get().getMembers()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{id}/programs", produces = {"application/json"})
    public ResponseEntity<List<Program>> getProgramsFromTeam(@PathVariable("id") long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);
        if (team.isPresent()) {
            return ResponseEntity.ok(new ArrayList<>(team.get().getPrograms()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

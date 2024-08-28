package com.ericsson.graduate.maincontroller.controller;

import com.ericsson.graduate.core.dto.ProgramDTO;
import com.ericsson.graduate.maincontroller.model.Program;
import com.ericsson.graduate.maincontroller.model.Team;
import com.ericsson.graduate.maincontroller.repository.ProgramRepository;
import com.ericsson.graduate.maincontroller.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/programs")
public class ProgramController {
    @Autowired
    private ProgramRepository repository;

    @Autowired
    private TeamRepository teamRepository;

    @GetMapping(value = "/", produces = {"application/json"})
    public ResponseEntity<List<Program>> getAllPrograms() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Program> getProgram(@PathVariable("id") long programId) {
        Optional<Program> program = repository.findById(programId);
        return program.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping(value = "/", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Program> addProgram(@RequestBody ProgramDTO programDTO) {
        Program program = repository.save(new Program(programDTO));
        return ResponseEntity.created(URI.create("/programs/" + program.getId())).body(program);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeProgram(@PathVariable("id") long programId) {
        Optional<Program> programOptional = repository.findById(programId);
        if (programOptional.isPresent()) {
            Program program = programOptional.get();
            program.getTeams().forEach(team -> team.getPrograms().remove(program));
            repository.delete(programOptional.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/{id}/teams", produces = {"application/json"})
    public ResponseEntity<List<Team>> getTeamsOnProject(@PathVariable("id") long programId) {
        Optional<Program> program = repository.findById(programId);
        if (program.isPresent()) {
            return ResponseEntity.ok(new ArrayList<>(program.get().getTeams()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<?> addOrRemoveTeamFromProgram(long teamId, long programId, boolean add) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        Optional<Program> programOptional = repository.findById(programId);

        if (teamOptional.isEmpty() || programOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Team team = teamOptional.get();
        Program program = programOptional.get();

        if (add) {
            team.getPrograms().add(program);
            program.getTeams().add(team);
        } else {
            team.getPrograms().remove(program);
            program.getTeams().remove(team);
        }

        teamRepository.save(team);
        repository.save(program);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{pId}/teams/{tId}")
    public ResponseEntity<?> addTeamToProgram(@PathVariable("pId") long programId,
                                              @PathVariable("tId") long teamId) {
        return addOrRemoveTeamFromProgram(teamId, programId, true);
    }

    @DeleteMapping(value = "/{pId}/teams/{tId}")
    public ResponseEntity<?> removeTeamFromProgram(@PathVariable("pId") long programId,
                                                   @PathVariable("tId") long teamId) {
        return addOrRemoveTeamFromProgram(teamId, programId, false);
    }
}
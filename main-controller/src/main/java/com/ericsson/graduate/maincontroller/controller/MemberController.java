package com.ericsson.graduate.maincontroller.controller;

import com.ericsson.graduate.core.dto.MemberDTO;
import com.ericsson.graduate.maincontroller.model.Member;
import com.ericsson.graduate.maincontroller.model.Team;
import com.ericsson.graduate.maincontroller.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
public class MemberController {
    @Autowired
    private MemberRepository memberRepository;

    @GetMapping(value = "/", produces = {"application/json"})
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberRepository.findAll());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Member> getMember(@PathVariable("id") long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        return member.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/", consumes = {"application/json"},
            produces = {"application/json"})
    public ResponseEntity<Member> addMember(@RequestBody MemberDTO memberDTO) {
        Member member = memberRepository.save(new Member(memberDTO));
        return ResponseEntity.created(URI.create("/member/" + member.getId())).body(member);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeMember(@PathVariable("id") long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isPresent()) {
            memberRepository.delete(member.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "{id}/teams", produces = {"application/json"})
    public ResponseEntity<List<Team>> getTeamsFromMember(@PathVariable("id") long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isPresent()) {
            return ResponseEntity.ok(new ArrayList<>(member.get().getTeams()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

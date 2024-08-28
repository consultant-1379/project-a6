package com.ericsson.graduate.maincontroller.jpa;


import com.ericsson.graduate.maincontroller.model.Member;
import com.ericsson.graduate.maincontroller.model.Program;
import com.ericsson.graduate.maincontroller.model.Team;
import com.ericsson.graduate.maincontroller.repository.MemberRepository;
import com.ericsson.graduate.maincontroller.repository.ProgramRepository;
import com.ericsson.graduate.maincontroller.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Profile("!production")
public class SeedDB {
    private final Map<String, List<String>> teamMembers;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ProgramRepository programRepository;

    public SeedDB() {
        teamMembers = new HashMap<>();
        teamMembers.put("Team 1", Arrays.asList("EIAWLPO", "ERINGEB", "ELOFDYL", "EKNIVAN"));
        teamMembers.put("Team 2", Arrays.asList("EBEECON", "EKZKPTK", "EWSNWKI", "ECSCNTH"));
        teamMembers.put("Team 3", Arrays.asList("EBENCIA", "ESIMDAM", "EOTMRAH"));
        teamMembers.put("Team 4", Arrays.asList("EIMMACC", "EYITSUN", "EADCUSN"));
        teamMembers.put("Team 5", Arrays.asList("ESEHANU", "EOMEWIL", "EWAAMLR"));
    }

    @PostConstruct
    public void seedDB() {
        setupMembers();
        setupTeams();
        setupPrograms();

        addTeamsToPrograms();
        addMembersToTeams();
    }

    private void setupMembers() {
        memberRepository.saveAll(Arrays.asList(
                getMember("ECSCNTH", "Nathan", "Cusack"),
                getMember("EWSNWKI", "Tomasz", "Wisniowski"),
                getMember("EKZKPTK", "Patryk", "Kozak"),
                getMember("EBEECON", "Conor", "Beenham"),
                getMember("EIAWLPO", "Liam", "Power"),
                getMember("ERINGEB", "Benjamin", "Grimes"),
                getMember("ELOFDYL", "Dylan", "Loftus"),
                getMember("EKNIVAN", "Vanessa", "Knibbe"),
                getMember("EBENCIA", "Ciaran", "Bent"),
                getMember("ESIMDAM", "Simas", "Damanauskas"),
                getMember("EOTMRAH", "Edward", "Martin"),
                getMember("EIMMACC", "Michelle", "Campion"),
                getMember("EYITSUN", "Yitong", "Sun"),
                getMember("EADCUSN", "Daniel", "Cusack"),
                getMember("ESEHANU", "Anuva", "Sehgal"),
                getMember("EOMEWIL", "William", "O'Meara"),
                getMember("EWAAMLR", "Martin", "Walsh")
        ));
    }

    private void setupTeams() {
        teamRepository.saveAll(Arrays.asList(
                getTeam("Team 1"),
                getTeam("Team 2"),
                getTeam("Team 3"),
                getTeam("Team 4"),
                getTeam("Team 5")
        ));
    }

    private void setupPrograms() {
        programRepository.saveAll(Arrays.asList(
                getProgram("Alarm Link Management",
                        "OSS/com.ericsson.oss.services.connectivity.alm/alarmlinkmanagement-service",
                        "alarmlinkmanagement-service_PCR"),
                getProgram("NHC Profile Management Service",
                        "OSS/com.ericsson.oss.services.healthcheck/nhc-profile-management-service",
                        "nhc-profile-management-service_PCR"),
                getProgram("EBS Stream Topology Service",
                        "OSS/com.ericsson.oss.services.pm.ebs/ebs-stream-topology-service",
                        "ebs-stream-topology-service_Release"),
                getProgram("Monitoring Troubleshooting Flows",
                        "OSS/com.ericsson.oss.services.mtsflows/monitoring-troubleshooting-flows",
                        "monitoring-troubleshooting-flows_Release"),
                getProgram("Project A5", "OSS/com.ericsson.graduates/project-a5", null),
                getProgram("Project A6", "OSS/com.ericsson.graduates/project-a6", null),
                getProgram("Project A7", "OSS/com.ericsson.graduates/project-a7", null),
                getProgram("Project A8", "OSS/com.ericsson.graduates/project-a8", null),
                getProgram("Project A9", "OSS/com.ericsson.graduates/project-a9", null)
        ));
    }

    private void addTeamsToPrograms() {
        List<Program> programs = programRepository.findAll();
        List<Team> teams = teamRepository.findAll();

        for (int i = 0; i < 5; i++) {
            programs.get(i + 4).getTeams().add(teams.get(i));
            teams.get(i).getPrograms().add(programs.get(i + 4));
        }

        programRepository.saveAll(programs);
        teamRepository.saveAll(teams);
    }

    private void addMembersToTeams() {
        for (Map.Entry<String, List<String>> entry : teamMembers.entrySet()) {
            Optional<Team> team = teamRepository.findTeamByName(entry.getKey());

            if (team.isPresent()) {
                List<Member> members = memberRepository.findAllBySignum(entry.getValue());
                team.get().getMembers().addAll(members);
                members.forEach(member -> member.getTeams().add(team.get()));
                memberRepository.saveAll(members);
                teamRepository.save(team.get());
            }
        }
    }

    private Member getMember(String signum, String firstName, String lastName) {
        Member member = new Member();
        member.setSignum(signum);
        member.setFirstName(firstName);
        member.setLastName(lastName);
        return member;
    }

    private Team getTeam(String name) {
        Team team = new Team();
        team.setName(name);
        return team;
    }

    private Program getProgram(String name, String gerritName, String jenkinsName) {
        Program program = new Program();
        program.setName(name);
        program.setGerritName(gerritName);
        program.setJenkinsName(jenkinsName);
        return program;
    }
}

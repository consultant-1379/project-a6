package com.ericsson.graduate.maincontroller.model;

import com.ericsson.graduate.core.dto.ProgramDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(exclude = {"teams"})
@Data
@Entity
@Table(name = "programs")
@NoArgsConstructor
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Column(unique = true)
    @Length(max = 64)
    private String name;

    @Column(unique = true)
    private String gerritName;

    @Column(unique = true)
    private String jenkinsName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    private Set<Team> teams = new HashSet<>();


    @OneToMany(fetch = FetchType.EAGER)
    private Set<Commit> commits = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER)
    private Set<Build> builds = new HashSet<>();

    public Program(ProgramDTO dto) {
        setName(dto.getName());
        setGerritName(dto.getGerritName());
        setJenkinsName(dto.getJenkinsName());
    }


    @Override
    public String toString() {
        return String.format("Program: %s, Teams: %s",
                getName(), teams.stream().map(Team::getName).collect(Collectors.joining(", ")));
    }

    public boolean containsCommit(Commit commit) {
        return commits.stream().anyMatch(checkingCommit -> commit.getCommitId().startsWith(checkingCommit.getCommitId()));
    }

    public Commit getLatestCommit() {
        return commits.stream().max(Comparator.comparing(Commit::getTime)).orElse(null);
    }

    public boolean containsBuild(Build build) {
        return builds.stream().anyMatch(checkingBuild -> build.getBuildNumber() == checkingBuild.getBuildNumber());
    }

    public Build getLatestBuild() {
        return builds.stream().max(Comparator.comparing(Build::getBuildTimestamp)).orElse(null);
    }
}

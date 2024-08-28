package com.ericsson.graduate.maincontroller.model;

import com.ericsson.graduate.core.dto.TeamDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(exclude = {"members", "programs"})
@Data
@Entity
@Table(name = "teams")
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Column(unique = true, length = 64, nullable = false)
    @Length(max = 64)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "teams")
    private Set<Member> members = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "teams")
    @Getter(onMethod = @__(@JsonIgnore))
    private Set<Program> programs = new HashSet<>();

    public Team(TeamDTO dto) {
        setName(dto.getName());
    }

    @Override
    public String toString() {
        return String.format("Team: %s Members sig: %s , programs : %s", getName(),
                members.stream().map(Member::getSignum).collect(Collectors.joining(", ")),
                programs.stream().map(Program::getName).collect(Collectors.joining(", ")));
    }
}

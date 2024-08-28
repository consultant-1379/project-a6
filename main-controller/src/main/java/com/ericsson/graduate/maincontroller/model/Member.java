package com.ericsson.graduate.maincontroller.model;

import com.ericsson.graduate.core.dto.MemberDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@EqualsAndHashCode(exclude = {"teams"})
@Data
@Entity

@Table(name = "members")
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Column(unique = true)
    private String signum;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    @Getter(onMethod = @__(@JsonIgnore))
    private Set<Team> teams;

    public Member(MemberDTO dto) {
        setSignum(dto.getSignum());
        setFirstName(dto.getFirstName());
        setLastName(dto.getLastName());
    }

    @Override
    public String toString() {
        return "Name: " + firstName + " " + lastName + "/" + signum;
    }
}

package com.ericsson.graduate.maincontroller.repository;

import com.ericsson.graduate.maincontroller.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findTeamByName(@Param("name") String name);
}

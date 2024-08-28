package com.ericsson.graduate.maincontroller.repository;

import com.ericsson.graduate.maincontroller.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    Optional<Program> findProgramByName(@Param("name") String name);
}

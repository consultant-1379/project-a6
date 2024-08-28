package com.ericsson.graduate.maincontroller.repository;

import com.ericsson.graduate.maincontroller.model.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommitRepository extends JpaRepository<Commit, Long> {
    List<Commit> findAllCommitByAuthorEmail(@Param("authorEmail") String email);
}

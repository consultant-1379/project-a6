package com.ericsson.graduate.maincontroller.repository;

import com.ericsson.graduate.maincontroller.model.Build;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildRepository extends JpaRepository<Build, Long> {
}
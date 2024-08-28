package com.ericsson.graduate.maincontroller.repository;

import com.ericsson.graduate.maincontroller.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySignumIgnoreCase(@Param("signum") String signum);

    default List<Member> findAllBySignum(List<String> signums) {
        return signums.stream().map(this::findBySignumIgnoreCase).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
}

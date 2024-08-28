package com.ericsson.graduate.microservices.gerrit.data;

import com.ericsson.graduate.core.dto.CommitDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class GitLog {
    private List<Commit> log = new ArrayList<>();

    public List<CommitDTO> getCommits(String except) {
        return log.stream()
                .map(Commit::toDTO)
                .filter(commit -> except == null || !commit.getCommitId().startsWith(except))
                .sorted(Comparator.comparing(CommitDTO::getTime).reversed())
                .collect(Collectors.toList());
    }
}
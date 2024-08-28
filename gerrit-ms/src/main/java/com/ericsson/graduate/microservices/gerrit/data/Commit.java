package com.ericsson.graduate.microservices.gerrit.data;

import com.ericsson.graduate.core.dto.CommitDTO;
import lombok.Data;

@Data
public class Commit {
    private String commit;
    private String[] parents;
    private CommitAuthor author;
    private String message;

    public CommitDTO toDTO() {
        CommitDTO dto = new CommitDTO();
        dto.setCommitId(commit);
        dto.setParents(parents);
        dto.setMessage(message);
        dto.setAuthorName(author.getName());
        dto.setAuthorEmail(author.getEmail());
        dto.setTime(author.getTime());
        return dto;
    }
}

package com.priyanshu.RecoverAi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRecoveryAgentResponse {
    private String diagnosis;

    private String proposedAction;

    private String explanation;

    private List<String> nextSteps;

    private String confidenceLevel;
}

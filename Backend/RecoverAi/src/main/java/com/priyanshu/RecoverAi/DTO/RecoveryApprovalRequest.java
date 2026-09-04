package com.priyanshu.RecoverAi.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecoveryApprovalRequest {

    @NotBlank
    private String reviewedBy;

    private String reason;
}

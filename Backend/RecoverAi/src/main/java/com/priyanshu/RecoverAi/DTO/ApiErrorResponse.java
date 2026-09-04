package com.priyanshu.RecoverAi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {

    private Integer status;

    private String error;

    private String message;

    private String path;

    private LocalDateTime timestamp;
}

package com.stanbic.internMs.intern.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardAPIResponse {
    @Builder.Default //No need to explicitly set data in case an exception occurs
    Object data=null;

    @Builder.Default // No need to explicitly set errors in case the happy path is followed
    Object errors=null;

    final LocalDateTime timestamp=LocalDateTime.now();

    String message;
    boolean successful;
}

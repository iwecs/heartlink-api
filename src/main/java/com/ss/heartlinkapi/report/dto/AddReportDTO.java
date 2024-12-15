package com.ss.heartlinkapi.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AddReportDTO {
    private Long userId;
    private Long postId;
    private String reason;
}

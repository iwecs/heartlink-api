package com.ss.heartlinkapi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMatchDTO {
    private String match1;
    private String match2;
    private LocalDate displayDate;
}

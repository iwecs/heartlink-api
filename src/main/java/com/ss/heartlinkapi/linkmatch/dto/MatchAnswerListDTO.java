package com.ss.heartlinkapi.linkmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchAnswerListDTO {
    private String match1;
    private String match2;
    private LocalDate date;
    private int myChoice;
    private int partnerChoice;
}
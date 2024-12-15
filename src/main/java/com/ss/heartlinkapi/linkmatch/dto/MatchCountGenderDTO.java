package com.ss.heartlinkapi.linkmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCountGenderDTO {
    private Long linkMatchId;
    private String gender;
    private int choice;
    private int count;
}

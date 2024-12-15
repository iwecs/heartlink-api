package com.ss.heartlinkapi.linkmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchAnswer {
    private Long questionId;
    private int selectedOption;
}

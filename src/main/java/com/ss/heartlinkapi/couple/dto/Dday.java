package com.ss.heartlinkapi.couple.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dday {
    private Long coupleId;
    private LocalDate firstMetDate;
}

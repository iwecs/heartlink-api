package com.ss.heartlinkapi.linkmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchGenderRateDTO {
    private Integer choice0ByF; // 0번 선택지 고른 여성 인원 수
    private Integer choice1ByF; // 1번 선택지 고른 여성 인원 수
    private Integer choice0ByM; // 0번 선택지 고른 남성 인원 수
    private Integer choice1ByM; // 1번 선택지 고른 남성 인원 수
    private Integer totalFCount; // 매치에 선택한 모든 여성 인원 수
    private Integer totalMCount; // 매치에 선택한 모든 남성 인원 수
    private Integer select0RateF; // 0번 선택지 고른 여성 비율(%)
    private Integer select1RateF; // 1번 선택지 고른 여성 비율(%)
    private Integer select0RateM; // 0번 선택지 고른 남성 비율(%)
    private Integer select1RateM; // 1번 선택지 고른 남성 비율(%)
}

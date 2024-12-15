package com.ss.heartlinkapi.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkMissionDTO {
    private String missionTagName;
    private int year;
    private int month;
}

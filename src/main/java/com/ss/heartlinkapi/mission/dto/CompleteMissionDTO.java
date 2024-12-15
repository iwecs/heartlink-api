package com.ss.heartlinkapi.mission.dto;

import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMissionDTO {
    private Long missionId;
    private Long linkTagId;
    private String keyword;
    private Long postId;
    private String postImgUrl;
}

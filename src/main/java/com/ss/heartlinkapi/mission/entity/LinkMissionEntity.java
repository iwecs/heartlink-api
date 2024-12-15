package com.ss.heartlinkapi.mission.entity;

import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "link_mission")
public class LinkMissionEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkMissionId;
    @JoinColumn(name = "link_tag_id")
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private LinkTagEntity linkTagId;
    private LocalDate start_date;
    private LocalDate end_date;
}

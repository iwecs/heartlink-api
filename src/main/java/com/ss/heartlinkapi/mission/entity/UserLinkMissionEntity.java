package com.ss.heartlinkapi.mission.entity;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_link_mission")
public class UserLinkMissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLinkId;
    @JoinColumn(name = "couple_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private CoupleEntity coupleId;
    @JoinColumn(name = "link_mission_id")
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private LinkMissionEntity linkMissionId;
    private boolean status = false;


}

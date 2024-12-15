package com.ss.heartlinkapi.linkmatch.entity;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "match_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkMatchAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long LinkMatchAnswerId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "users_id")
    private UserEntity userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "couple_id")
    private CoupleEntity coupleId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "link_match_id")
    private LinkMatchEntity matchId;

    private int choice;
    private LocalDate createdAt;

}

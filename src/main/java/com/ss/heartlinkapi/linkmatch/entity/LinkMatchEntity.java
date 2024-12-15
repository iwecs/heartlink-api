package com.ss.heartlinkapi.linkmatch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "link_match")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkMatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long linkMatchId;
    private String match1;
    private String match2;
    @Column(name = "display_date")
    private LocalDate displayDate;

}

package com.ss.heartlinkapi.ads.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EbayProductDTO {
    private String title; // 상품명
    private String imgUrl; // 이미지 URL
    private String siteUrl; // 상품 페이지 URL
    private String currency; // 화폐 단위
    private double price; // 가격
    private int viewCount; // 조회 수
    private LocalDateTime searchTime;
}

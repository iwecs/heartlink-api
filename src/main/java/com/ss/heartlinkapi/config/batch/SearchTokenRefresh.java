package com.ss.heartlinkapi.config.batch;

import com.ss.heartlinkapi.ads.service.AdsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SearchTokenRefresh {

    private final AdsService adsService;

    public SearchTokenRefresh(AdsService adsService) {
        this.adsService = adsService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void searchTokenRefresh() {
        System.out.println("이베이 토큰 새로 발급");
        adsService.getAdsToken();
    }
}

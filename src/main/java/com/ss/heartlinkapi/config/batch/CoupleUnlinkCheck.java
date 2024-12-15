package com.ss.heartlinkapi.config.batch;

import com.ss.heartlinkapi.couple.service.CoupleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class CoupleUnlinkCheck {

    @Autowired
    private CoupleService coupleService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void CoupleUnlinkCheck() {
        coupleService.batchFinalUnlinkCouple();
    }
}

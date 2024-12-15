package com.ss.heartlinkapi.report.controller;

import com.ss.heartlinkapi.report.dto.AddReportDTO;
import com.ss.heartlinkapi.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> addReport(@RequestBody AddReportDTO addReportDTO) {

        reportService.addReport(addReportDTO);

        return ResponseEntity.ok("add report");
    }
}

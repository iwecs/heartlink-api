package com.ss.heartlinkapi.report.service;


import com.ss.heartlinkapi.comment.entity.CommentEntity;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.report.dto.AddReportDTO;
import com.ss.heartlinkapi.report.entity.Status;
import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.report.entity.ReportEntity;
import com.ss.heartlinkapi.report.repository.ReportRepository;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public void addReport(AddReportDTO addReportDTO) {

//		entity생성
		CommentEntity comment = new CommentEntity();
		PostEntity post = new PostEntity();
		UserEntity user = new UserEntity();

//		각 엔티티에 id값 설정
		post.setPostId(addReportDTO.getPostId());
		user.setUserId(addReportDTO.getUserId());

//		각 엔티티 reportEntity에 합치기
		ReportEntity reportEntity = new ReportEntity();
		reportEntity.setUserId(user);
		reportEntity.setPostId(post);
		reportEntity.setReason(addReportDTO.getReason());
		reportEntity.setStatus(Status.valueOf("AWAIT"));

//		저장
		reportRepository.save(reportEntity);
    }

//		신고 리스트 전체 불러오는 메서드
    public List<ReportEntity> getAllList() {
		return reportRepository.findAll();
    }

//		신고 반려 처분하는 메서드
	public void updateStatus(Long reportId) {
		reportRepository.updateStatus(reportId);
	}
}

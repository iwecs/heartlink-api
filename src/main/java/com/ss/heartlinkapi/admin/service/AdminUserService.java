package com.ss.heartlinkapi.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ss.heartlinkapi.admin.dto.AdminUserDTO;
import com.ss.heartlinkapi.comment.repository.CommentRepository;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.SocialEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.SocialRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class AdminUserService {

	private final UserRepository userRepository;
	private final CoupleService coupleService;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final SocialRepository socialRepository;

	public AdminUserService(UserRepository userRepository, CoupleService coupleService, PostRepository postRepository,
			CommentRepository commentRepository, SocialRepository socialRepository) {
		this.userRepository = userRepository;
		this.coupleService = coupleService;
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.socialRepository = socialRepository;
	}
	
	@Transactional(readOnly = true)
	public Page<AdminUserDTO> getAllUsers(Pageable pageable, Long userId, String loginId, LocalDateTime startDate,
			LocalDateTime endDate, String role) {

		Page<UserEntity> users;
		if (userId != null) {
			users = userRepository.findByUserId(userId, pageable);
		} else if (loginId != null) {
			users = userRepository.findByLoginIdContaining(loginId, pageable);
		} else if (startDate != null && endDate != null) {
			users = userRepository.findByCreatedAtBetween(startDate, endDate, pageable);
		} else if (role != null) {
			try {
				users = userRepository.findByRole(Role.valueOf(role), pageable);
			} catch (IllegalArgumentException e) {
				// 유효하지 않은 Role 값 처리 (예: 모든 사용자 반환하거나 에러 메시지 반환)
				users = userRepository.findAll(pageable);
			}
		} else {
			users = userRepository.findAll(pageable);
		}

		return users.map(user -> {
			AdminUserDTO dto = new AdminUserDTO();
			dto.setUserId(user.getUserId());
			dto.setLoginId(user.getLoginId());
			dto.setEmail(user.getEmail());
			dto.setName(user.getName());
			dto.setGender(user.getGender());
			dto.setPhone(user.getPhone());
			dto.setRole(user.getRole());
			dto.setCreatedAt(user.getCreatedAt());

			// 커플 유저 아이디
			// 디데이
			CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());
			if (couple != null) {
				UserEntity coupleUser = coupleService.getCouplePartner(user.getUserId());
				dto.setCoupleUserId(coupleUser.getUserId());
				dto.setDDay(coupleService.getDday(couple));
			}
			// 작성한 보드 수 가져오기
			dto.setPostCount(postRepository.countByUserId(user));
			// 작성한 댓글 수 가져오기
			dto.setCommentCount(commentRepository.countByUserId(user));
			// 연결된 소셜 계정 가져오기
			List<SocialEntity> socailEntities = socialRepository.findByUserEntity(user);
			List<String> connectedSocials = socailEntities.stream().map(SocialEntity::getProvider)
					.collect(Collectors.toList());
			dto.setConnectedSocails(connectedSocials);

			return dto;
		});
	}

}

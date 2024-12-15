package com.ss.heartlinkapi.follow.controller;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.follow.dto.FollowerDTO;
import com.ss.heartlinkapi.follow.dto.FollowingDTO;
import com.ss.heartlinkapi.follow.service.FollowService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.user.entity.UserEntity;

@RestController
@RequestMapping("/follow")
public class FollowController {

	private final FollowService followService;

	public FollowController(FollowService followService) {
		this.followService = followService;
	}

	/********** 팔로우 하기 **********/

	@PostMapping("/{userId}")
	public ResponseEntity<?> follow(@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity follower = loginUser.getUserEntity();
		UserEntity following = followService.findByUserId(userId);

		if (following == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		if(followService.isFollowing(follower, following)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 팔로우 중인 유저입니다.");
		}

		try {
			followService.follow(follower, following);
			return ResponseEntity.status(HttpStatus.CREATED).body("팔로우 성공");
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("커플 정보를 찾을 수 없습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 : " + e);
		}

	}

	/********** 회원이 팔로우한 유저 목록 보기 **********/
	@GetMapping("/following/{userId}")
	public ResponseEntity<?> getFollowing(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser, Pageable pageable) {
		Long loginUserId = loginUser.getUserId();
		try {
			Page<FollowingDTO> followingList = followService.getFollowingByUserId(userId, loginUserId, pageable);
			return ResponseEntity.ok(followingList);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저가 존재하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 : " + e);
		}
	}

	/********** 회원을 팔로우한 유저 목록 보기 **********/
	@GetMapping("/follower/{userId}")
	public ResponseEntity<?> getFollower(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser, Pageable pageable) {
		Long loginUserId = loginUser.getUserId();
		try {
			Page<FollowerDTO> followerList = followService.getFollowersByUserId(userId, loginUserId, pageable);
			return ResponseEntity.ok(followerList);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저가 존재하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러 : " + e);
		}
	}

	/********** 팔로우 취소하기 **********/

	@DeleteMapping("/cancel/{userId}")
	public ResponseEntity<?> cancelFollow(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity follower = loginUser.getUserEntity();
		UserEntity following = followService.findByUserId(userId);

		if (following == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		try {

			followService.unfollow(follower, following);

			return ResponseEntity.noContent().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("팔로우 관계가 존재하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}

	}

	/********** 팔로잉 목록에서 팔로워 삭제하기 **********/

	@DeleteMapping("/delete/{userId}")
	public ResponseEntity<?> deleteFollower(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity follower = followService.findByUserId(userId);
		UserEntity following = loginUser.getUserEntity();

		if (follower == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		try {

			followService.unfollow(follower, following);

			return ResponseEntity.noContent().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("팔로우 관계가 존재하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}

	}

	/********** 팔로우 요청 수락 **********/
	@PatchMapping("/accept/{userId}")
	public ResponseEntity<?> acceptRequest(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity following = loginUser.getUserEntity();
		UserEntity follower = followService.findByUserId(userId);

		if (follower == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		try {

			followService.acceptFollow(follower, following);

			return ResponseEntity.ok("팔로우 요청이 수락되었습니다.");

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("팔로우 요청이 존재하지 않습니다.");
		} catch (IllegalArgumentException e) {
			// 이미 수락된 요청인 경우
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}
	}

	/********** 팔로우 요청 거절 **********/

	@DeleteMapping("/reject/{userId}")
	public ResponseEntity<?> rejectRequest(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity following = loginUser.getUserEntity();
		UserEntity follower = followService.findByUserId(userId);

		if (follower == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		try {
			followService.rejectFollow(follower, following);
			
			return ResponseEntity.noContent().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("팔로우 요청이 존재하지 않습니다.");
		} catch (IllegalArgumentException e) {
			// 이미 수락된 요청인 경우
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}
	}

}

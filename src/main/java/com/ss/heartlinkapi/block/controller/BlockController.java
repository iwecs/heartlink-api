package com.ss.heartlinkapi.block.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.block.dto.BlockListDTO;
import com.ss.heartlinkapi.block.service.BlockService;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.user.entity.UserEntity;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/user/block")
public class BlockController {

	private final BlockService blockService;
	private final CoupleService coupleService;

	public BlockController(BlockService blockService, CoupleService coupleService) {
		this.blockService = blockService;
		this.coupleService = coupleService;
	}

	/********** 차단하기 **********/
	@PostMapping("{blockedUserId}")
	public ResponseEntity<?> blockUser(@PathVariable Long blockedUserId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity blockerUser = loginUser.getUserEntity();
		UserEntity blockedUser = blockService.findByUserId(blockedUserId);

		if (blockedUser == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		CoupleEntity blockerCouple = coupleService.findCoupleEntity(blockerUser);
		CoupleEntity blockedCouple = coupleService.findCoupleEntity(blockedUser);

		if (blockerCouple != null && blockerCouple.getCoupleId().equals(blockedCouple.getCoupleId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("커플은 차단할 수 없습니다.");
		}

		try {
			blockService.blockUser(blockerUser, blockedUser, blockedCouple);
			return ResponseEntity.status(HttpStatus.CREATED).body("유저가 차단되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}

	}

	/********** 차단 해제 **********/
	@DeleteMapping("cancel/{blockedUserId}")
	public ResponseEntity<?> unBlockUser(@PathVariable Long blockedUserId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity blocker = loginUser.getUserEntity();
		UserEntity blocked = blockService.findByUserId(blockedUserId);

		if (blocked == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		try {

			blockService.unblockUser(blocker, blocked);
			return ResponseEntity.noContent().build();

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("차단된 유저를 찾을 수 없습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}
	}

	/********** 차단한 유저 목록 가져오기 **********/

	@GetMapping("/list")
	public ResponseEntity<?> getBlockedUsers(@AuthenticationPrincipal CustomUserDetails loginUser, Pageable pageable) {

		UserEntity blocker = loginUser.getUserEntity();
		try {
			Page<BlockListDTO> blockedUsers = blockService.getBlockedUsers(blocker, pageable);
			return ResponseEntity.ok(blockedUsers);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e);
		}

	}

}

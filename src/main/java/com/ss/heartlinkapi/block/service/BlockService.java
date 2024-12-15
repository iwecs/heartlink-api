package com.ss.heartlinkapi.block.service;

import javax.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.block.dto.BlockListDTO;
import com.ss.heartlinkapi.block.entity.BlockEntity;
import com.ss.heartlinkapi.block.repository.BlockRepository;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class BlockService {

	private final BlockRepository blockRepository;
	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;

	public BlockService(BlockRepository blockRepository, ProfileRepository profileRepository,
			UserRepository userRepository) {
		this.blockRepository = blockRepository;
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
	}

	public boolean isUserBlockedByCouple(UserEntity blocker, CoupleEntity couple) {
		return blockRepository.existsByBlockerIdAndCoupleId(blocker, couple);
	}
	
	/************* 차단 **************/
	@Transactional
	public void blockUser(UserEntity blockerUser, UserEntity blockedUser, CoupleEntity blockedCouple) {
		
	    if (blockRepository.existsByBlockerIdAndBlockedId(blockerUser, blockedUser)) {
	        throw new IllegalArgumentException("이미 차단된 사용자입니다.");
	    }
		
		BlockEntity blockEntity = new BlockEntity();
		blockEntity.setBlockedId(blockedUser);
		blockEntity.setBlockerId(blockerUser);
		blockEntity.setCoupleId(blockedCouple);
		
		blockRepository.save(blockEntity);
	}
	
	
	/************* 유저 아이디로 유저 엔티티 반환 **************/
	public UserEntity findByUserId(Long userId) {	
		return userRepository.findById(userId).orElse(null);
	}
	
	/************* 차단한 회원 리스트 불러오는 서비스 **************/
	@Transactional(readOnly = true)
	public Page<BlockListDTO> getBlockedUsers(UserEntity blocker, Pageable pageable){
		
		Page<BlockEntity> blockEntities = blockRepository.findByBlockerId(blocker, pageable);
		
		return blockEntities.map(block -> {
			
			BlockListDTO dto = new BlockListDTO();
			dto.setBlockerUserId(blocker.getUserId());
			dto.setBlockedUserId(block.getBlockedId().getUserId());
			dto.setBlockedLoginId(block.getBlockedId().getLoginId());
			
			ProfileEntity blockedProfile = profileRepository.findByUserEntity(block.getBlockedId());	
			dto.setBlockedImg(blockedProfile.getProfile_img());
			dto.setBlockedBio(blockedProfile.getBio());
			
			return dto;
		});
	}
	/************* 차단 해제 **************/
	@Transactional
	public void unblockUser(UserEntity blocker, UserEntity blocked) {

		BlockEntity blockEntity = blockRepository.findByBlockedIdAndBlockerId(blocked, blocker);
		
		if(blockEntity == null) {
			throw new EntityNotFoundException("차단된 유저가 존재하지 않습니다.");
		}
		
		blockRepository.delete(blockEntity);
	}

}

package com.ss.heartlinkapi.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileDTO {
	private String userimg;
	private String pairimg;
	private String bio;
	private String loginId;
	private String nickname;
	private int followerCount;
	private int followingCount;
	private boolean isFollowed; // 현재 로그인한 사용자가 해당 유저를 팔로우하고 있는지
	private boolean followStatus; // 팔로우 상태 (대기 중인지 아닌지)
	private Long coupleUserId;
	private boolean isPrivate;
}

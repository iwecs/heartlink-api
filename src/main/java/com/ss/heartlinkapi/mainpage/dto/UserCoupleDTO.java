package com.ss.heartlinkapi.mainpage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCoupleDTO {
	private Long coupleUserId;
	private String coupleImg;
	private String coupleNickname;
	private String coupleBio;
}

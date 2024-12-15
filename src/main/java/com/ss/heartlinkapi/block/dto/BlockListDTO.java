package com.ss.heartlinkapi.block.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockListDTO {
	private Long blockerUserId;
	private Long blockedUserId;
	private String blockedLoginId;
	private String blockedImg;
	private String blockedBio;
}

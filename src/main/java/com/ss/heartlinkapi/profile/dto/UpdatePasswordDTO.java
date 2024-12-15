package com.ss.heartlinkapi.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDTO {
	private String beforePassword;
	private String afterPassword;
}

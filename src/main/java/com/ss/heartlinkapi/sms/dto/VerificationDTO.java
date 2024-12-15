package com.ss.heartlinkapi.sms.dto;

import lombok.Data;

@Data
public class VerificationDTO {
	private String phone;
	private String code;
}

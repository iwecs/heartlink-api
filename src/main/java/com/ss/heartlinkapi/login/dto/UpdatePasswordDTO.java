package com.ss.heartlinkapi.login.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDTO {
	
    private String phone;
    private String loginId;
    private String password; //변경할 비밀번호
}

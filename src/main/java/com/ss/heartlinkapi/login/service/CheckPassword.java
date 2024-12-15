package com.ss.heartlinkapi.login.service;

public final class CheckPassword {
    
	private static final String PASSWORD_REGEX = "^(?!.*[가-힣])(?=.*[a-zA-Z])(?=.*\\d)(?=.*(_|[^\\w])).{8,16}$";

    private CheckPassword() {
    }

    public static boolean isPasswordValid(String password) {
        return password.matches(PASSWORD_REGEX);
    }
    
}
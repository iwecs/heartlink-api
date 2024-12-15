package com.ss.heartlinkapi.login.service;

import java.security.SecureRandom;

public class CoupleCode {
	
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int CODE_LENGTH = 6;
	
	public static String generateRandomCode() {
	    SecureRandom random = new SecureRandom();
	    StringBuilder code = new StringBuilder(CODE_LENGTH);
	    int charactersLength = CHARACTERS.length();
	    for (int i = 0; i < CODE_LENGTH; i++) {
	        int index = random.nextInt(charactersLength);
	        code.append(CHARACTERS.charAt(index));
	    }
	    return code.toString();
	}
}

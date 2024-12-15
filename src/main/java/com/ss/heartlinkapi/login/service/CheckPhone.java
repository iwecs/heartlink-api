package com.ss.heartlinkapi.login.service;

public final class CheckPhone {

    private static final String PHONE_REGEX = "^\\d{2,3}-\\d{3,4}-\\d{4}$";

    private CheckPhone() {
    }

    public static boolean isPhoneValid(String phone) {
        return phone.matches(PHONE_REGEX);
    }
}
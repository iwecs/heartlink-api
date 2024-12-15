package com.ss.heartlinkapi.oauth2.jwt;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		 System.out.println("Authentication failed: " + exception.getMessage());
		String errorMessage = exception.getMessage();
		String providerId = null;
		String errorType = "general";

		// OAuth2AuthenticationException인 경우, 오류 메시지 추출
		if (exception instanceof OAuth2AuthenticationException) {
			OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
			OAuth2Error oauthError = oauthException.getError();
			errorMessage = oauthError.getDescription();
			providerId = oauthError.getErrorCode();
		}

		// 오류 메시지를 분석하여 세부적인 오류 타입을 설정
		if (errorMessage != null) {
			if (errorMessage.contains("전화번호를 입력받아야 합니다")) {
				errorType = "phone"; // 전화번호 오류
			} else if (errorMessage.contains("아이디를 입력받아야 합니다")) {
				errorType = "loginId"; // 로그인 아이디 오류
			}
		}
		System.out.println(providerId);
	    String redirectUri = "http://localhost:3000/login";
		String redirectUrl = redirectUri + "?providerId=" + providerId + "&errorType=" + errorType;
		response.sendRedirect(redirectUrl);
	}
}

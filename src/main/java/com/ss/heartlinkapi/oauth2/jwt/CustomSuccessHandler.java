package com.ss.heartlinkapi.oauth2.jwt;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ss.heartlinkapi.login.jwt.JWTUtil;
import com.ss.heartlinkapi.login.service.RefreshTokenService;
import com.ss.heartlinkapi.oauth2.dto.CustomOAuth2User;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{
	
	private final JWTUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	public CustomSuccessHandler(JWTUtil jwtUtil, RefreshTokenService refreshTokenService) {
		this.jwtUtil = jwtUtil;
		this.refreshTokenService = refreshTokenService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
		
		String loginId = customUserDetails.getLoginId();

		String role = null;
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		if(!authorities.isEmpty()) {
			GrantedAuthority authority = authorities.iterator().next();
			role = authority.getAuthority();
		}
		
		String token = jwtUtil.createJwt("refresh", loginId, role, 86400000L);
		
		// 리프레쉬 토큰 디비 저장
		 refreshTokenService.saveRefreshToken(loginId, token);
		
		// 리프레쉬 쿠키에 추가
		response.addCookie(createCookie("RefreshToken", token));
		response.sendRedirect("http://localhost:3000/refresh");
		
	}
	
	// 쿠키 생성
	private Cookie createCookie(String key,String value) {
		
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(60*60*60);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		
		return cookie;
	}
}

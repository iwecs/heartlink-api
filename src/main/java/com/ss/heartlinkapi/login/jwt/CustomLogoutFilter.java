package com.ss.heartlinkapi.login.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.ss.heartlinkapi.login.service.CustomLogoutService;

import io.jsonwebtoken.ExpiredJwtException;

public class CustomLogoutFilter extends GenericFilterBean {

	private final CustomLogoutService customLogoutService;

	public CustomLogoutFilter(CustomLogoutService customLogoutService) {
		this.customLogoutService = customLogoutService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		doFilterInternal((HttpServletRequest) request, (HttpServletResponse) response, chain);

	}

	private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		// 경로 확인
		String requestUri = request.getRequestURI();

		if (!requestUri.matches("^\\/logout$")) {
			filterChain.doFilter(request, response);
			return;
		}

		String requestMethod = request.getMethod();
		if (!requestMethod.equals("POST")) {
			filterChain.doFilter(request, response);
			return;
		}

		// refresh 토큰 확인
		String refresh = request.getHeader("RefreshToken");

		// null 체크
		if (refresh == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			customLogoutService.logout(refresh);
		} catch (ExpiredJwtException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\": \"이미 로그아웃되었습니다.\"}");
			return;
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\": \"" + e.getMessage() + "\"}");
			return;
		}
		
		//로그아웃 성공
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().write("{\"message\": \"로그아웃되었습니다.\"}");
	}
}

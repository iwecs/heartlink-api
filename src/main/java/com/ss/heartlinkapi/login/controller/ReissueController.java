package com.ss.heartlinkapi.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.heartlinkapi.login.service.ReissueService;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ReissueController {

	private final ReissueService reissueService;

	public ReissueController(ReissueService reissueService) {
		this.reissueService = reissueService;
	}

	@PostMapping("/reissue")
	public void reissue(@RequestHeader(value = "RefreshToken", required = false) String refreshToken, HttpServletRequest request, HttpServletResponse response) {
		
		String cookieRefreshToken = null;
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(Cookie cookie:cookies) {
				if("RefreshToken".equals(cookie.getName())) {
					cookieRefreshToken = cookie.getValue();
					break;
				}
			}
		}
		if(refreshToken == null) {
			refreshToken = cookieRefreshToken;
		}
		
		ResponseEntity<Map<String, String>> result = reissueService.reissueToken(refreshToken);
		response.setStatus(result.getStatusCodeValue());
		response.setContentType("application/json");

		try {
			if (result.getStatusCode() == HttpStatus.OK) {
				response.setHeader("Authorization", result.getBody().get("accessToken"));
				response.setHeader("RefreshToken", result.getBody().get("refreshToken"));
				response.getWriter().write(new ObjectMapper().writeValueAsString(result.getBody()));
			} else {
				response.getWriter().write(new ObjectMapper().writeValueAsString(result.getBody()));
			}
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("{\"error\": \"Internal Server Error\"}");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}

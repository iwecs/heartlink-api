package com.ss.heartlinkapi.login.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.login.service.JWTService;

public class JWTFilter extends OncePerRequestFilter{
	
	private final JWTService jwtService;

	public JWTFilter(JWTService jwtService) {
		this.jwtService = jwtService;
	}


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// header token 확인
		String accessToken = request.getHeader("Authorization");
		if(accessToken == null || !accessToken.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = accessToken.split(" ")[1];

		CustomUserDetails customUserDetails;
		try {
			customUserDetails = jwtService.validateToken(token);
			Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails,null,customUserDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authToken);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return;
		}
		
		filterChain.doFilter(request, response);		
	}

}

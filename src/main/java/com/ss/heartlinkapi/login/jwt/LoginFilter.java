package com.ss.heartlinkapi.login.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.login.dto.LoginDTO;
import com.ss.heartlinkapi.login.service.CustomUserDetailsService;
import com.ss.heartlinkapi.login.service.RefreshTokenService;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
	
	private final CustomUserDetailsService customUserDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	public LoginFilter(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshTokenService refreshTokenService) {
		this.customUserDetailsService = customUserDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.refreshTokenService = refreshTokenService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		LoginDTO loginDTO = new LoginDTO();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ServletInputStream inputStream = request.getInputStream();
			String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			loginDTO = objectMapper.readValue(body, LoginDTO.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String loginId = loginDTO.getLoginId();
		String password = loginDTO.getPassword();
		
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId);
		
		if(userDetails instanceof CustomUserDetails) {
			CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
			if(!passwordEncoder.matches(password, customUserDetails.getPassword())) {
				throw new BadCredentialsException("비밀번호 불일치");
			}
			Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, password, authorities);
			return authenticationManager.authenticate(authToken);
		}else {
			throw new UsernameNotFoundException("UsernameNotFound : "+loginId);
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {
		
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		String loginId = customUserDetails.getUsername();
		String role = null;
		Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
		if(!authorities.isEmpty()) {
			GrantedAuthority authority = authorities.iterator().next();
			role = authority.getAuthority();
		}
		
		 String access = jwtUtil.createJwt("access", loginId, role, 600000L);
		 String refresh = jwtUtil.createJwt("refresh", loginId, role, 86400000L);
		 
		 refreshTokenService.saveRefreshToken(loginId, refresh);
		 
		 response.setHeader("Authorization","Bearer "+ access);
		 response.setHeader("RefreshToken", refresh);	 
		 response.setHeader("Access-Control-Expose-Headers", "Authorization, RefreshToken");
		 
		 response.setStatus(HttpStatus.OK.value());

	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		response.setStatus(401);
	}
	

}

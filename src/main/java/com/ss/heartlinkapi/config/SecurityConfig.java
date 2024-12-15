package com.ss.heartlinkapi.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ss.heartlinkapi.login.jwt.CustomLogoutFilter;
import com.ss.heartlinkapi.login.jwt.JWTFilter;
import com.ss.heartlinkapi.login.jwt.JWTUtil;
import com.ss.heartlinkapi.login.jwt.LoginFilter;
import com.ss.heartlinkapi.login.service.CustomLogoutService;
import com.ss.heartlinkapi.login.service.CustomUserDetailsService;
import com.ss.heartlinkapi.login.service.JWTService;
import com.ss.heartlinkapi.login.service.RefreshTokenService;
import com.ss.heartlinkapi.oauth2.jwt.CustomFailureHandler;
import com.ss.heartlinkapi.oauth2.jwt.CustomSuccessHandler;
import com.ss.heartlinkapi.oauth2.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final AuthenticationConfiguration authenticationConfiguration;
	private final CustomUserDetailsService customUserDetailsService;
	private final JWTUtil jwtUtil;
	private final JWTService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final CustomLogoutService customLogoutService;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomFailureHandler customFailureHandler;

	public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
			CustomUserDetailsService customUserDetailsService, JWTUtil jwtUtil, JWTService jwtService,
			RefreshTokenService refreshTokenService, CustomLogoutService customLogoutService,
			CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler,
			CustomFailureHandler customFailureHandler) {
		this.authenticationConfiguration = authenticationConfiguration;
		this.customUserDetailsService = customUserDetailsService;
		this.jwtUtil = jwtUtil;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.customLogoutService = customLogoutService;
		this.customOAuth2UserService = customOAuth2UserService;
		this.customSuccessHandler = customSuccessHandler;
		this.customFailureHandler = customFailureHandler;
	}

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                		//user
				.authorizeHttpRequests(auth -> auth
						.anyRequest().permitAll());
//						.antMatchers("/user/join").permitAll()
//						.antMatchers("/user/idcheck").permitAll()
//						.antMatchers("/user/account/linking").permitAll()
//						.antMatchers("/user/auth/**").permitAll()
//						.antMatchers("/user/sms/**").permitAll()
//						.antMatchers("/oauth2/**").permitAll()
//						.antMatchers("/login/**").permitAll()
//						.antMatchers("/reissue").permitAll()
//						.antMatchers("/user/find/loginId").permitAll()
//						.antMatchers("/user/update/password").permitAll()
//						.antMatchers("/user/profile/**").hasAnyRole("SINGLE","COUPLE", "ADMIN")
//						.antMatchers("/user/block/**").hasAnyRole("COUPLE", "ADMIN")
//						.antMatchers("/user/couple").hasAnyRole("COUPLE","ADMIN","SINGLE","USER")
//						//follow
//						.antMatchers("/follow/**").hasAnyRole("COUPLE", "ADMIN","SINGLE")
//						//couple
//						.antMatchers("/couple/dday").permitAll()
//						.antMatchers("/couple/unlink").hasRole("COUPLE")
//						.antMatchers("/couple/unlink/cancel").hasRole("SINGLE")
//						.antMatchers("/couple/finalNowUnlink").hasRole("SINGLE")
//						.antMatchers("/couple/match/code/**").hasRole("USER")
//						.antMatchers("/couple/**").hasAnyRole("COUPLE", "ADMIN", "SINGLE")
//						//post
//						.antMatchers("/feed/**").hasAnyRole("COUPLE","SINGLE","ADMIN")
//						.antMatchers("/comment/**").hasAnyRole("COUPLE","ADMIN")
//						.antMatchers("/like/**").hasAnyRole("COUPLE","SINGLE","ADMIN")
//						.antMatchers("/bookmark/**").hasAnyRole("COUPLE","SINGLE","ADMIN")
//						.antMatchers("/tag/**").hasAnyRole("COUPLE","ADMIN")
//						//message
//						.antMatchers("/dm/**").hasAnyRole("COUPLE","ADMIN")
//						.antMatchers("/message").hasAnyRole("COUPLE","ADMIN")
        				//.antMatchers("/notifications/subscribe/**").permitAll()
//						//report
//						.antMatchers("/report").hasAnyRole("COUPLE","ADMIN")
//						//notification
//						.antMatchers("/notifications/**").hasAnyRole("COUPLE","SINGLE","ADMIN")
//						//search
//						.antMatchers("/search/**").hasAnyRole("COUPLE","ADMIN")
//						.antMatchers("/es/**").hasAnyRole("COUPLE","ADMIN","USER","SINGLE")
//						//ad
//						.antMatchers("/ads/**").hasAnyRole("COUPLE","ADMIN","SINGLE")
//						//admin
//						.antMatchers("/admin/**").hasRole("ADMIN")
//						//img
//						.antMatchers("/img/**","/images/**").permitAll()
//						.anyRequest().authenticated());
        // oauth2
        http.oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(customOAuth2UserService)).successHandler(customSuccessHandler).failureHandler(customFailureHandler));
        // login
        http.addFilterAt(new LoginFilter(customUserDetailsService, bCryptPasswordEncoder(), authenticationManager(),jwtUtil,refreshTokenService), UsernamePasswordAuthenticationFilter.class);
        // JWTFilter
        http.addFilterBefore(new JWTFilter(jwtService), LoginFilter.class);
        // logout
        http.addFilterBefore(new CustomLogoutFilter(customLogoutService), LogoutFilter.class);
			return http.build();	
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		
		CorsConfiguration configuration = new CorsConfiguration();
		
		configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
		configuration.setAllowedMethods(Collections.singletonList("*"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setMaxAge(3600L);
		configuration.setExposedHeaders(Collections.singletonList("Authorization"));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    
	    return source;
	}
	
}

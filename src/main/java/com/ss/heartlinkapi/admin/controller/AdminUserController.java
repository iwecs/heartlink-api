package com.ss.heartlinkapi.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.admin.dto.AdminUserDTO;
import com.ss.heartlinkapi.admin.service.AdminUserService;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/admin/user")
public class AdminUserController {
	
	private final AdminUserService adminUserService;
	
	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	/************* 관리자 페이지에서 회원 조회 *************/
	@GetMapping("/list")
	public ResponseEntity<Page<AdminUserDTO>> getUserList(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "30") int size,
	        @RequestParam(defaultValue = "createdAt") String sortBy,
	        @RequestParam(defaultValue = "desc") String direction,
	        @RequestParam(required = false) Long userId,
	        @RequestParam(required = false) String loginId,
	        @RequestParam(required = false) 
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
	        @RequestParam(required = false) 
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
	        @RequestParam(required = false) String role) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
	    Page<AdminUserDTO> userList = adminUserService.getAllUsers(pageable, userId, loginId, startDate, endDate, role);
	    
	    return ResponseEntity.ok(userList);
	}
	
	
	
}

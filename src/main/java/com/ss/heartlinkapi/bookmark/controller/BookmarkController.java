package com.ss.heartlinkapi.bookmark.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.bookmark.service.BookmarkService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;

@RestController
@RequestMapping("/bookmark")
public class BookmarkController {
	
	private final BookmarkService bookmarkService;
	
	public BookmarkController(BookmarkService bookmarkService) {
		this.bookmarkService = bookmarkService;
	}
	
	@PostMapping("/{postId}")
	public ResponseEntity<String> toggleBookmark(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails user) {
	    Long userId = user.getUserId();
	    boolean result = bookmarkService.addOrRemoveBookmark(postId, userId);

	    return ResponseEntity.ok(result ? "북마크 추가됨" : "북마크 삭제됨");
	}

	

}

package com.ss.heartlinkapi.bookmark.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.bookmark.entity.BookmarkEntity;
import com.ss.heartlinkapi.bookmark.repository.BookmarkRepository;
import com.ss.heartlinkapi.post.dto.PostFileDTO;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.entity.PostFileEntity;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class BookmarkService {
	
	private final BookmarkRepository bookmarkRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	
	public BookmarkService(BookmarkRepository bookmarkRepository, PostRepository postRepository, UserRepository userRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}
	
	// 내가 누른 북마크 목록 조회
	public Map<String, Object> getBookmarkPostFilesByUserId(Long userId, Integer cursor, int limit) {
	    List<PostFileEntity> allPostFiles = bookmarkRepository.findBookmarkPostFilesByUserId(userId); // 전체 데이터를 가져옴

	    // 커서가 없는 경우 (처음 페이지)
	    if (cursor == null) cursor = 0;

	    // 요청된 커서 위치부터 limit만큼 자르기
	    int endIndex = Math.min(cursor + limit, allPostFiles.size());
	    List<PostFileEntity> sliceData = allPostFiles.subList(cursor, endIndex);

	    // PostFileDTO로 변환
	    List<PostFileDTO> postFileDTOs = sliceData.stream()
	            .map(file -> new PostFileDTO(
	                    file.getPostId().getPostId(),
	                    file.getFileUrl(),
	                    file.getFileType(),
	                    file.getSortOrder()
	            ))
	            .collect(Collectors.toList());

	    // 다음 커서를 계산
	    Integer nextCursor = (endIndex < allPostFiles.size()) ? endIndex : null;

	    // 응답 데이터 생성
	    Map<String, Object> response = new HashMap<>();
	    response.put("data", postFileDTOs);
	    response.put("nextCursor", nextCursor);
	    response.put("hasNext", nextCursor != null);

	    return response;
	}


	
	// 북마크 추가, 삭제
	@Transactional
	public boolean addOrRemoveBookmark(Long postId, Long userId) {
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		
		// 게시글 엔티티 가져오기
		PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("Post not found"));
		
		// 중복 좋아요 여부 확인
		Optional<BookmarkEntity> existingBookmark = bookmarkRepository.findByUserIdAndPostId(user, post);
		
		// 이미 북마크를 누른 상태일 경우 삭제
		if(existingBookmark.isPresent()) {
			bookmarkRepository.delete(existingBookmark.get());
			return false;
		} else {
			BookmarkEntity bookmark = new BookmarkEntity();
			bookmark.setUserId(user);
			bookmark.setPostId(post);
			bookmarkRepository.save(bookmark);
			return true;
		}
		
		
	}

}

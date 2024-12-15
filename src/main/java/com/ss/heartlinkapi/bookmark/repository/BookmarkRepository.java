package com.ss.heartlinkapi.bookmark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ss.heartlinkapi.bookmark.entity.BookmarkEntity;
import com.ss.heartlinkapi.like.entity.LikeEntity;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.entity.PostFileEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long>{
	
	// 내가 누른 북마크 목록 조회
    @Query("SELECT pf " +
           "FROM BookmarkEntity b " +
           "JOIN b.postId p " +
           "JOIN PostFileEntity pf ON pf.postId = p AND pf.sortOrder = 1 " +
           "WHERE b.userId.id = :userId")
    List<PostFileEntity> findBookmarkPostFilesByUserId(@Param("userId") Long userId);

	Optional<BookmarkEntity> findByUserIdAndPostId(UserEntity user, PostEntity post);

	
	// 게시글 좋아요 상태 확인
    @Query("SELECT b FROM BookmarkEntity b WHERE b.userId.userId = :userId AND b.postId.postId = :postId")
	Optional<BookmarkEntity> findByUserIdAndPostIdUsingQuery(@Param("userId") Long userId, @Param("postId") Long postId);

}

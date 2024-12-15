package com.ss.heartlinkapi.post.repository;

import java.util.List;
import java.util.Optional;

import com.ss.heartlinkapi.post.dto.PostFileDTO;
import com.ss.heartlinkapi.post.dto.PostSearchDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ss.heartlinkapi.post.entity.PostFileEntity;

public interface PostFileRepository extends JpaRepository<PostFileEntity, Long>{
	
	// 게시글 첨부파일 가져오기
	@Query("SELECT pf FROM PostFileEntity pf WHERE pf.postId.id = :postId")
	List<PostFileEntity> findByPostId(@Param("postId") Long postId);

	
	// 사용자와 사용자의 커플 게시글 목록 가져오기
	@Query("SELECT pf FROM PostFileEntity pf JOIN PostEntity p ON pf.postId = p.postId " +
	           "WHERE pf.sortOrder = 1 AND p.userId.userId = :userId " +
	           "ORDER BY p.createdAt DESC")
	List<PostFileEntity> findPostFilesByUserId(@Param("userId") Long userId);

	// 게시글 수정 시 해당하는 게시글의 PostFile 데이터 모두 삭제
	@Modifying
	@Query("DELETE FROM PostFileEntity pf WHERE pf.postId.id = :postId")
	void deleteByPostId(@Param("postId") Long postId);

	// 입력한 postId의 sortOrder가 1인 데이터 가져오기
	@Query("SELECT pf FROM PostFileEntity pf WHERE pf.postId.postId = :postId AND pf.sortOrder = 1")
    List<PostFileEntity> findByPostIdAndSortOrder(@Param("postId") Long postId);

}

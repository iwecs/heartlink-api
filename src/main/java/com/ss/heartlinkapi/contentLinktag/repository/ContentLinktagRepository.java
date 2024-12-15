package com.ss.heartlinkapi.contentLinktag.repository;

import com.ss.heartlinkapi.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ss.heartlinkapi.comment.entity.CommentEntity;
import com.ss.heartlinkapi.contentLinktag.entity.ContentLinktagEntity;

import java.util.List;

public interface ContentLinktagRepository extends JpaRepository<ContentLinktagEntity, Long>{

	// 댓글 수정 시 링크태그 데이터 삭제
	void deleteByCommentId(CommentEntity comment);

	// 게시글 아이디로 조회
	List<ContentLinktagEntity> findByBoardId(PostEntity boardId);

	// 게시글 수정 시 링크태그 데이터 삭제
	void deleteByBoardId(PostEntity post);

}

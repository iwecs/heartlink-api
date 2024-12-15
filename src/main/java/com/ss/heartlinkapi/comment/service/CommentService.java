package com.ss.heartlinkapi.comment.service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.comment.dto.CommentDTO;
import com.ss.heartlinkapi.comment.dto.CommentUpdateDTO;
import com.ss.heartlinkapi.comment.entity.CommentEntity;
import com.ss.heartlinkapi.comment.repository.CommentRepository;
import com.ss.heartlinkapi.contentLinktag.entity.ContentLinktagEntity;
import com.ss.heartlinkapi.contentLinktag.repository.ContentLinktagRepository;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import com.ss.heartlinkapi.mention.entity.MentionEntity;
import com.ss.heartlinkapi.mention.repository.MentionRepository;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class CommentService {
	
	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final ContentLinktagRepository contentLinktagRepository;
	private final LinkTagRepository linkTagRepository;
	private final MentionRepository mentionRepository;
	private final ElasticService elasticService;

	public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, ContentLinktagRepository contentLinktagRepository, LinkTagRepository linkTagRepository, MentionRepository mentionRepository, ElasticService elasticService) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.contentLinktagRepository = contentLinktagRepository;
		this.linkTagRepository = linkTagRepository;
		this.mentionRepository = mentionRepository;
		this.elasticService = elasticService;
	}
	
	// 댓글 작성
	@Transactional
	public void writeComment(CommentDTO commentDTO, UserEntity user) {
		
		// 댓글 내용 유효성 검사
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        
        // PostEntity 가져오기
        PostEntity post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		
		CommentEntity comment = new CommentEntity();
		comment.setPostId(post);
		comment.setUserId(user);
		comment.setContent(commentDTO.getContent());
		
		// 댓글이 대댓글일 경우
		if(commentDTO.getParentId() != null) {
			CommentEntity parentComment = commentRepository.findById(commentDTO.getParentId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
			
			comment.setParentId(parentComment);
		} else {
			comment.setParentId(null);
		}
		
		commentRepository.save(comment);
		
		// 댓글 수 증가
		post.setCommentCount(post.getCommentCount() + 1);
		postRepository.save(post);
		
		// 태그 처리
		processTags(commentDTO.getContent(), comment);
				
	}
	
	// 댓글 작성 시 태그 처리
	@Transactional
	private void processTags(String content, CommentEntity comment) {
		
		// 아이디 태그 처리
		Pattern userPattern = Pattern.compile("@(\\w+)");
		Matcher userMatcher = userPattern.matcher(content);
		
		// 아이디 태그 있을 경우
		while(userMatcher.find()) {
			String username = userMatcher.group(1); // @ 생략
			UserEntity user = userRepository.findByLoginId(username);
			
			// 해당 유저 있을 경우
	        if(user != null) {
	            // 기타 처리(알림)
	            System.out.println("아이디 태그 처리: " + username);
	            
	            MentionEntity mention = new MentionEntity();
	            mention.setUserId(user);
	            mention.setCommentId(comment);
	         
	            mentionRepository.save(mention);
	        } else {
	            System.out.println("아이디 태그 처리 실패: " + username + "는 존재하지 않는 사용자입니다.");
	        }
		}
		
		// 링크태그 처리
		Pattern linktagPattern = Pattern.compile("&([\\w가-힣_]+)");
		Matcher linktagMatcher = linktagPattern.matcher(content);
		
		List<ContentLinktagEntity> contentLinktags = new ArrayList<>();
		
		// 링크태그 있을 경우
		while(linktagMatcher.find()) {
			String keyword = linktagMatcher.group(1); // & 생략
			LinkTagEntity linkTag = linkTagRepository.findByKeyword(keyword)
					.orElseGet(() -> new LinkTagEntity(null, keyword));
			LinkTagEntity result = linkTagRepository.save(linkTag);
			// 엘라스틱에 새 태그 추가
//			if(elasticService.addTag(result)==null) {
//				System.out.println("엘라스틱 태그 저장 실패");
//			}
			
			ContentLinktagEntity contentLinktag = new ContentLinktagEntity();
			contentLinktag.setLinktagId(linkTag);
			contentLinktag.setCommentId(comment);
			contentLinktags.add(contentLinktag);
		}
		
		contentLinktagRepository.saveAll(contentLinktags);
		
	}
	
	// 댓글 삭제
	public void deleteComment(Long commentId, Long userId) {
	    
	    System.out.println("Deleting comment with ID: " + commentId + " for user ID: " + userId);
	    CommentEntity comment = commentRepository.findByCommentIdAndUserId_UserId(commentId, userId);
	    
	    if (comment == null) {
	        throw new IllegalArgumentException("댓글이 존재하지 않거나 접근 권한이 없습니다.");
	    }

	    commentRepository.delete(comment);
	    
	    // 댓글 수 감소
	    PostEntity post = comment.getPostId();
	    
	    if (post != null) {
	        post.setCommentCount(post.getCommentCount() - 1);
	        postRepository.save(post);
	    }
	}
	
	// 댓글 수정
	@Transactional
	public void updateComment(Long commentId, Long userId, CommentUpdateDTO updateDTO) {
		
		CommentEntity comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new IllegalArgumentException("해당 댓글이 없습니다. id=" + commentId));
		
		if (!comment.getUserId().getUserId().equals(userId)) {
			throw new IllegalArgumentException("권한이 없습니다. 댓글 작성자와 동일한 사용자가 아닙니다.");
		}
		
		// 멘션 데이터 삭제
		mentionRepository.deleteByCommentId(comment);
		
		// 링크 태그 데이터 삭제
		contentLinktagRepository.deleteByCommentId(comment);
		
		comment.setContent(updateDTO.getContent());
		
		// 태그 처리
		processTags(updateDTO.getContent(), comment);
		
		commentRepository.save(comment);
		
	}
	
	

}

package com.ss.heartlinkapi.post.service;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import com.ss.heartlinkapi.like.entity.LikeEntity;
import com.ss.heartlinkapi.like.repository.LikeRepository;
import com.ss.heartlinkapi.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ss.heartlinkapi.block.repository.BlockRepository;
import com.ss.heartlinkapi.bookmark.entity.BookmarkEntity;
import com.ss.heartlinkapi.bookmark.repository.BookmarkRepository;
import com.ss.heartlinkapi.comment.dto.CommentDTO;
import com.ss.heartlinkapi.comment.entity.CommentEntity;
import com.ss.heartlinkapi.comment.repository.CommentRepository;
import com.ss.heartlinkapi.contentLinktag.entity.ContentLinktagEntity;
import com.ss.heartlinkapi.contentLinktag.repository.ContentLinktagRepository;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import com.ss.heartlinkapi.mention.entity.MentionEntity;
import com.ss.heartlinkapi.mention.repository.MentionRepository;
import com.ss.heartlinkapi.mission.service.CoupleMissionService;
import com.ss.heartlinkapi.post.dto.PostDTO;
import com.ss.heartlinkapi.post.dto.PostFileDTO;
import com.ss.heartlinkapi.post.dto.PostUpdateDTO;
import com.ss.heartlinkapi.post.entity.FileType;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.entity.PostFileEntity;
import com.ss.heartlinkapi.post.entity.Visibility;
import com.ss.heartlinkapi.post.repository.PostFileRepository;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;



@Service
public class PostService {

	private final PostRepository postRepository;
	private final PostFileRepository postFileRepository;
	private final CoupleService coupleService;
	private final CommentRepository commentRepository;
	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;
	private final PostFileService postFileService;
	private final ContentLinktagRepository contentLinktagRepository;
	private final LinkTagRepository linkTagRepository;
	private final MentionRepository mentionRepository;
	private final CoupleMissionService coupleMissionService;
	private final ElasticService elasticService;
	private final BlockRepository blockRepository;
	private final NotificationService notificationService;
	private final LikeRepository likeRepository;
	private final BookmarkRepository bookmarkRepository;


	public PostService(PostRepository postRepository, PostFileRepository postFileRepository, CoupleService coupleService, CommentRepository commentRepository, ProfileRepository profileRepository, UserRepository userRepository, PostFileService postFileService, ContentLinktagRepository contentLinktagRepository, LinkTagRepository linkTagRepository, MentionRepository mentionRepository, CoupleMissionService coupleMissionService, ElasticService elasticService, BlockRepository blockRepository, NotificationService notificationService, LikeRepository likeRepository, BookmarkRepository bookmarkRepository) {
		this.postRepository = postRepository;
		this.postFileRepository = postFileRepository;
		this.coupleService = coupleService;
		this.commentRepository = commentRepository;
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
		this.postFileService = postFileService;
		this.contentLinktagRepository = contentLinktagRepository;
		this.linkTagRepository = linkTagRepository;
		this.mentionRepository = mentionRepository;
		this.coupleMissionService = coupleMissionService;
		this.elasticService = elasticService;
		this.blockRepository = blockRepository;
        this.notificationService = notificationService;
        this.likeRepository = likeRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

	// 게시글 작성
	@Transactional
	public void savePost(PostDTO postDTO, List<MultipartFile> files, UserEntity user) {
		
		// 파일 개수 제한 검사
	    if (files.size() > 10) {
	        throw new IllegalArgumentException("첨부파일은 최대 10개까지만 허용됩니다.");
	    }
	    
		// VIDEO 파일 개수 제한 검사
	    long videoFileCount = files.stream()
		    .filter(file -> {
		        String originalFileName = file.getOriginalFilename();
		        String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
		        return postFileService.determineFileType(fileExtension) == FileType.VIDEO;
		    })
		    .count();
	    
	    if (videoFileCount > 1) {
	        throw new IllegalArgumentException("비디오 파일은 최대 1개까지만 업로드할 수 있습니다.");
	    }
		
		
	    // PostEntity 생성
	    PostEntity post = new PostEntity();
	    post.setUserId(user);
	    post.setContent(postDTO.getContent());
	    post.setVisibility(postDTO.getVisibility());
	    post.setLikeCount(0);
	    post.setCommentCount(0);

	    // PostEntity 저장
	    postRepository.save(post);
	    

	    // 파일 저장 경로 지정
	    String uploadDir = Paths.get("").toAbsolutePath().toString();
	    System.out.println("주소 보기 " + uploadDir);
	    
	    int sortOrder = 1; // 정렬 순서 초기화

	    
	    for (MultipartFile file : files) {
	        if (!file.isEmpty()) {
	            try {
	                // 파일 확장자 추출 및 검증
	                String originalFileName = file.getOriginalFilename();
	                String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
	                
	                if (!fileExtension.matches("\\.(jpg|jpeg|png|mp4|avi|mov|gif)$")) {
	                    throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
	                }
	                
	                // 파일 이름에 UUID 추가
	                String newFileName = UUID.randomUUID().toString() + fileExtension;
	                File destinationFile = new File(uploadDir + "/images/" + newFileName);
	                file.transferTo(destinationFile);
	                
	                // 파일 URL 생성
	                String fileUrl = "http://localhost:9090/images/" + newFileName;
	                
	                // PostFileEntity 생성
	                PostFileEntity postFile = new PostFileEntity();
	                postFile.setPostId(post); // postId 설정
	                postFile.setFileUrl(fileUrl);
	                postFile.setFileType(postFileService.determineFileType(fileExtension));
	                postFile.setSortOrder(sortOrder++);
	                
	                // PostFileEntity 저장
	                postFileRepository.save(postFile);
	                
	                System.out.println("파일 저장 완료: " + fileUrl);

	            } catch (Exception e) {
	                e.printStackTrace();
	                // 파일 저장 실패 로그 출력
	                System.err.println("파일 저장 실패: " + file.getOriginalFilename() + " - " + e.getMessage());
	            }
	        } else {
	            // 비어 있는 파일 경고 로그 출력
	            System.err.println("비어 있는 파일: " + file.getOriginalFilename());
	        }
	    }
	    
	    // 아이디 태그 및 해시태그 처리
	    processTags(postDTO.getContent(), post);
	    
	}
	
	// 게시글 작성 태그처리
	@Transactional
	private void processTags(String content, PostEntity post) {
		
		// 아이디 태그 처리
		Pattern userPattern = Pattern.compile("@(\\w+)");
		Matcher userMatcher = userPattern.matcher(content);
		
		while(userMatcher.find()) {
			String username = userMatcher.group(1); // @ 생략
			UserEntity user = userRepository.findByLoginId(username);
			
			// 해당 유저 있을 경우
	        if(user != null) {
	            // 기타 처리(알림)
	            System.out.println("아이디 태그 처리: " + username);
	            
	            MentionEntity mention = new MentionEntity();
	            mention.setUserId(user);
	            mention.setPostId(post);

	            mentionRepository.save(mention);

				//	알람 연동
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

				notificationService.notifyIdTag(authentication.getName(), post.getPostId(), user.getUserId());
	        } else {
	            System.out.println("아이디 태그 처리 실패: " + username + "는 존재하지 않는 사용자입니다.");
	        }
		}
		
		// 해시태그 처리
		Pattern linktagPattern = Pattern.compile("&([\\w가-힣_]+)");
		Matcher linktagMatcher = linktagPattern.matcher(content);
		
		List<ContentLinktagEntity> contentLinktags = new ArrayList<>();
		
		while(linktagMatcher.find()) {
			System.out.println("해시태그 발견: " + linktagMatcher.group(1)); // 추가된 로깅
			
			String keyword = linktagMatcher.group(1); // & 생략
			// linkTag에 내가 작성한 태그가 없으면 데이터 생성
			LinkTagEntity linkTag = linkTagRepository.findByKeyword(keyword)
					.orElseGet(() -> new LinkTagEntity(null, keyword));
			LinkTagEntity result = linkTagRepository.save(linkTag);
			// 엘라스틱 태그 인덱스에 추가
			if(elasticService.addTag(result)==null) {
				System.out.println("엘라스틱 태그 저장 실패");
			}
			System.out.println("저장된 LinkTag: " + linkTag.getKeyword());
			
			
			ContentLinktagEntity contentLinktag = new ContentLinktagEntity();
			contentLinktag.setLinktagId(linkTag);
			contentLinktag.setBoardId(post);
			contentLinktags.add(contentLinktag);
			
			System.out.println("저장될 ContentLinktag: " + contentLinktag);
		}
		
		contentLinktagRepository.saveAll(contentLinktags);
		
		// 미션 서비스 추가
		coupleMissionService.checkMissionTag(post.getCreatedAt(), contentLinktags);
	}
	


	
	
	// 내 팔로잉 게시물 조회
		public Map<String, Object> getPublicPostByFollowerId(Long userId, Integer cursor, int limit) {
		    // 모든 게시물을 가져옴
		    List<PostEntity> allPosts = postRepository.findPublicPostsByFollowerId(userId);
		    
//		    // role이 ROLE_COUPLE인 게시글만 필터링
//		    allPosts = allPosts.stream()
//		                       .filter(post -> post.getUserId().getRole() == Role.ROLE_COUPLE)
//		                       .collect(Collectors.toList());

		    // 커서가 없는 경우 (처음 페이지)
		    if (cursor == null) cursor = Integer.MAX_VALUE;
		    
		    final Integer effectiveCursor = cursor;

		    // 요청된 커서 위치부터 limit만큼 자르기
		    List<PostEntity> sliceData = allPosts.stream()
		        .filter(post -> post.getPostId() < effectiveCursor)
		        .limit(limit)
		        .collect(Collectors.toList());

		    // PostDTO로 변환
		    List<PostDTO> postDTOs = sliceData.stream()
		        .map(post -> {
		            List<PostFileEntity> postFiles = postFileRepository.findByPostId(post.getPostId());
		            List<ProfileEntity> profiles = profileRepository.findAllByUserEntity(post.getUserId());
		            UserEntity partner = coupleService.getCouplePartner(post.getUserId().getUserId());
		            
		            // 현재 사용자가 해당 게시글에 좋아요를 눌렀는지 여부를 확인
			        Optional<LikeEntity> existingLike = likeRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
			        boolean isLiked = existingLike.isPresent();  // 좋아요가 눌렀으면 true, 아니면 false
			        
			        // 현재 사용자가 해당 게시글에 북마크를 눌렀는지 여부를 확인
			        Optional<BookmarkEntity> existingBookmark = bookmarkRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
			        boolean isBookmarked = existingBookmark.isPresent();

		            return new PostDTO(
		                post.getPostId(),
		                post.getUserId().getUserId(),
		                post.getUserId().getLoginId(),
		                post.getContent(),
		                post.getCreatedAt(),
		                post.getUpdatedAt(),
		                post.getLikeCount(),
		                post.getCommentCount(),
		                post.getVisibility(),
		                profiles != null && !profiles.isEmpty() ? profiles.get(0).getProfile_img() : null,
		                postFiles.stream()
		                    .map(file -> new PostFileDTO(
		                        post.getPostId(),
		                        file.getFileUrl(),
		                        file.getFileType(),
		                        file.getSortOrder()))
		                    .collect(Collectors.toList()),
		                null,
		                partner != null ? partner.getLoginId() : "No Partner",
		                partner != null ? partner.getUserId() : null,
		                null,
		                null,
		                isLiked,
		                isBookmarked
		            );
		        })
		        .collect(Collectors.toList());

		    // 다음 커서를 계산
		    Long nextCursor = sliceData.size() < limit ? null : sliceData.get(sliceData.size() - 1).getPostId();

		    // 응답 데이터 생성
		    Map<String, Object> response = new HashMap<>();
		    response.put("data", postDTOs);
		    response.put("nextCursor", nextCursor);
		    response.put("hasNext", nextCursor != null);

		    return response;
		}

		// 비팔로우 및 신고되지 않은 게시물 조회
		public Map<String, Object> getNonFollowedAndNonReportedPosts(Long userId, Integer cursor, int limit) {
		    // 모든 게시물을 가져옴
		    List<PostEntity> allPosts = postRepository.findNonFollowedAndNonReportedPosts(userId);
		    
		    // role이 ROLE_COUPLE인 게시글만 필터링
		    allPosts = allPosts.stream()
		                       .filter(post -> post.getUserId().getRole() == Role.ROLE_COUPLE)
		                       .collect(Collectors.toList());

		    // 커서가 없는 경우 (처음 페이지)
		    if (cursor == null) cursor = Integer.MAX_VALUE;
		    
		    final Integer effectiveCursor = cursor;

		    // 요청된 커서 위치부터 limit만큼 자르기
		    List<PostEntity> sliceData = allPosts.stream()
		    	    .filter(post -> post.getPostId() < effectiveCursor)
		    	    .limit(limit)
		    	    .collect(Collectors.toList());

		    // PostDTO로 변환
		    List<PostDTO> postDTOs = sliceData.stream()
		        .map(post -> {
		            List<PostFileEntity> postFiles = postFileRepository.findByPostId(post.getPostId());
		            List<ProfileEntity> profiles = profileRepository.findAllByUserEntity(post.getUserId());
		            UserEntity partner = coupleService.getCouplePartner(post.getUserId().getUserId());
		            
		            // 현재 사용자가 해당 게시글에 좋아요를 눌렀는지 여부를 확인
			        Optional<LikeEntity> existingLike = likeRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
			        boolean isLiked = existingLike.isPresent();  // 좋아요가 눌렀으면 true, 아니면 false
			        
			        // 현재 사용자가 해당 게시글에 북마크를 눌렀는지 여부를 확인
			        Optional<BookmarkEntity> existingBookmark = bookmarkRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
			        boolean isBookmarked = existingBookmark.isPresent();
			        
		            return new PostDTO(
		                post.getPostId(),
		                post.getUserId().getUserId(),
		                post.getUserId().getLoginId(),
		                post.getContent(),
		                post.getCreatedAt(),
		                post.getUpdatedAt(),
		                post.getLikeCount(),
		                post.getCommentCount(),
		                post.getVisibility(),
		                profiles != null && !profiles.isEmpty() ? profiles.get(0).getProfile_img() : null,
		                postFiles.stream()
		                    .map(file -> new PostFileDTO(
		                        post.getPostId(),
		                        file.getFileUrl(),
		                        file.getFileType(),
		                        file.getSortOrder()))
		                    .collect(Collectors.toList()),
		                null,
		                partner != null ? partner.getLoginId() : "No Partner",
		                partner != null ? partner.getUserId() : null,
		                null,
		                null,
		                isLiked,
		                isBookmarked
		            );
		        })
		        .collect(Collectors.toList());

		    // 다음 커서를 계산
		    Long nextCursor = sliceData.size() < limit ? null : sliceData.get(sliceData.size() - 1).getPostId();

		    // 응답 데이터 생성
		    Map<String, Object> response = new HashMap<>();
		    response.put("data", postDTOs);
		    response.put("nextCursor", nextCursor);
		    response.put("hasNext", nextCursor != null);

		    return response;
		}

	
		// 게시글 상세보기
		public PostDTO getPostById(Long postId, Long userId) {
		    Optional<PostEntity> optionalPost = postRepository.findById(postId);
		    
		    // 값이 존재하는 경우
		    if (optionalPost.isPresent()) {
		        PostEntity post = optionalPost.get();
		        List<PostFileEntity> postFiles = postFileRepository.findByPostId(post.getPostId());
		        List<ProfileEntity> profiles = profileRepository.findAllByUserEntity(post.getUserId());
		        UserEntity partner = coupleService.getCouplePartner(post.getUserId().getUserId());
		        
		        // 댓글 목록 가져오기
		        List<CommentEntity> comments = commentRepository.findByPostIdAndNotReported(post, userId);
		        
		        
		        
		        List<CommentDTO> commentDTO = comments.stream()
		            .map(comment -> {
		            	List<ProfileEntity> commentProfiles = profileRepository.findAllByUserEntity(comment.getUserId());
		                String profileImage = (commentProfiles != null && !commentProfiles.isEmpty()) ? commentProfiles.get(0).getProfile_img() : null;
		            	
		                // 댓글에 태그된 사용자들을 가져옴
			            List<MentionEntity> mentionEntities = mentionRepository.findByCommentId(comment);
			            
			            List<String> mentionedLoginIds = mentionEntities.stream()
				                .map(mention -> mention.getUserId().getLoginId()) // loginId 추출
				                .collect(Collectors.toList()); 
			            
			            List<Long> mentionedUserIds = mentionEntities.stream()
			                .map(mention -> mention.getUserId().getUserId()) // userId 추출
			                .collect(Collectors.toList());
			            
			            // 댓글에 좋아요 상태 확인
		                Optional<LikeEntity> existingCommentLike = likeRepository.findByUserIdAndCommentIdUsingQuery(userId, comment.getCommentId());
		                boolean isCommentLiked = existingCommentLike.isPresent();  // 댓글 좋아요 여부
		
		                // 현재 사용자가 해당 게시글에 북마크를 눌렀는지 여부를 확인
				        Optional<BookmarkEntity> existingBookmark = bookmarkRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
				        boolean isBookmarked = existingBookmark.isPresent();
		                
		            return new CommentDTO(
		                comment.getCommentId(),
		                comment.getPostId().getPostId(),
		                comment.getParentId() != null ? comment.getParentId().getCommentId() : null,
		                comment.getUserId().getUserId(),
		                comment.getContent(),
		                comment.getCreatedAt(),
		                comment.getUpdatedAt(),
		                comment.getUserId().getLoginId(),
		                profileImage,
		                mentionedLoginIds,
		                mentionedUserIds,
		                isCommentLiked
		            );
		        })
		        .collect(Collectors.toList());
		        
		        // 게시글에 태그된 사용자들(userId) 조회
		        List<MentionEntity> mentions = mentionRepository.findMentionsByPostIdPostId(postId);

		        List<String> mentionedLoginIds = mentions.stream()
			            .map(mention -> mention.getUserId().getLoginId()) // loginId만 추출
			            .collect(Collectors.toList());
		        
		        List<Long> mentionedUserIds = mentions.stream()
	            .map(mention -> mention.getUserId().getUserId()) // userId만 추출
	            .collect(Collectors.toList());
		        
		        // 현재 사용자가 해당 게시글에 좋아요를 눌렀는지 여부를 확인
		        Optional<LikeEntity> existingLike = likeRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
		        boolean isLiked = existingLike.isPresent();  // 좋아요가 눌렀으면 true, 아니면 false
		        
		        // 현재 사용자가 해당 게시글에 북마크를 눌렀는지 여부를 확인
		        Optional<BookmarkEntity> existingBookmark = bookmarkRepository.findByUserIdAndPostIdUsingQuery(userId, post.getPostId());
		        boolean isBookmarked = existingBookmark.isPresent();

		        
		        return new PostDTO(
		            post.getPostId(),
		            post.getUserId().getUserId(),
		            post.getUserId().getLoginId(),
		            post.getContent(),
		            post.getCreatedAt(),
		            post.getUpdatedAt(),
		            post.getLikeCount(),
		            post.getCommentCount(),
		            post.getVisibility(),
		            (profiles != null) ? profiles.get(0).getProfile_img() : null, // 프로필 이미지 추가
		            postFiles.stream()
		                .map(file -> new PostFileDTO(
		                    post.getPostId(),
		                    file.getFileUrl(),
		                    file.getFileType(),
		                    file.getSortOrder()))
		                .collect(Collectors.toList()),
		            commentDTO.isEmpty() ? Collections.emptyList() : commentDTO, // 댓글이 없으면 빈 리스트
		            partner != null ? partner.getLoginId() : "No Partner",
		           partner != null ? partner.getUserId() : null,
		           mentionedLoginIds,
		           mentionedUserIds,
		           isLiked,
		           isBookmarked
		        );
		    } else {
		        throw new NoSuchElementException("해당 게시글을 찾을 수 없습니다.");
		    }
		}
	
	// 사용자와 사용자의 커플 게시글 목록 가져오기
	public Map<String, Object> getPostFilesByUserId(Long currentUserId, Long userId, Integer cursor, int limit) {
	    UserEntity partner = coupleService.getCouplePartner(userId);
	    
	    // 로그인한 사용자가 차당당한 경우
	    boolean isBlocked1 = blockRepository.existsByBlockedId_UserIdAndBlockerId_UserId(userId, currentUserId);
	    boolean isBlocked2 = blockRepository.existsByBlockedId_UserIdAndCoupleId_CoupleId(userId, currentUserId);

	    // 로그인한 사용자가 차단한 경우
	    boolean isBlocker1 = blockRepository.existsByBlockedId_UserIdAndBlockerId_UserId(currentUserId, userId);
	    boolean isBlocker2 = blockRepository.existsByBlockedId_UserIdAndCoupleId_CoupleId(currentUserId, userId);
	    
	    // 차단당한 경우
	    boolean isBlocked = isBlocked1 || isBlocked2;
	    
	    // 차단한경우
	    boolean isBlocker = isBlocker1 || isBlocker2;
	    
	    List<PostFileEntity> myPostFiles = postFileRepository.findPostFilesByUserId(userId);
	    List<PostFileEntity> partnerPostFiles = new ArrayList<>();
	    
	    if (partner != null) {
	        partnerPostFiles = postFileRepository.findPostFilesByUserId(partner.getUserId());
	    }
	    
	    List<PostFileEntity> allPostFiles = new ArrayList<>();
	    allPostFiles.addAll(myPostFiles);
	    allPostFiles.addAll(partnerPostFiles);

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
	    response.put("isBlocked", isBlocked);  // 차단당함
	    response.put("isBlocker", isBlocker);  // 차단함

	    return response;
	}

	
	// 게시글 삭제
	public void deleteMyPost(Long postId, Long userId) {
		PostEntity post = postRepository.findByPostIdAndUserId_UserId(postId, userId);
		
		if (post != null) {
			postRepository.delete(post);
		} else {
			throw new RuntimeException("게시글이 존재하지 않거나 작성자가 아닙니다.");
		}

	}
	
	// 모든 게시글 삭제
	@Transactional
	public void deleteAllPostByUser(Long userId) {
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		
		postRepository.deleteByUserId(user);
		
	}
	
	// 게시글 수정
	@Transactional
	public void updatePost(Long postId, Long userId, PostUpdateDTO updateDTO) {
	    // 게시글 조회 및 권한 확인
	    PostEntity post = postRepository.findById(postId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + postId));
	
	    if (!post.getUserId().getUserId().equals(userId)) {
	        throw new IllegalArgumentException("권한이 없습니다. 게시글 작성자와 동일한 사용자가 아닙니다.");
	    }
	    
	    // 멘션 데이터 삭제
	    mentionRepository.deleteByPostId(post);
	    
	    // 링크 태그 데이터 삭제
	    contentLinktagRepository.deleteByBoardId(post);
	
	    // 게시글 내용 및 가시성 업데이트
	    post.setContent(updateDTO.getContent());
	
	    processTags(updateDTO.getContent(), post);
	
	    postRepository.save(post);
	}

	// 태그 검색 결과 조회
	public List<PostFileDTO> searchPostByLinktag(String keyword) {
	    List<PostFileDTO> result = new ArrayList<>();
	    
	    // 입력한 내용이 Linktag에 있는지 조회
	    Optional<LinkTagEntity> linkTagOptional = linkTagRepository.findByKeyword(keyword);
	    
	    // LinkTag에 없으면 빈 리스트 반환
	    if (!linkTagOptional.isPresent()) {
	        System.out.println("LinkTag에 없는 keyword : " + keyword);
	        return result;
	    }
	    String Linktag = '&' + keyword;
	    
	    // content에 해당 keyword가 있고 visibility가 "PRIVATE"이 아닌 게시글만 조회
	    List<PostEntity> posts = postRepository.findByContentContainingAndVisibilityNot(Linktag, Visibility.PRIVATE);
	    
	    // Post에 없으면 빈 리스트 반환
	    if(posts.isEmpty()) {
	        System.out.println("해당하는 게시글이 없습니다.");
	        return result;
	    }
	    
	    // role이 ROLE_COUPLE인 게시글만 필터링
	    posts = posts.stream()
	                 .filter(post -> post.getUserId().getRole() == Role.ROLE_COUPLE)
	                 .collect(Collectors.toList());
	    
	    for(PostEntity post : posts) {
	        System.out.println("게시글 있음 post는 " + post);
	        Long postId = post.getPostId();
	        
	        List<PostFileEntity> postFiles = postFileRepository.findByPostIdAndSortOrder(postId);
	        
	        for (PostFileEntity postFile : postFiles) {
	            result.add(new PostFileDTO(postId, postFile.getFileUrl(), postFile.getFileType(), postFile.getSortOrder()));
	        }
	    }
	    System.out.println(result);
	    return result;
	}






	


//	관리자 신고한 게시물 삭제
    public void deletePost(Long postId) {
		postRepository.deleteById(postId);
    }
}

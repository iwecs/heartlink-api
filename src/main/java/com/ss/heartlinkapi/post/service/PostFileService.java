package com.ss.heartlinkapi.post.service;

import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.post.entity.FileType;
import com.ss.heartlinkapi.post.repository.PostFileRepository;

@Service
public class PostFileService {

	private final PostFileRepository postFileRepository;
	
	public PostFileService(PostFileRepository postFileRepository) {
		this.postFileRepository = postFileRepository;
	}
	
	// 게시글 작성 시 파일 업로드 메서드
//	private final String UPLOAD_DIR
	
	
	// 파일 타입 설정
	public FileType determineFileType(String fileUrl) {
		
		if(fileUrl.endsWith(".jpg") || fileUrl.endsWith(".jpeg") || fileUrl.endsWith(".png")) {

			return FileType.IMAGE;
		}else if(fileUrl.endsWith(".mp4") || fileUrl.endsWith(".avi") || fileUrl.endsWith(".mov") || fileUrl.endsWith(".gif")) {
			
			return FileType.VIDEO;
		}
		throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. " + fileUrl);
	}
}

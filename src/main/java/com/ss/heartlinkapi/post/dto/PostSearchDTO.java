package com.ss.heartlinkapi.post.dto;

import com.ss.heartlinkapi.post.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchDTO {
    private Long postId;		// 게시글 id
    private String fileUrl; 	// 파일 경로
}

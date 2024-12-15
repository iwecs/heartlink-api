package com.ss.heartlinkapi.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDTO {

	private String content;						// 수정할 게시글 내용
}

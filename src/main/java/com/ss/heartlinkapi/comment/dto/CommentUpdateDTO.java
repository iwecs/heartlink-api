package com.ss.heartlinkapi.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateDTO {
	
	private String content;		// 수정할 댓글 내용

}

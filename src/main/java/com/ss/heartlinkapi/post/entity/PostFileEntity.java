package com.ss.heartlinkapi.post.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;

@Entity
@Data
@Table(name = "post_file",
	   uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "sort_order"})
    }
)
public class PostFileEntity {
	@Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long postFileId;		// 게시글 첨부파일 id
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private PostEntity postId;		// 게시글 id
	
	@Column(name = "file_url", nullable = false)
	private String fileUrl;			// 게시글 첨부파일 url
	
	@Enumerated(EnumType.STRING)
	private FileType fileType;		// 파일 타입(video or image)
	
	@Column(name = "sort_order")
	private int sortOrder;			// 정렬 순서

	
	
	@Override
	public String toString() {
	    return "PostFileEntity{" +
	            "postFileId=" + postFileId +
	            ", fileUrl='" + fileUrl + '\'' +
	            '}';
	}
}




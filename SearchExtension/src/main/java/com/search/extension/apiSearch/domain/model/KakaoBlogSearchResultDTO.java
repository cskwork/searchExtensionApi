package com.search.extension.apiSearch.domain.model;

import java.util.List;

import lombok.Data;

@Data
public class KakaoBlogSearchResultDTO implements BlogSearchResultDTO {
	private Meta meta; 				  // 응답 관련 정보
	private List<Document> documents; // 응답 결과

	@Data
	public static class Meta {
		private int total_count; 	  // 검색된 문서 수
		private int pageable_count;   // total_count 중 노출 가능 문서 수
		private String is_end; 		  // 현재 페이지가 마지막 페이지인지 여부, 값이 false면 page를 증가시켜 다음 페이지를 요청할 수 있음
	}

	@Data
	public static class Document {
		private String title; 		  // 문서 제목
		private String contents; 	  // 문서 본문 중 일부
		private String url; 		  // 문서 URL
		private String blogname; 	  // 블로그명
		private String thumbnail; 	  // 썸네일
		private String datetime; 	  // 문서 글 작성시간, ISO 8601 [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].000+[tz]
	}
	
    public boolean isEmpty() {
        return (meta == null || documents.isEmpty());
       // Add more conditions for remaining fields
    }
    

}
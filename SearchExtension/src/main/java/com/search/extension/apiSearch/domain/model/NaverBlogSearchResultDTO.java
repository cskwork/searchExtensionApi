package com.search.extension.apiSearch.domain.model;

import java.util.List;

import lombok.Data;

@Data
public class NaverBlogSearchResultDTO implements BlogSearchResultDTO {
	private String lastBuildDate; // 검색 결과를 생성한 시간
	private int total; // 총 검색 결과 개수
	private int start; // 검색 시작 위치
	private int display; // 한 번에 표시할 검색 결과 개수

	private List<Item> items;

	@Data
	public static class Item {
		private String title; // 블로그 포스트의 제목. 제목에서 검색어와 일치하는 부분은 <b> 태그로 감싸져 있습니다.
		private String link; // 블로그 포스트의 URL
		private String description; // 블로그 포스트의 내용을 요약한 패시지 정보. 패시지 정보에서 검색어와 일치하는 부분은 <b> 태그로 감싸져 있습니다.
		private String bloggername; // 블로그 포스트가 있는 블로그의 이름
		private String bloggerlink; // 블로그 포스트가 있는 블로그의 주소
		private String postdate; // 블로그 포스트가 작성된 날짜
	}

}
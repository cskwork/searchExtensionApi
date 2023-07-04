package com.search.extension.apiSearch.domain;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "POPULAR_KEYWORD", uniqueConstraints = {
		@UniqueConstraint(name = "KEYWORD_ID", columnNames = { "KEYWORD_ID" }) })
public class PopularKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "KEYWORD_ID")
	private Long keywordId;

	@Column(name = "KEYWORD", nullable = false)
	private String keyword;

	@Column(name = "COUNT", nullable = false)
	private Integer count;

	@Column(name = "CREATED_TIME", nullable = false)
	private Timestamp createdTime;

	@PrePersist
	public void onPrePersist() {
		createdTime = new Timestamp(System.currentTimeMillis());
	}
	
	public PopularKeyword(String keyword, Integer count, Timestamp createdTime) {
		super();
		this.keyword = keyword;
		this.count = count;
		this.createdTime = createdTime;
	}
	public PopularKeyword(String keyword, Integer count) {
		super();
		this.keyword = keyword;
		this.count = count;
	}
}

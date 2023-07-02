package com.search.extension.apiSearch.domain;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "POPULAR_KEYWORD"
, uniqueConstraints = { @UniqueConstraint(name = "KEYWORD_ID", columnNames = { "KEYWORD_ID" }) })
public class PopularKeyword {
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "KEYWORD_ID")
	private String keywordId;

	@Column(name = "KEYWORD", nullable = false)
	private String keyword;

	@Column(name = "COUNT", nullable = false)
	private Integer count;

	@Column(name = "API_SOURCE")
	private String apiSource;

	@Column(name = "CREATED_TIME", nullable = false)
	private Timestamp createdTime;

	@Column(name = "UPDATED_TIME", nullable = false)
	private Timestamp updatedTime;

}

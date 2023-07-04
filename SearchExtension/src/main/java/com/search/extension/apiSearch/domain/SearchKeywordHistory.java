package com.search.extension.apiSearch.domain;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "SEARCH_KEYWORD_HISTORY"
, uniqueConstraints = { @UniqueConstraint(name = "KEYWORD_ID", columnNames = { "KEYWORD_ID" }) })
public class SearchKeywordHistory { 
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "KEYWORD_ID")
	private Long keywordId;

	@Column(name = "KEYWORD", nullable = false)
	private String keyword;

	@Column(name = "COUNT", nullable = false)
	private Integer count;

	@Column(name = "API_SOURCE")
	private String apiSource;

	@Column(name = "CREATED_TIME", nullable = false)
	private Timestamp createdTime;	
}

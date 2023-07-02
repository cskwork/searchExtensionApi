package com.search.extension.apiSearch.domain;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "USER_REQUEST"
, uniqueConstraints = { @UniqueConstraint(name = "USER_ID", columnNames = { "USER_ID" }) })
public class UserRequest {
	@Id
	@Column(name = "USER_ID")
	private String userId;

	@ManyToOne
	@JoinColumn(name = "KEYWORD_ID", nullable = false)
	private PopularKeyword popularKeyword;

	@Column(name = "REQUEST_IP")
	private String requestIp;

	@Column(name = "REQUEST_COUNT", nullable = false)
	private Integer requestCount;

	@Column(name = "CREATED_TIME", nullable = false)
	private Timestamp createdTime;

	@Column(name = "UPDATED_TIME", nullable = false)
	private Timestamp updatedTime;

}
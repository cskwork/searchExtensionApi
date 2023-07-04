package com.search.extension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.search.extension.apiSearch.domain")
@EnableJpaRepositories("com.search.extension.apiSearch.adapter.persistence")
public class SearchExtensionApplication {
	public static void main(String[] args) {
		SpringApplication.run(SearchExtensionApplication.class, args);
	}
}

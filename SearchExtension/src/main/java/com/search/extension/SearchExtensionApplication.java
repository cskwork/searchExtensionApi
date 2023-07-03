package com.search.extension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("com.search.extension.apiSearch.domain")
@SpringBootApplication
public class SearchExtensionApplication {
	public static void main(String[] args) {
		SpringApplication.run(SearchExtensionApplication.class, args);
	}
}

package com.search.extension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;

import com.search.extension.apiSearch.application.exception.CustomErrorController;

@SpringBootApplication
public class SearchExtensionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchExtensionApplication.class, args);
	}
	
    @Bean
    public CustomErrorController customErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
		return new CustomErrorController(errorAttributes, errorProperties);
	}

}

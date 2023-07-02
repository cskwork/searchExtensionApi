package com.search.extension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SearchExtensionApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(SearchExtensionApplication.class, args);
	}
	
//    @Bean
//    public CustomErrorController customErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
//		return new CustomErrorController(errorAttributes, errorProperties);
//	}

}

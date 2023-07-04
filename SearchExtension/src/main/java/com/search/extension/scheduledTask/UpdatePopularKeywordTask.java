package com.search.extension.scheduledTask;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.search.extension.apiSearch.adapter.persistence.PopularKeywordJpaRepository;
import com.search.extension.apiSearch.adapter.persistence.SearchKeywordHistoryQueryRepository;
import com.search.extension.apiSearch.domain.PopularKeyword;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@NoArgsConstructor
public class UpdatePopularKeywordTask implements Runnable {
	private String message;

	public UpdatePopularKeywordTask(String message){
        this.message = message;
    }

	@Autowired
	private PopularKeywordJpaRepository popularKeywordJpaRepository;
	@Autowired
	private SearchKeywordHistoryQueryRepository searchKeywordQueryRepository;

	@Transactional
	public void updatePopularKeywordDatabase() {
		List<PopularKeyword> popularKeywordList = searchKeywordQueryRepository.getGroupByApiSourceForKeyword();
		
		// 데이터 없는 경우
		if (popularKeywordJpaRepository.count() == 0) {
			for (PopularKeyword popularKeyword : popularKeywordList) {
				popularKeywordJpaRepository.save(popularKeyword);
			}
			return;
		}

		// 기존 데이터가 있으면
		popularKeywordJpaRepository.deleteAll();
		for (PopularKeyword popularKeyword : popularKeywordList) {
			popularKeywordJpaRepository.save(popularKeyword);
		}
		log.info("UpdatePopularKeywordTask success");
		return;
	}

	@Override
	public void run() {
		log.info(new Date() 
				+ " Runnable Task with " 
				+ message 
				+ " on thread " 
				+ Thread.currentThread().getName());
	}
}
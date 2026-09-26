package com.search.extension.scheduledTask;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.search.extension.apiSearch.adapter.persistence.PopularKeywordJpaRepository;
import com.search.extension.apiSearch.adapter.persistence.SearchKeywordHistoryQueryRepository;
import com.search.extension.apiSearch.domain.PopularKeyword;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
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

	@Transactional(isolation=Isolation.REPEATABLE_READ)
	public void updatePopularKeywordDatabase() {
		List<PopularKeyword> popularKeywordList = searchKeywordQueryRepository.getGroupByApiSourceForKeyword();
		
        popularKeywordJpaRepository.deleteAllInBatch();
        popularKeywordJpaRepository.saveAll(popularKeywordList);
        popularKeywordJpaRepository.flush();
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
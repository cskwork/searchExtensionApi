package com.search.extension.scheduledTask;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "com.search.extension.scheduledTask"
			 , basePackageClasses = {
					 	UpdatePopularKeywordTask.class 
			 })
public class TaskSchedulerConfig implements SchedulingConfigurer {
	@Autowired
	private UpdatePopularKeywordTask updatePopularKeywordsTask;

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegister) {
		//TaskScheduler taskScheduler = threadPoolTaskScheduler();
		//taskRegister.setTaskScheduler(taskScheduler);
	    Runnable task = () -> updatePopularKeywordsTask.updatePopularKeywordDatabase();
	    threadPoolTaskScheduler().scheduleAtFixedRate(task,Duration.ofSeconds(5));
	}
}

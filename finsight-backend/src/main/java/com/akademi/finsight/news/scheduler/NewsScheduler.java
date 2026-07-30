package com.akademi.finsight.news.scheduler;

import com.akademi.finsight.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsScheduler{

    private final NewsService newsService;

    @Scheduled(cron = "0 0 * * * *")
    public void fetchNewsHourly() {
        log.info("Scheduled task triggered: refreshing news cache.");
        newsService.fetchAndCacheNews();
    }

	@EventListener(ApplicationReadyEvent.class)
	public void loadNewsAtStartup() {
		try {
			newsService.fetchAndCacheNews();
		} catch (Exception e) {
			log.warn("Failed to load news at startup; application will continue without cached news.", e);
		}
	}
}

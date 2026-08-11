package com.akademi.finsight.news.controller;

import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.news.controller.api.NewsApi;
import com.akademi.finsight.news.dto.response.NewsResponse;
import com.akademi.finsight.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class NewsController extends BaseController implements NewsApi {

    private final NewsService newsService;

    @Override
    public ResponseEntity<ApiStandardResponse<List<NewsResponse>>> getLatestNews(Locale locale) {
        return ok(newsService.getNews(locale));
    }
}
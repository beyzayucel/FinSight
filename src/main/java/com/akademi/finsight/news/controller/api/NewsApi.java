package com.akademi.finsight.news.controller.api;

import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.news.dto.response.NewsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Locale;

@RequestMapping(ApiEndpoints.News.BASE)
@Tag(
        name = "News Management",
        description = "Latest business and finance news operations"
)
public interface NewsApi {

    @Operation(
            summary = "Get latest news",
            description = "Returns the latest curated business and finance news. Automatically handles translation."
    )
    @ApiResponse(responseCode = "200", description = "News retrieved successfully")
    @GetMapping
    ResponseEntity<ApiStandardResponse<List<NewsResponse>>> getLatestNews(Locale locale);
}
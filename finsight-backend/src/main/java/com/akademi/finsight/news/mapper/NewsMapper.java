package com.akademi.finsight.news.mapper;

import com.akademi.finsight.common.mapper.BaseMapperConfig;
import com.akademi.finsight.news.dto.client.NewsItem;
import com.akademi.finsight.news.dto.response.NewsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = BaseMapperConfig.class, imports = {com.akademi.finsight.news.util.DateUtil.class})
public interface NewsMapper {

    @Mapping(target = "hoursAgo", expression = "java(DateUtil.getHoursAgo(newsItem.publishDate()))")
    NewsResponse toResponse(NewsItem newsItem);

    List<NewsResponse> toResponseList(List<NewsItem> newsItems);
}

package com.newzy.backend.domain.news.service;

import com.newzy.backend.domain.news.dto.request.NewsCardRequestDTO;
import com.newzy.backend.domain.news.dto.request.NewsListGetRequestDTO;
import com.newzy.backend.domain.news.dto.response.*;

import java.util.List;
import java.util.Map;

public interface NewsService {
    Map<String, Object> getNewsList(NewsListGetRequestDTO newsListGetRequestDTO);

    NewsDetailGetResponseDTO getNewsDetail(Long userId, Long NewsId);

    void bookmark(Long userId, Long NewsId);

    void deleteBookmark(Long userId, Long newsId);

    void likeNews(Long userId, Long NewsId);

    void deleteLike(Long userId, Long NewsId);

    Map<String, Object> getCardList(Long userId, int page);

    List<NewsListGetResponseDTO> getHotNewsList();

    List<NewsRecommendGetResponseDTO> getRecommendedNewsList(Long userId);

    NewsDailyGetResponseDTO getDailyContent(Long userId);

    void collectNewsCard(Long userId, NewsCardRequestDTO newsCardRequestDTO);

    NewsCardListGetResponseDTO getCardInfo(Long userId, Long cardId);
}

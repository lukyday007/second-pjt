package com.newzy.backend.domain.news.controller;

import com.newzy.backend.domain.news.dto.request.NewsCardRequestDTO;
import com.newzy.backend.domain.news.dto.request.NewsListGetRequestDTO;
import com.newzy.backend.domain.news.dto.response.*;
import com.newzy.backend.domain.news.service.NewsService;
import com.newzy.backend.domain.user.service.UserService;
import com.newzy.backend.global.exception.CustomIllegalStateException;
import com.newzy.backend.global.exception.NoTokenRequestException;
import com.newzy.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
@Tag(name = "News API", description = "News 관련 API")
public class NewsController {

    private final NewsService newsService;
    private final UserService userService;

    @GetMapping("/{newsId}")
    @Operation(summary = "뉴스 정보 조회", description = "뉴스 ID로 뉴스의 상세 정보를 조회합니다.")
    public ResponseEntity<NewsDetailGetResponseDTO> getNews(
            @Parameter(description = "뉴스 ID", required = true)
            @PathVariable Long newsId,
            @Parameter(description = "JWT", required = false)
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        }

        log.info(">>> [GET] /news/{} - 요청 ID: {}, userId - {}", newsId, newsId, userId);

        NewsDetailGetResponseDTO newsDetailGetResponseDto = newsService.getNewsDetail(userId, newsId);

        return ResponseEntity.status(200).body(newsDetailGetResponseDto);
    }


    @GetMapping
    @Operation(summary = "뉴스 목록 조회", description = "해당 뉴스 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getNewsList(
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "카테고리")
            @RequestParam(value = "category", required = false, defaultValue = "3") int category,
            @Parameter(description = "정렬기준 (0: 기사 등록시간 최신순, 1: 조회수순)")
            @RequestParam(value = "sort", required = false, defaultValue = "0") int sort,
            @Parameter(description = "키워드")
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info(">>> [GET] /news - 요청 파라미터: page - {}, sort - {}, category - {}, keyword - {}", page, sort, category, keyword);
        NewsListGetRequestDTO newsListGetRequestDTO = new NewsListGetRequestDTO(page, category, sort, keyword);
        Map<String, Object> newsListGetResponseDtoMap = newsService.getNewsList(newsListGetRequestDTO);

        return ResponseEntity.status(200).body(newsListGetResponseDtoMap);
    }


    @GetMapping(value = "/hot")
    @Operation(summary = "많이 본 뉴스 조회", description = "조회수가 많은 뉴스를 조회합니다.")
    public ResponseEntity<List<NewsListGetResponseDTO>> getHotNewsList() {
        log.info(">>> [GET] /news/hot - 요청 파라미터");
        List<NewsListGetResponseDTO> hotNewsList = newsService.getHotNewsList();

        return ResponseEntity.status(200).body(hotNewsList);
    }

    @GetMapping(value = "/recommend")
    @Operation(summary = "추천 뉴스 조회", description = "사용자에 맞는 추천 뉴스를 조회합니다.")
    public ResponseEntity<List<NewsRecommendGetResponseDTO>> getRecommendedNewsList(
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info(">>> [GET] /news/recommend - 요청 파라미터");
        Long userId;
        if (token == null || token.isEmpty())
            userId = 0L;
        else
            userId = userService.getUser(token).getUserId();
        List<NewsRecommendGetResponseDTO> newsRecommendGetResponseDTOList = newsService.getRecommendedNewsList(userId);

        return ResponseEntity.status(200).body(newsRecommendGetResponseDTOList);
    }

    @GetMapping(value = "/daily")
    @Operation(summary = "데일리 뉴스 + 퀴즈 조회", description = "사용자의 데일리 뉴스를 조회합니다.")
    public ResponseEntity<NewsDailyGetResponseDTO> getDailyNews(
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization") String token) {
        log.info(">>> [GET] /news/daily-news - 요청 파라미터");
        if (token == null || token.isEmpty())
            throw new NoTokenRequestException("token이 비어있습니다");
        Long userId = userService.getUser(token).getUserId();
        NewsDailyGetResponseDTO newsDailyGetResponseDTO = newsService.getDailyContent(userId);
        return ResponseEntity.status(200).body(newsDailyGetResponseDTO);
    }

    @PostMapping(value="/daily")
    @Operation(summary = "데일리 퀴즈 결과 저장", description = "사용자의 데일리 퀴즈 결과를 저장합니다.")
    public ResponseEntity<BaseResponseBody> checkDailyQuizResult(
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization") String token) {
        if (token == null || token.isEmpty())
            throw new NoTokenRequestException("token이 비어있습니다");
        Long userId = userService.getUser(token).getUserId();

        log.info(">>> [POST] /news/daily - 요청 파라미터: userId-{}",  userId);

        newsService.saveDailyQuiz(userId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "데일리 퀴즈 결과가 저장되었습니다."));
    }

    // 카테고리 정보를 int로 프론트에서 받아옴
    @PostMapping(value = "/{newsId}/collect-news-card")
    @Operation(summary = "뉴스 카드 수집", description = "읽은 뉴스를 요약하고 카드를 수집합니다.")
    public ResponseEntity<BaseResponseBody> collectNewsCard(
            @RequestBody NewsCardRequestDTO requestDto,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [POST] /news/{}/collect-news-card - 요청 파라미터 : newsId - {}, userId - {}", requestDto.getNewsId(), requestDto.getNewsId(), userId);

        if (requestDto == null) {
            throw new CustomIllegalStateException("해당 뉴스 카드 요청 dto를 찾을 수 없습니다. " + requestDto);
        }
        newsService.collectNewsCard(userId, requestDto);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴스 카드를 수집했습니다."));
    }


    @GetMapping(value = "/news-card-list")
    @Operation(summary = "뉴스 카드 리스트", description = "뉴스 카드 리스트를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getNewsCardList(
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token,
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new IllegalStateException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [GET] /news/news-card-list - 요청 파라미터 : userId - {}", userId);

        Map<String, Object> cardList = newsService.getCardList(userId, page);
        return ResponseEntity.status(200).body(cardList);
    }


    @GetMapping(value = "/news-card-list/{newsId}")
    @Operation(summary = "뉴스 카드", description = "뉴스 카드의 상세 정보를 반환합니다.")
    public ResponseEntity<NewsCardInfoGetResponseDTO> getNewsCardDetail(
            @PathVariable("newsId") Long newsId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new IllegalStateException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [GET] /news/news-card-list - 요청 파라미터 : userId - {}, cardId - {}", userId, newsId);

        NewsCardInfoGetResponseDTO cardInfo = newsService.getCardInfo(userId, newsId);
        return ResponseEntity.status(200).body(cardInfo);
    }


    @PostMapping(value = "/{newsId}/bookmark")
    @Operation(summary = "뉴스 북마크 등록", description = "해당 뉴스를 북마크합니다.")
    public ResponseEntity<BaseResponseBody> bookmarkNews(
            @PathVariable("newsId") Long newsId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        if (newsId == null) {
            throw new CustomIllegalStateException("해당 아이디의 뉴스를 찾을 수 없습니다.: " + newsId);
        }

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [POST] /news/{}/bookmark - 요청 파라미터: newsId - {}, userId - {}", newsId, newsId, userId);
        newsService.bookmark(userId, newsId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴스를 북마크했습니다."));
    }


    @DeleteMapping(value = "/{newsId}/bookmark")
    @Operation(summary = "뉴스 북마크 삭제", description = "해당 뉴스 북마크를 삭제합니다.")
    public ResponseEntity<BaseResponseBody> deleteBookmark(
            @PathVariable("newsId") Long newsId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        if (newsId == null) {
            throw new CustomIllegalStateException("해당 아이디의 뉴스를 찾을 수 없습니다.: " + newsId);
        }

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [DELETE] /news/{}/bookmark - 요청 파라미터: newsId - {}, userId - {}", newsId, newsId, userId);

        newsService.deleteBookmark(newsId, userId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴스를 북마크를 삭제했습니다."));
    }


    @PostMapping("/{newsId}/like")
    @Operation(summary = "뉴스 좋아요 등록", description = "해당 뉴스가 좋습니다.")
    public ResponseEntity<BaseResponseBody> likeNews(
            @PathVariable("newsId") Long newsId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        if (newsId == null) {
            throw new CustomIllegalStateException("해당 아이디의 뉴스를 찾을 수 없습니다." + newsId);
        }

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [POST] /news/{}/like - 요청 파라미터: newsId - {}, userId - {}", newsId, newsId, userId);

        newsService.likeNews(userId, newsId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴스가 좋습니다."));
    }


    @DeleteMapping(value = "/{newsId}/like")
    @Operation(summary = "뉴스 좋아요 취소", description = "해당 뉴스 좋아요를 취소합니다.")
    public ResponseEntity<BaseResponseBody> deleteNewsLike(
            @PathVariable("newsId") Long newsId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        if (newsId == null) {
            throw new CustomIllegalStateException("해당 아이디의 뉴스를 찾을 수 없습니다." + newsId);
        }

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [DELETE] /news/{}/like - 요청 파라미터: newsId - {}, userId - {}", newsId, newsId, userId);

        newsService.deleteLike(userId, newsId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴스의 좋아요를 삭제했습니다."));
    }
}

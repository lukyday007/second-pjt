package com.newzy.backend.domain.newzy.service;

import com.newzy.backend.domain.image.service.ImageService;
import com.newzy.backend.domain.news.dto.response.NewsListGetResponseDTO;
import com.newzy.backend.domain.newzy.dto.request.NewzyListGetRequestDTO;
import com.newzy.backend.domain.newzy.dto.request.NewzyRequestDTO;
import com.newzy.backend.domain.newzy.dto.response.NewzyImageResponseDTO;
import com.newzy.backend.domain.newzy.dto.response.NewzyListGetResponseDTO;
import com.newzy.backend.domain.newzy.dto.response.NewzyResponseDTO;
import com.newzy.backend.domain.newzy.entity.Newzy;
import com.newzy.backend.domain.newzy.entity.NewzyBookmark;
import com.newzy.backend.domain.newzy.entity.NewzyLike;
import com.newzy.backend.domain.newzy.repository.*;
import com.newzy.backend.domain.user.entity.User;
import com.newzy.backend.domain.user.repository.UserRepository;
import com.newzy.backend.global.exception.CustomIllegalStateException;
import com.newzy.backend.global.exception.EntityIsFoundException;
import com.newzy.backend.global.exception.EntityNotFoundException;
import com.newzy.backend.global.exception.NotValidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.newzy.backend.domain.newzy.entity.QNewzy.newzy;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NewzyServiceImpl implements NewzyService {

    private final NewzyRepository newzyRepository;
    private final NewzyBookmarkRepository bookmarkRepository;
    private final NewzyLikeRepository newzyLikeRepository;
    private final NewzyRepositorySupport newzyRepositorySupport;
    private final UserRepository userRepository;
    private final NewzyBookmarkRepository newzyBookmarkRepository;
    private final ImageService imageService;
    private final NewzyLikeRepositorySupport newzyLikeRepositorySupport;
    private final NewzyBookmarkRepositorySupport newzyBookmarkRepositorySupport;

    private final RedisTemplate<String, String> redisTemplate;


    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getNewzyList(NewzyListGetRequestDTO requestDTO, Long userId) {
        log.info(">>> getNewzyList - dto: {}", requestDTO);

        int page = requestDTO.getPage();
        int category = requestDTO.getCategory();
        int sort = requestDTO.getSort();
        String keyword = requestDTO.getKeyword();

        String todayDate = LocalDate.now().toString();  // 오늘 날짜

        // 내가 쓴 뉴지 목록이 아닐 경우 userId = 0
        Map<String, Object> newzyList = newzyRepositorySupport.findNewzyList(page, category, keyword, sort, userId);

        List<NewzyListGetResponseDTO> newzyListGetResponseDTOs = (List<NewzyListGetResponseDTO>) newzyList.get("newzyList");

        for (NewzyListGetResponseDTO newzy : newzyListGetResponseDTOs) {
            String redisKey = "ranking:newzy:" + todayDate + ":" + newzy.getNewzyId();  // Redis 키
            String redisHit = redisTemplate.opsForValue().get(redisKey);  // Redis에서 조회수 가져오기
            if (redisHit != null) {
                newzy.setHit(newzy.getHit() + Integer.parseInt(redisHit));  // 조회수가 있을 경우 DTO에 설정
            }
        }

        if (newzyList.isEmpty()) {
            throw new EntityNotFoundException("일치하는 뉴지 데이터를 조회할 수 없습니다.");
        }

        return newzyList;
    }


    @Override
    public NewzyResponseDTO getNewzyDetail(Long userId, Long newzyId) {
        log.info(">>> newzyServiceImpl getNewzyDetail - userId: {}, newzyId: {}", userId, newzyId);

        Newzy newzy = newzyRepository.findById(newzyId).orElseThrow(
                () -> new EntityNotFoundException("일치하는 뉴지 데이터를 찾을 수 없습니다.")
        );
        // redis 조회수 증가
        String todayDate = LocalDate.now().toString();  // 오늘 날짜
        String redisKey = "ranking:newzy:" + todayDate + ":" + newzyId;  // Redis 키

        Long hit = redisTemplate.opsForValue().increment(redisKey);

        // 키가 새로 생성된 경우에만 만료 시간 설정 (24시간)
        if (hit == 1) {
            redisTemplate.expire(redisKey, Duration.ofDays(2));  // 24시간 만료 설정
        }

//        newzy.setHit(newzy.getHit() + 1);
//        newzyRepository.save(newzy);

        NewzyResponseDTO newzyResponseDTO = NewzyResponseDTO.convertToDTO(newzy);
        newzyResponseDTO.setHit((int) (newzyResponseDTO.getHit() + hit));

        if (userId != 0) {
            User user = userRepository.findByUserId(userId).orElseThrow(
                    () -> new EntityNotFoundException("일치하는 유저를 찾을 수 없습니다.")
            );

            boolean isLiked = newzyLikeRepositorySupport.isLikedByUser(userId, newzyId);
            if (isLiked) newzyResponseDTO.setLiked(true);
            boolean isBookmarked = newzyBookmarkRepositorySupport.isBookmarkedByUser(userId, newzyId);
            if (isBookmarked) newzyResponseDTO.setBookmakred(true);
        }

        return newzyResponseDTO;
    }


    @Override
    @Transactional(readOnly = true)
    public List<NewzyListGetResponseDTO> getHotNewzyList() {
        String yesterdayDate = LocalDate.now().minusDays(1).toString();  // 오늘 날짜
        String pattern = "ranking:newzy:" + yesterdayDate + ":*";  // 오늘 날짜의 모든 뉴지 조회수

        Set<String> keys = redisTemplate.keys(pattern);  // 해당 패턴에 맞는 키 가져오기
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();  // 만약 키가 없으면 빈 리스트 반환
        }

        // Redis에서 각 키에 대한 조회수 정보를 가져옴
        List<String> keyList = new ArrayList<>(keys);
        List<String> values = redisTemplate.opsForValue().multiGet(keyList);
        if (values == null || values.isEmpty()) {
            throw new NotValidRequestException("조회수가 없습니다.");
        }

        // 키와 조회수를 쌍으로 묶어서 리스트로 만듦
        List<Map.Entry<String, Integer>> keyValueList = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            String value = values.get(i);
            if (value != null) {
                keyValueList.add(new AbstractMap.SimpleEntry<>(key, Integer.parseInt(value)));
            }
        }
        // 조회수(value) 기준으로 내림차순 정렬 후 상위 3개의 키 추출
        List<String> topNewzyKeys = keyValueList.stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())  // 조회수를 기준으로 내림차순 정렬
                .limit(4)  // 상위 3개의 키 추출
                .map(entry -> {
                    String[] keyParts = entry.getKey().split(":");
                    if (keyParts.length > 3) {
                        return keyParts[3];  // key에서 newsId 추출
                    } else {
                        log.warn("Invalid key format: " + entry.getKey());
                        return null;  // 잘못된 형식의 키는 무시
                    }
                })
                .filter(Objects::nonNull)  // null 값 필터링
                .collect(Collectors.toList());
        log.info(topNewzyKeys.toString());
        // 상위 4개의 newzyId에 해당하는 Newzy 객체들을 데이터베이스에서 조회한 후 DTO로 변환
        return topNewzyKeys.stream()
                .map(newzyId -> newzyRepository.findById(Long.parseLong(newzyId))
                        .map(NewzyListGetResponseDTO::convertToDTO)  // News 객체를 DTO로 변환
                        .orElseThrow(() -> new EntityNotFoundException("해당 뉴지 데이터를 찾을 수 없습니다.")))
                .collect(Collectors.toList());
    }


    @Override
    public NewzyImageResponseDTO convertImgUrl(MultipartFile[] files) {
        String[] urls = imageService.uploadImages(files);
        return NewzyImageResponseDTO.builder()
                .url(urls[0])
                .build();
    }


    @Override
    public void save(Long userId, NewzyRequestDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저 엔티티를 찾을 수 없습니다."));

        // HTML 파싱 적용 by Jsoup
        String content = dto.getContent();
        String ContentText = parseHtmlToText(content);
        String thumbnailUrl = extractFirstImageSrc(content);

        Newzy newzy = Newzy.convertToEntity(user, dto, thumbnailUrl);
        newzy.setContentText(ContentText);

        // 경험치 업데이트
        user.setExp(user.getExp()+15);

        // 이미지 업로드 및 뉴지와 이미지 매핑 처리
//        if (dto.getImages() != null && dto.getImages().length > 0) {
//            String[] uploadedUrls = imageService.newzyUploadImages(dto.getImages(), newzy.getNewzyId(), 0);
//            newzy.setThumbnail(uploadedUrls[0]);  // 첫 번째 이미지를 썸네일로 설정
//        }
        userRepository.save(user);
        newzyRepository.save(newzy);
    }


    @Override
    public NewzyResponseDTO update(Long userId, Long newzyId, NewzyRequestDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저 엔티티를 찾을 수 없습니다."));

        // HTML 파싱 적용 by Jsoup
        String content = dto.getContent();
        String ContentText = parseHtmlToText(content);
        String thumbnailUrl = extractFirstImageSrc(content);

        Newzy updatedNewzy = Newzy.convertToEntity(user, newzyId, dto, ContentText, thumbnailUrl);

        Newzy newzy = newzyRepository.updateNewzyInfo(updatedNewzy);
        NewzyResponseDTO newzyResponseDTO = NewzyResponseDTO.convertToDTO(newzy);

        return newzyResponseDTO;
    }


    // HTML 파싱
    private String parseHtmlToText(String content) {
        Document document = Jsoup.parse(content);
        return document.text();
    }


    // HTML 파싱: 첫 번째 이미지 src 추출
    private String extractFirstImageSrc(String content) {
        Document document = Jsoup.parse(content);
        Element firstImg = document.select("img").first();

        // 첫 번째 이미지 태그가 있으면 src 속성 반환, 없으면 null 반환
        return (firstImg != null) ? firstImg.attr("src") : null;
    }


    @Override
    public void delete(Long userId, Long newzyId) {
        Newzy newzy = newzyRepository.findById(newzyId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴스를 찾을 수 없습니다: " + newzyId));

        if (newzy.isDeleted()) {
            throw new CustomIllegalStateException("이미 삭제된 뉴지 입니다.");
        }

        if (userId.equals(newzy.getUser().getUserId())) {
            newzyRepository.deleteNewzyById(newzyId);
        }
    }

    @Override
    public void bookmark(Long userId, Long newzyId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저 데이터를 찾을 수 없습니다."));

        Newzy newzy = newzyRepository.findById(newzyId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 뉴지를 찾을 수 없습니다: " + newzyId));

        boolean isBookmark = newzyBookmarkRepository.existsByUserAndNewzy(user, newzy);

        if (isBookmark) {
            throw new EntityIsFoundException("이미 북마크가 존재합니다.");
        }

        NewzyBookmark newzyBookmark = new NewzyBookmark();
        newzyBookmark.setUser(user);
        newzyBookmark.setNewzy(newzy);

        newzyBookmarkRepository.save(newzyBookmark);
    }


    @Override
    public void deleteBookmark(Long userId, Long newzyId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("일치하는 유저 데이터가 없습니다."));
        Newzy newzy = newzyRepository.findById(newzyId).orElseThrow(() -> new EntityNotFoundException("일치하는 뉴지 데이터가 없습니다."));
        boolean isBookmark = newzyBookmarkRepository.existsByUserAndNewzy(user, newzy);

        if (!isBookmark) {
            throw new EntityNotFoundException("해당하는 북마크 데이터가 없습니다.");
        }

        newzyBookmarkRepository.deleteByUserAndNewzy(user, newzy);
    }


    @Override
    public void likeNewzy(Long userId, Long newzyId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("일치하는 유저 데이터가 없습니다."));
        Newzy newzy = newzyRepository.findById(newzyId).orElseThrow(() -> new EntityNotFoundException("일치하는 뉴지 데이터가 없습니다."));

        boolean isLike = newzyLikeRepository.existsByUserAndNewzy(user, newzy);

        if (isLike) {
            throw new EntityIsFoundException("이미 뉴지 좋아요가 존재합니다.");
        }

        NewzyLike like = new NewzyLike();
        like.setUser(user);
        like.setNewzy(newzy);

        newzy.setLikeCnt(newzy.getLikeCnt() + 1);
        newzyRepository.save(newzy);
        newzyLikeRepository.save(like);
    }


    @Override
    public void deleteNewzyLike(Long userId, Long newzyId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("일치하는 유저 데이터가 없습니다."));
        Newzy newzy = newzyRepository.findById(newzyId).orElseThrow(() -> new EntityNotFoundException("일치하는 뉴지 데이터가 없습니다."));

        boolean isLike = newzyLikeRepository.existsByUserAndNewzy(user, newzy);

        if (!isLike) {
            throw new EntityIsFoundException("해당하는 뉴지 좋아요가 없습니다.");
        }

        newzy.setLikeCnt(newzy.getLikeCnt() - 1);
        newzyRepository.save(newzy);
        newzyLikeRepository.deleteByUserAndNewzy(user, newzy);
    }

}

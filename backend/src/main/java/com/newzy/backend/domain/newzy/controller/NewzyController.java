package com.newzy.backend.domain.newzy.controller;

import com.newzy.backend.domain.newzy.dto.request.NewzyRequestDTO;
import com.newzy.backend.domain.newzy.dto.response.NewzyListGetResponseDTO;
import com.newzy.backend.domain.newzy.dto.response.NewzyResponseDTO;
import com.newzy.backend.domain.newzy.service.NewzyServiceImpl;
import com.newzy.backend.domain.user.service.UserService;
import com.newzy.backend.global.exception.CustomIllegalStateException;
import com.newzy.backend.global.exception.NoTokenRequestException;
import com.newzy.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/newzy")
public class NewzyController {

    private final NewzyServiceImpl newzyServiceImpl;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "뉴지 게시글 추가", description = "새로운 뉴지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> create(
            @Parameter(description = "JWT", required = false)
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody @Validated NewzyRequestDTO dto
    ){
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저토큰이 없습니다.");
        }

        log.info(">>> [POST] /newzy - 요청 파라미터: dto - {}, userId - {}", dto.toString(), userId);
        newzyServiceImpl.save(userId, dto);

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "뉴지 등록이 완료되었습니다."));
    }


    @GetMapping
    @Operation(summary = "모든 뉴지 게시글 조회", description = "모든 뉴지 게시글을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getNewzyList(
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "category (0: 시사, 1: 문화, 2: 자유)")
            @RequestParam(value = "category", required = false, defaultValue = "3") int category
    ){
        log.info(">>> [GET] /newzy - 요청 파라미터: page - {}, category - {}", page, category);
        Map<String, Object> newzyListWithLastPage = newzyServiceImpl.getNewzyListWithLastPage(page, category);

        return ResponseEntity.status(200).body(newzyListWithLastPage);
    }


    @GetMapping(value = "/{newzyId}")
    @Operation(summary = "뉴지 상세 조회", description = "해당 뉴지를 상세 조회합니다.")
    public ResponseEntity<NewzyResponseDTO> getNewzy(@PathVariable Long newzyId){
        log.info(">>> [GET] /newzy/{} - 요청 파라미터: newzyId - {}", newzyId, newzyId);
        NewzyResponseDTO newzyDetail = newzyServiceImpl.getNewzyDetail(newzyId);

        return ResponseEntity.status(200).body(newzyDetail);
    }


    @GetMapping(value = "/hot")
    @Operation(summary = "많이 본 뉴지 조회", description = "조회수가 많은 뉴지를 조회합니다.")
    public ResponseEntity<List<NewzyListGetResponseDTO>> getHotNewzy(){
        log.info(">>> [GET] /newzy/hot - 요청 파라미터");
        List<NewzyListGetResponseDTO> hotNewzyList = newzyServiceImpl.getHotNewzyList();

        return ResponseEntity.status(200).body(hotNewzyList);
    }


    @PatchMapping(value = "/{newzyId}")
    @Operation(summary = "뉴지 게시글 수정", description = "해당 뉴지를 수정합니다.")
    public ResponseEntity<BaseResponseBody> updateNewzyInfo (
            @PathVariable("newzyId") Long newzyId,
            @Parameter(description = "JWT", required = false)
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody @Validated NewzyRequestDTO dto){
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [PATCH] /newzy/{} - 요청 파라미터: newzyId - {}, userId - {}", newzyId, newzyId, userId);
        newzyServiceImpl.update(userId, newzyId, dto);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "뉴지 수정이 완료되었습니다."));
    }


    @DeleteMapping(value = "/{newzyId}")
    @Operation(summary = "뉴지 게시글 삭제", description = "해당 뉴지를 삭제합니다.")
    public ResponseEntity<BaseResponseBody> deleteNewzyInfo (
            @PathVariable("newzyId") Long newzyId,
            @Parameter(description = "JWT", required = false)
            @RequestHeader(value = "Authorization", required = false) String token
    ){
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [DELETE] /newzy/{} - 요청 파라미터: newzyId - {}, userId - {}", newzyId, newzyId, userId);
        newzyServiceImpl.delete(userId, newzyId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "뉴지 삭제가 완료되었습니다."));
    }


    @PostMapping(value = "/{newzyId}/bookmark")
    @Operation(summary = "뉴지 북마크 등록", description = "해당 뉴지를 북마크합니다.")
    public ResponseEntity<BaseResponseBody> bookmarkNewzy (
            @PathVariable("newzyId") Long newzyId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ){
        if (newzyId == null) {
            throw new CustomIllegalStateException("해당 아이디의 뉴지를 찾을 수 없습니다.: " + newzyId);
        }

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [POST] /newzy/{}/bookmark - 요청 파라미터: newzyId - {}, userId - {}", newzyId, newzyId, userId);
        newzyServiceImpl.bookmark(userId, newzyId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴지를 북마크했습니다."));
    }


    @DeleteMapping(value = "/{newzyId}/bookmark")
    @Operation(summary = "뉴지 북마크 삭제", description = "해당 뉴지 북마크를 삭제합니다.")
    public ResponseEntity<BaseResponseBody> deleteBookmark (
            @PathVariable("newzyId") Long newzyId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ){
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [DELETE] /newzy/{}/bookmark - 요청 파라미터: newzyId - {}, userId - {}", newzyId, newzyId, userId);        newzyServiceImpl.deleteBookmark(userId, newzyId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴지를 북마크를 삭제했습니다."));
    }


    @PostMapping("/{newzyId}/like")
    @Operation(summary = "뉴지 좋아요 등록", description = "해당 뉴지가 좋습니다.")
    public ResponseEntity<BaseResponseBody> likeNewzy (
            @PathVariable("newzyId") Long newzyId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ){
        if (newzyId == null) {
            throw new CustomIllegalStateException("해당 아이디의 뉴지를 찾을 수 없습니다.: " + newzyId);
        }

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [POST] /newzy/{}/like - 요청 파라미터: newzyId - {}, userId - {}", newzyId, newzyId, userId);

        newzyServiceImpl.likeNewzy(userId, newzyId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴지가 좋습니다."));
    }


    @DeleteMapping(value = "/{newzyId}/like")
    @Operation(summary = "뉴지 좋아요 취소", description = "해당 뉴지 좋아요를 취소합니다.")
    public ResponseEntity<BaseResponseBody> deleteNewzyLike (
            @PathVariable("newzyId") Long newzyId,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ){
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [DELETE] /newzy/{}/like - 요청 파라미터: newzyId - {}, userId - {}", newzyId, newzyId, userId);
        newzyServiceImpl.deleteNewzyLike(userId, newzyId);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "해당 뉴지의 좋아요를 삭제했습니다."));
    }


}
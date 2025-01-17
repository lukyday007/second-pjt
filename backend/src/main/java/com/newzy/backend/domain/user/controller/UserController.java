package com.newzy.backend.domain.user.controller;

import com.newzy.backend.domain.newzy.dto.request.NewzyListGetRequestDTO;
import com.newzy.backend.domain.newzy.service.NewzyService;
import com.newzy.backend.domain.user.dto.request.UserUpdateRequestDTO;
import com.newzy.backend.domain.user.dto.response.*;
import com.newzy.backend.domain.user.service.UserService;
import com.newzy.backend.global.exception.NoTokenRequestException;
import com.newzy.backend.global.exception.NotValidRequestException;
import com.newzy.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final NewzyService newzyService;
    private static final int NICKNAME_MAX_LENGTH = 10;

    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
    @PatchMapping
    public ResponseEntity<BaseResponseBody> updateUser(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody UserUpdateRequestDTO userUpdateRequestDto) {
        log.info(">>> [PATCH] /user - 회원 수정 요청 데이터: {}", userUpdateRequestDto);

        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        if (userUpdateRequestDto.getNickname() == null || userUpdateRequestDto.getNickname().trim().isEmpty()) {
            log.error(">>> [PATCH] /user - 닉네임이 필수 필드입니다.");
            throw new NotValidRequestException("닉네임은 필수 입력 값입니다.");
        }
        if (userUpdateRequestDto.getNickname().length() > NICKNAME_MAX_LENGTH) {
            log.error(">>> [PATCH] /user - 닉네임은 최대 {}자 입니다.", NICKNAME_MAX_LENGTH);
            throw new NotValidRequestException("닉네임은 최대 " + NICKNAME_MAX_LENGTH + "자 입니다.");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [PATCH] /user - Bearer 제거 후 토큰: {}", token);
        }

        UserUpdateResponseDTO user = userService.updateUser(token, userUpdateRequestDto);
        log.info(">>> [PATCH] /user - 회원 수정 완료: {}", user);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "회원 정보 수정이 완료되었습니다."));
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 처리하고 Redis에서 토큰을 삭제합니다.")
    @GetMapping("/logout")
    public ResponseEntity<BaseResponseBody> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [GET] /user/logout - 로그아웃 요청: {}", token);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /user/logout - Bearer 제거 후 토큰: {}", token);
        }
        userService.userSignOut(token);
        log.info(">>> [GET] /user/logout - 로그아웃 완료");
        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "로그아웃이 완료되었습니다."));
    }


    @Operation(summary = "중복 닉네임 확인", description = "사용 가능한 닉네임인지 확인합니다.")
    @GetMapping("/check/{nickname}")
    public ResponseEntity<BaseResponseBody> checkNickname(
            @PathVariable("nickname") String nickname
    ) {
        log.info(">>> [GET] /user/check/{} - 닉네임 중복 확인 요청", nickname);
        boolean isPresent = userService.checkUserNickname(nickname);

        if (isPresent) {
            log.warn(">>> [GET] /user/check/{} - 이미 존재하는 닉네임입니다.", nickname);
            return ResponseEntity.status(409).body(BaseResponseBody.of(409, "이미 존재하는 닉네임입니다."));
        } else {
            log.info(">>> [GET] /user/check/{} - 사용 가능한 닉네임입니다.", nickname);
            return ResponseEntity.status(200).body(BaseResponseBody.of(200, "사용 가능한 닉네임입니다."));
        }
    }


    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<UserInfoResponseDTO> getUser(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [GET] /user - 회원 정보 조회 요청: {}", token);
        UserInfoResponseDTO userGetResponseDto = userService.getUser(token);
        log.info(">>> [GET] /user - 회원 정보 조회 완료: {}", userGetResponseDto);
        return ResponseEntity.status(200).body(userGetResponseDto);
    }


    @GetMapping("/profile/{nickname}")
    @Operation(summary = "회원 정보 조회 By Nickname", description = "회원 정보를 조회합니다.")
    public ResponseEntity<UserProfileResponseDTO> getUserByNickname(
            @PathVariable(value = "nickname") String nickname
    ) {
        log.info(">>> [GET] /user/profile/{} - 회원 정보 조회 요청: {}", nickname);
        UserProfileResponseDTO userProfileResponseDto = userService.getUserProfile(nickname);
        log.info(">>> [GET] /user/profile/{} - 회원 정보 조회 완료: {}", nickname, userProfileResponseDto);
        return ResponseEntity.status(200).body(userProfileResponseDto);
    }


    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리합니다.")
    @DeleteMapping
    public ResponseEntity<BaseResponseBody> deleteUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [DELETE] /user - 회원 탈퇴 요청: {}", token);

        userService.deleteUser(token);

        log.info(">>> [DELETE] /user - 회원 탈퇴 완료");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(BaseResponseBody.of(204, "회원 탈퇴 되었습니다."));
    }

    @Operation(summary = "이메일 사용자 조회", description = "이메일 기준으로 사용자를 조회합니다.")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserInfoResponseDTO> getUserByEmail(@PathVariable("email") String email) {
        UserInfoResponseDTO responseDto = userService.getUserByEmail(email);
        log.info(">>> 상대 프로필 조회, 이메일, 반환값 : {}, {}", email, responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "첫 로그인 여부 확인", description = "회원의 첫 로그인 여부를 확인합니다.")
    @GetMapping("/first/login")
    public ResponseEntity<UserFirstLoginResponseDTO> checkFirstLogin(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [GET] /user/first/login - 첫 로그인 여부 확인 요청: {}", token);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /user/first/login - Bearer 제거 후 토큰: {}", token);
        }

        UserFirstLoginResponseDTO userFirstLoginResponseDTO = userService.isFirstLogin(token);  // 서비스 메소드 호출
        log.info(">>> [GET] /user/is-first-login - 첫 로그인 여부: {}", userFirstLoginResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(userFirstLoginResponseDTO);
    }


    @PostMapping("/upload-profile")
    public ResponseEntity<BaseResponseBody> uploadProfileImage(@RequestPart(value = "profile", required = false) MultipartFile[] profile,
                                                               @RequestHeader(value = "Authorization", required = false) String token) {
        log.info(">>> [POST] /api/users/upload-profile - 프로필 사진 업로드 요청");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /api/users/upload-profile - Bearer 제거 후 토큰: {}", token);
        }

        UserInfoResponseDTO user = userService.updateProfileImage(token, profile);
        log.info(">>> [PATCH] /api/users/upload-profile - 회원 수정 완료: {}", user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(BaseResponseBody.of(204, "프로필 사진 업데이트가 완료되었습니다."));
    }


    /*
        유저 팔로우, 북마크, 좋아요 기능
    */


    @PostMapping(value = "/{nickname}/follower")
    @Operation(summary = "팔로우", description = "선택한 상대를 구독합니다.")
    public ResponseEntity<BaseResponseBody> followers(
            @PathVariable String nickname,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [POST] /news/{}/followings - 요청 파라미터: nickname - {}, userId - {}", nickname, nickname, userId);

        userService.followUser(userId, nickname);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "{nickname}을 팔로워했습니다."));
    }


    @DeleteMapping(value = "/{nickname}/follower")
    @Operation(summary = "팔로우 취소", description = "선택한 상대를 언팔로우합니다.")
    public ResponseEntity<BaseResponseBody> deletefollower(
            @PathVariable String nickname,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [DELETE] /news/{}/followings - 요청 파라미터: nickname - {}, userId - {}", nickname, nickname, userId);

        userService.deleteFollower(userId, nickname);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "{nickname}을 언팔로우했습니다."));
    }


    // /user/{nickname}/followings?page={page}
    @GetMapping(value = "/followings-list/{nickname}")
    @Operation(summary = "팔로잉 목록", description = "내가 팔로잉한 사람들의 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getFollowings(
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {
        log.info(">>> [GET] /news/{}/followings - 요청 파라미터: nickname - {}, page - {}", nickname, nickname, page);

        Map<String, Object> followingList = userService.getFollowingList(page, nickname);

        return ResponseEntity.status(HttpStatus.OK).body(followingList);
    }

    // /user/{nickname}/followers?page={page}
    @GetMapping(value = "/followers-list/{nickname}")
    @Operation(summary = "팔로워 목록", description = "나를 팔로우한 사람들의 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getFollowers(
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {

        log.info(">>> [GET] /news/{}/followers - 요청 파라미터: nickname - {}, page - {}", nickname, nickname, page);

        Map<String, Object> followerList = userService.getFollowerList(page, nickname);

        return ResponseEntity.status(HttpStatus.OK).body(followerList);
    }


    @GetMapping(value = "/news-bookmark")
    @Operation(summary = "북마크한 뉴스 목록", description = "유저가 북마크한 뉴스 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getNewsBookmarkList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [GET] /news/news-bookmark - 요청 파라미터: userId - {}, page - {}", userId, page);
        Map<String, Object> NewsBookmarkList = userService.getNewsBookmarkList(page, userId);
        return ResponseEntity.status(HttpStatus.OK).body(NewsBookmarkList);
    }

    @GetMapping(value = "/news-like")
    @Operation(summary = "북마크한 뉴스 목록", description = "유저가 북마크한 뉴스 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getNewsLikeList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [GET] /news/news-like - 요청 파라미터: userId - {}, page - {}", userId, page);
        Map<String, Object> newsLikeList = userService.getNewsLikeList(page, userId);
        return ResponseEntity.status(HttpStatus.OK).body(newsLikeList);
    }


    @GetMapping(value = "/newzy-bookmark")
    @Operation(summary = "북마크한 뉴스 목록", description = "유저가 북마크한 뉴스 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getNewzyBookmarkList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [GET] /newzy/newzy-bookmark - 요청 파라미터: userId - {}, page - {}", userId, page);
        Map<String, Object> NewzyBookmarkList = userService.getNewzyBookmarkList(page, userId);
        return ResponseEntity.status(HttpStatus.OK).body(NewzyBookmarkList);
    }

    @GetMapping(value = "/newzy-like")
    @Operation(summary = "북마크한 뉴스 목록", description = "유저가 북마크한 뉴스 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getNewzyLikemarkList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }
        log.info(">>> [GET] /newzy/newzy-like - 요청 파라미터: userId - {}, page - {}", userId, page);
        Map<String, Object> NewzyLikeList = userService.getNewzyLikeList(page, userId);
        return ResponseEntity.status(HttpStatus.OK).body(NewzyLikeList);
    }


    //  내가 작성한 뉴지 목록
    @GetMapping(value = "/newzy-list/{nickname}")
    @Operation(summary = "내가 쓴 뉴지 목록 조회", description = "내가 작성한 뉴지 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getMyNewzyList(
            @Parameter(description = "유저 닉네임")
            @PathVariable String nickname,
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {

        log.info(">>> [GET] /user/newzy-list/{} - 요청 파라미터: nickname- {}, page - {}", nickname, nickname, page);

        Map<String, Object> newzyList = userService.getNewzyListByNickname(page, nickname);

        return ResponseEntity.status(HttpStatus.OK).body(newzyList);
    }


    // 내가 팔로잉한 사람이 쓴 뉴지 목록
    @GetMapping(value = "/followings-newzy-list")
    @Operation(summary = "내가 팔로잉한 사람의 뉴지 목록", description = "내가 팔로잉한 사람의 뉴지 목록을 반환합니다.")
    public ResponseEntity<Map<String, Object>> getFollowingsNewzyList(
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "category (0: 시사, 1: 문화, 2: 자유)")
            @RequestParam(value = "category", required = false, defaultValue = "3") int category,
            @Parameter(description = "정렬기준")
            @RequestParam(value = "sort", required = false, defaultValue = "0") int sort,
            @Parameter(description = "키워드")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "JWT", required = true)
            @RequestHeader(value = "Authorization", required = true) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUser(token).getUserId();
        } else {

        }
        log.info(">>> [GET] /followings-newzy-list - 요청 파라미터: page - {}, category - {}, keyword - {}, sort - {}, userId - {}", page, category, keyword, sort, userId);

        NewzyListGetRequestDTO requestDTO = new NewzyListGetRequestDTO(page, category, sort, keyword);

        Map<String, Object> followingsNewzyList = userService.getFollowingsNewzyList(requestDTO, userId);

        return ResponseEntity.status(200).body(followingsNewzyList);
    }

    @GetMapping(value = "/ranking/card-collector")
    @Operation(summary = "지난 주의 카드왕 조회", description = "주마다 갱신되는 카드왕 조회 API 입니다.")
    public ResponseEntity<UserCardCollectorResponseDTO> getBestCardCollector() {
        log.info(">>> [GET] /user/card-collector");
        UserCardCollectorResponseDTO bestCardCollector = userService.getBestCardCollector();
        return ResponseEntity.status(HttpStatus.OK).body(bestCardCollector);
    }

    @GetMapping(value = "/ranking/newporter")
    @Operation(summary = "지난 주의 뉴포터 조회", description = "주마다 갱신되는 뉴포터 조회 API 입니다.")
    public ResponseEntity<List<UserNewporterResponseDTO>> getNewporter() {
        log.info(">>> [GET] /user/ranking/newporter");
        List<UserNewporterResponseDTO> newporters = userService.getNewporter();
        return ResponseEntity.status(HttpStatus.OK).body(newporters);
    }
}

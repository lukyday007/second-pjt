package com.newzy.backend.domain.user.controller;

import com.newzy.backend.domain.user.dto.request.UserUpdateRequestDTO;
import com.newzy.backend.domain.user.dto.response.UserFirstLoginResponseDTO;
import com.newzy.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.newzy.backend.domain.user.dto.response.UserUpdateResponseDTO;
import com.newzy.backend.domain.user.service.UserService;
import com.newzy.backend.global.exception.NoTokenRequestException;
import com.newzy.backend.global.exception.NotValidRequestException;
import com.newzy.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
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

    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<UserInfoResponseDTO> getUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null)
            throw new NoTokenRequestException("Access 토큰이 필요합니다.");
        log.info(">>> [GET] /user - 회원 정보 조회 요청: {}", token);
        UserInfoResponseDTO userGetResponseDto = userService.getUser(token);
        log.info(">>> [GET] /user - 회원 정보 조회 완료: {}", userGetResponseDto);
        return ResponseEntity.status(200).body(userGetResponseDto);
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


    @PostMapping("/uploadProfileImage")
    public ResponseEntity<BaseResponseBody> uploadProfileImage(@RequestPart(value = "profile", required = false) MultipartFile[] profile,
                                                               @RequestHeader(value = "Authorization", required = false) String token) {
        log.info(">>> [POST] /api/users/uploadProfileImage - 프로필 사진 업로드 요청");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info(">>> [GET] /user/first/login - Bearer 제거 후 토큰: {}", token);
        }

        UserInfoResponseDTO user = userService.updateProfileImage(token, profile);
        log.info(">>> [PATCH] /user - 회원 수정 완료: {}", user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(BaseResponseBody.of(204, "프로필 사진 업데이트가 완료되었습니다."));
    }
}
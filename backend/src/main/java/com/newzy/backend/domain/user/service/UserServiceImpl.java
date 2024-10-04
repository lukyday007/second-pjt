package com.newzy.backend.domain.user.service;

import com.newzy.backend.domain.news.repository.NewsBookmarkRepository;
import com.newzy.backend.domain.news.repository.NewsBookmarkRepositorySupport;
import com.newzy.backend.domain.news.repository.NewsLikeRepositorySupport;
import com.newzy.backend.domain.image.entity.Image;
import com.newzy.backend.domain.image.repository.ImageRepository;
import com.newzy.backend.domain.image.service.ImageService;
import com.newzy.backend.domain.newzy.dto.response.NewzyResponseDTO;
import com.newzy.backend.domain.newzy.repository.NewzyBookmarkRepository;
import com.newzy.backend.domain.newzy.repository.NewzyBookmarkRepositorySupport;
import com.newzy.backend.domain.newzy.repository.NewzyLikeRepositorySupport;
import com.newzy.backend.domain.user.dto.request.AuthRequestDTO;
import com.newzy.backend.domain.user.dto.request.UserInfoRequestDTO;
import com.newzy.backend.domain.user.dto.request.UserUpdateRequestDTO;
import com.newzy.backend.domain.user.dto.response.UserFirstLoginResponseDTO;
import com.newzy.backend.domain.user.dto.response.UserInfoResponseDTO;
import com.newzy.backend.domain.user.dto.response.UserUpdateResponseDTO;
import com.newzy.backend.domain.user.entity.Follow;
import com.newzy.backend.domain.user.entity.User;
import com.newzy.backend.domain.user.repository.FollowRepository;
import com.newzy.backend.domain.user.repository.FollowRepositorySupport;
import com.newzy.backend.domain.user.repository.UserRepository;
import com.newzy.backend.global.auth.JwtProvider;
import com.newzy.backend.global.exception.EntityNotFoundException;
import com.newzy.backend.global.exception.NotValidRequestException;
import com.newzy.backend.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final FollowRepository followRepository;
    private final FollowRepositorySupport followRepositorySupport;
    private final NewsBookmarkRepositorySupport newsBookmarkRepositorySupport;
    private final NewsLikeRepositorySupport newsLikeRepositorySupport;
    private final NewzyBookmarkRepositorySupport newzyBookmarkRepositorySupport;
    private final NewzyLikeRepositorySupport newzyLikeRepositorySupport;


    @Override
    public void save(UserInfoRequestDTO requestDTO) {
        User user = User.convertToEntity(requestDTO);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public UserUpdateResponseDTO updateUser(String token, UserUpdateRequestDTO request) {
        log.info(">>> updateUser - 토큰: {}, 요청 데이터: {}", token, request);
        Long userId = jwtProvider.getUserIdFromToken(token);
        log.info(">>> updateUser - 추출된 사용자 ID: {}", userId);

        Optional<User> optionalUser = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            log.info(">>> updateUser - 사용자 찾음: {}", user);
            user.setNickname(request.getNickname());
            user.setEmail(request.getEmail());
            user.setBirth(request.getBirth());
            user.setInfo(request.getInfo());
            User updatedUser = userRepository.save(user);
            log.info(">>> updateUser - 사용자 업데이트됨: {}", updatedUser);
            return UserUpdateResponseDTO.builder()
                    .birth(updatedUser.getBirth())
                    .nickname(updatedUser.getNickname())
                    .email(updatedUser.getEmail())
                    .info(updatedUser.getInfo())
                    .socialLoginType(updatedUser.getSocialLoginType())
                    .userId(userId)
                    .build();
        } else {
            log.error(">>> updateUser - 사용자를 찾을 수 없음: {}", userId);
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다.");
        }
    }

    @Override
    public void userSignOut(String token) {
        log.info(">>> [USER SIGN OUT] - 사용자 로그아웃 요청: 토큰 = {}", token);

        Long userId = jwtProvider.getUserIdFromToken(token);

        redisUtil.deleteData("user:" + userId);

        log.info(">>> [USER SIGN OUT] - Redis에서 토큰 삭제 완료: 유저 ID = {}", userId);

        SecurityContextHolder.clearContext();

        log.info(">>> [USER SIGN OUT] - SecurityContextHolder 초기화 완료");
    }

    @Override
    public UserFirstLoginResponseDTO isFirstLogin(String token) {
        log.info(">>> [IS FIRST LOGIN] - 사용자 첫 로그인 확인: 토큰 = {}", token);
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserFirstLoginResponseDTO.builder()
                .isFirstLogin(user.getBirth() == null)
                .build();  // birth가 null이면 첫 로그인
    }

    @Transactional
    @Override
    public UserInfoResponseDTO updateProfileImage(String token, MultipartFile[] profile) {
        log.info(">>> updateProfileImage - 토큰: {}, 요청 데이터: {}", token, profile);
        Long userId = jwtProvider.getUserIdFromToken(token);
        log.info(">>> updateProfileImage - 추출된 사용자 ID: {}", userId);

        Optional<User> optionalUser = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 회원 대표 사진 변경
            if (profile != null) {
                if (profile.length > 1)
                    throw new NotValidRequestException("회원 프로필 사진은 한 장만 등록할 수 있습니다.");
                String[] imageUrl = imageService.uploadImages(profile);
                Image userImage = imageRepository.findByImageUrl(imageUrl[0])
                        .orElseThrow(() -> new EntityNotFoundException("회원의 수정한 대표 사진을 불러오는 데 실패하였습니다."));
                user.setImage(userImage);
                User updatedUser = userRepository.save(user);
                log.info(">>> updateUser - 사용자 업데이트됨: {}", updatedUser);
                return UserInfoResponseDTO.convertToDTO(updatedUser);
            }
        } else {
            log.error(">>> updateUser - 사용자를 찾을 수 없음: {}", userId);
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다.");
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteUser(String token) {
        log.info(">>> deleteUser - 토큰: {}", token);
        Long userId = jwtProvider.getUserIdFromToken(token);
        log.info(">>> deleteUser - 추출된 사용자 ID: {}", userId);

        // 사용자 조회
        Optional<User> optionalUser = userRepository.findByUserIdAndIsDeletedFalse(userId);

        if (optionalUser.isPresent()) {
            // redis에서도 지우기
            redisUtil.deleteData("user:" + userId);
            User user = optionalUser.get();
            user.setIsDeleted(true);
            userRepository.save(user);
            log.info(">>> deleteUser - 사용자 삭제 완료: {}", user);
        } else {
            log.error(">>> deleteUser - 사용자를 찾을 수 없음: {}", userId);
            throw new NotValidRequestException("사용자를 찾을 수 없습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewzyResponseDTO> getAllUsers() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public NewzyResponseDTO getUserById(Long id) {
        return null;
    }

    @Override
    public UserInfoResponseDTO getUser(String token) {
        // 1. 토큰에서 사용자 ID 추출
        Long userId;
        try {
            userId = jwtProvider.getUserIdFromToken(token); // JWT 토큰에서 userId 추출
            log.info(">>> getUser - 추출된 사용자 ID: {}", userId);
        } catch (Exception e) {
            log.error(">>> getUser - 토큰으로부터 사용자 ID를 추출하는 데 실패함: {}", e.getMessage());
            throw new NotValidRequestException("유효하지 않은 토큰입니다.");
        }

        // 2. 사용자 조회
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId).orElseThrow(() -> {
            log.error(">>> getUser - 사용자를 찾을 수 없음: {}", userId);
            throw new NotValidRequestException("사용자를 찾을 수 없습니다.");
        });

        log.info(">>> getUser - 사용자 정보: {}", user);

        // 3. 사용자 정보 DTO 반환
        return UserInfoResponseDTO.convertToDTO(user);
    }

    @Override
    public UserInfoResponseDTO getUserByEmail(String email) {

        // 2. 사용자 조회
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> {
            log.error(">>> getUserByEmail - 사용자를 찾을 수 없음: {}", email);
            throw new NotValidRequestException("사용자를 찾을 수 없습니다.");
        });

        log.info(">>> getUserByEmail - 사용자 정보: {}", user);

        // 3. 사용자 정보 DTO 반환
        return UserInfoResponseDTO.convertToDTO(user);
    }

    @Override
    public UserInfoResponseDTO oauthLogin(AuthRequestDTO authRequestDTO) {
        try {
            // 이메일로 기존 사용자 검색
            Optional<User> existingUser = userRepository.findUserByEmailAndSocialLoginType(
                    authRequestDTO.getEmail(), authRequestDTO.getType());

            if (existingUser.isPresent()) {
                log.info("기존 사용자 로그인: {}", existingUser.get().getEmail());
                // 기존 사용자 처리만 하고 반환값 없음
                return UserInfoResponseDTO.convertToDTO(existingUser.get());
            } else {
                // 새로운 사용자 등록
                User user = User.builder()
                        .email(authRequestDTO.getEmail())
                        .nickname(authRequestDTO.getNickname())
                        .password(authRequestDTO.getPassword())
                        .socialLoginType(authRequestDTO.getType())
                        .isDeleted(Boolean.FALSE)
                        .build();
                log.info("새로운 사용자 등록: {}", authRequestDTO.getEmail());
                userRepository.save(user); // 새 사용자 저장
                return UserInfoResponseDTO.convertToDTO(user);
            }
        } catch (Exception e) {
            // 예외 발생 시 로그를 남기고, 예외 처리
            log.error("사용자 처리 중 예외 발생: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 처리 중 문제가 발생했습니다.");
        }
    }

    @Override
    public UserInfoResponseDTO oauthSignup(AuthRequestDTO authRequestDTO) {
        // 새로운 사용자 등록
        Optional<Image> image = imageRepository.findByImageUrl("https://plogbucket.s3.ap-northeast-2.amazonaws.com/e63129aa-4855-43a4-a75b-840668687252_user.png");

        User user = User.builder()
                .email(authRequestDTO.getEmail())
                .nickname(authRequestDTO.getNickname())
                .password(authRequestDTO.getPassword())
                .socialLoginType(authRequestDTO.getType())
                .image(image.get())
                .build();
        log.info("새로운 사용자 등록: {}", authRequestDTO.getEmail());
        userRepository.save(user); // 새 사용자 저장
        return UserInfoResponseDTO.convertToDTO(user);
    }

    @Override
    public void followUser(Long userId, String nickname) {

        User fromUser = userRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저 데이터를 찾을 수 없습니다."));
        User toUSer = userRepository.findByNickname(nickname);
        if (toUSer == null) {
            throw new EntityNotFoundException("해당하는 유저 데이터를 찾을 수 없습니다.");
        }

        Follow follow = new Follow();
        follow.setFromUser(fromUser);
        follow.setToUser(toUSer);
        followRepository.save(follow);
    }

    @Override
    public void deleteFollower(Long userId, String nickname) {

        User fromUser = userRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저 데이터를 찾을 수 없습니다."));
        User toUser = userRepository.findByNickname(nickname);
        if (toUser == null) {
            throw new EntityNotFoundException("해당하는 유저 데이터를 찾을 수 없습니다.");
        }

        Follow follow = followRepository.findByFromUserAndToUser(fromUser, toUser);

        followRepository.delete(follow);
    }


    @Override       // 팔로잉 목록
    public Map<String, Object> getFollowingList(int page, String nickname) {

        return followRepositorySupport.findFollowingList(page, nickname);
    }


    @Override       // 팔로워 목록
    public Map<String, Object> getFollowerList(int page, String nickname) {

        return followRepositorySupport.findFollowerList(page, nickname);
    }


    @Override
    public Map<String, Object> getNewsBookmarkList(int page, Long userId) {
        return newsBookmarkRepositorySupport.findNewsListByNewsBookmark(page, userId);
    }

    @Override
    public Map<String, Object> getNewsLikeList(int page, Long userId) {
        return newsLikeRepositorySupport.findNewsListByNewsLike(page, userId);
    }

    @Override
    public Map<String, Object> getNewzyBookmarkList(int page, Long userId) {
        return newzyBookmarkRepositorySupport.findNewzyListByNewzyBookmark(page, userId);
    }

    @Override
    public Map<String, Object> getNewzyLikeList(int page, Long userId) {
        return newzyLikeRepositorySupport.findNewzyListByNewzyLike(page, userId);
    }


}

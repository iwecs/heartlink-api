package com.ss.heartlinkapi.notification.controller;

import com.ss.heartlinkapi.follow.repository.FollowRepository;
import com.ss.heartlinkapi.follow.service.FollowService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.notification.dto.NotificationDTO;
import com.ss.heartlinkapi.notification.service.NotificationService;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final FollowService followService;

    //    client가 SseEmitter를 넘겨받는 엔드포인트
    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable Long userId) {
        return notificationService.subscribe(userId);
    }


    //    알림 목록을 불러오는 기능
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        List<NotificationDTO> notifications = notificationService.getNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    //    userId를 반환하는 핸들러 메서드
    @GetMapping("/check-userid")
    public ResponseEntity<Long> checkUserId(@AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(user.getUserId());
    }

    //    팔로우 알람 메시지 수락
    @PostMapping("/confirm/{notificationId}")
    public ResponseEntity<String> confirmFollowRequest( @AuthenticationPrincipal CustomUserDetails follower, @PathVariable Long notificationId, @RequestParam Long senderId) {

        UserEntity followingUser = userRepository.findById(follower.getUserId()).get();
        UserEntity followerUser = userRepository.findById(senderId).get();

        followService.acceptFollow(followerUser, followingUser);
        notificationService.deleteById(notificationId);

        return ResponseEntity.ok("follow request accepted");

    }

    //    팔로우 알람 메시지 거절
    @PostMapping("/deny/{notificationId}")
    public ResponseEntity<String> denyFollowRequest(@PathVariable Long notificationId, @AuthenticationPrincipal CustomUserDetails follower, @RequestParam Long senderId) {

        UserEntity followingUser = userRepository.findById(follower.getUserId()).get();
        UserEntity followerUser = userRepository.findById(senderId).get();

        followService.rejectFollow(followerUser, followingUser);
        notificationService.deleteById(notificationId);

        return ResponseEntity.ok("follow request denied");
    }
}

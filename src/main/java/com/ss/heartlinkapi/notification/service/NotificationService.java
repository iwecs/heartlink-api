package com.ss.heartlinkapi.notification.service;

import com.ss.heartlinkapi.follow.entity.FollowEntity;
import com.ss.heartlinkapi.follow.repository.FollowRepository;
import com.ss.heartlinkapi.follow.service.FollowService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.notification.dto.NotificationCommentDTO;
import com.ss.heartlinkapi.notification.dto.NotificationDTO;
import com.ss.heartlinkapi.notification.dto.NotificationFollowDTO;
import com.ss.heartlinkapi.notification.dto.NotificationLikeDTO;
import com.ss.heartlinkapi.notification.entity.NotificationEntity;
import com.ss.heartlinkapi.notification.entity.Type;
import com.ss.heartlinkapi.notification.repository.EmitterRepository;
import com.ss.heartlinkapi.notification.repository.NotificationRepository;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    //    기본 타임아웃 설정
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    //    구독시 연결 해제 방지를 위해 더미데이터를 보내 연결을 유지시킨다.
    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = createdEmitter(userId);
        sendToClient(userId, "EventStream Created, [userId=" + userId + "]");
        return emitter;
    }

    //    이벤트발생시 data가 notify 메서드를 통해 sendToClient으로 넘어가고 client측으로 출력되게 된다.
    public void notifyLikePost(String userName, Long postId, Long userId) {
        NotificationLikeDTO notificationLikeDTO = new NotificationLikeDTO("http://localhost:3000/feed/details/" + postId, userName + "님이 회원님의 글을 좋아합니다.");
        Optional<PostEntity> post = postRepository.findById(postId);
        Long postWriterId = post
                .map(p -> p.getUserId().getUserId())
                .orElseThrow(() -> new NoSuchElementException("there is no post"));
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        String otherUserImg = profileRepository.findByUserEntity(user).getProfile_img();
        UserEntity senderUser = userRepository.findByLoginId(userName);
        saveNotification("MESSAGE_LIKE", notificationLikeDTO.getMessage(), postWriterId, senderUser.getUserId(), otherUserImg, "http://localhost:3000/feed/details/" + postId);

        sendToClient(postWriterId, notificationLikeDTO);
    }

    //    이벤트발생시 data가 notify 메서드를 통해 sendToClient으로 넘어가고 client측으로 출력되게 된다.
    public void notifyLikeComment(String userName, Long postId, Long userId) {
//        postId로 userId를 가져오는 메서드
        Optional<PostEntity> post = postRepository.findById(postId);
        Long postWriterId = post
                .map(p -> p.getUserId().getUserId())
                .orElseThrow(() -> new NoSuchElementException("there is no post"));
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        UserEntity senderUser = userRepository.findByLoginId(userName);
        String otherUserImg = profileRepository.findByUserEntity(user).getProfile_img();
        NotificationLikeDTO notificationLikeDTO = new NotificationLikeDTO("http://localhost:3000/feed/details/" + postId, userName + "님이 회원님의 댓글을 좋아합니다.");
        saveNotification("COMMENT_LIKE", notificationLikeDTO.getMessage(), postWriterId, senderUser.getUserId(), otherUserImg, "http://localhost:3000/feed/details/" + postId);
//        포스트 아이디 기준으로 작성자 찾아서 userId에 넣을 것.
        sendToClient(postWriterId, notificationLikeDTO);
    }

    public void notifyComment(String userName, Long postId, Long userId) {
        NotificationCommentDTO notificationCommentDTO = new NotificationCommentDTO("http://localhost:3000/feed/details/" + postId, userName + "님이 댓글을 남겼습니다.");
        Optional<PostEntity> post = postRepository.findById(postId);
        Long postWriterId = post
                .map(p -> p.getUserId().getUserId())
                .orElseThrow(() -> new NoSuchElementException("there is no post"));
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        UserEntity senderUser = userRepository.findByLoginId(userName);
        String otherUserImg = profileRepository.findByUserEntity(user).getProfile_img();
        saveNotification("COMMENT", notificationCommentDTO.getMessage(), postWriterId, senderUser.getUserId(), otherUserImg, "http://localhost:3000/feed/details/" + postId);
//        포스트 아이디 기준으로 작성자 찾아서 userId에 넣을 것.
        sendToClient(postWriterId, notificationCommentDTO);
    }

    //      팔로우 요청 시 알람
    public void notifyFollow(String userName, Long userId, Long id) {
        NotificationFollowDTO notificationFollowDTO = new NotificationFollowDTO("http://localhost:3000/user/profile/" + id, userName + "님이 회원님을 팔로우하였습니다.");
        UserEntity user = new UserEntity();
        user.setUserId(id);
        String otherUserImg = profileRepository.findByUserEntity(user).getProfile_img();
        saveNotification("FOLLOW", notificationFollowDTO.getMessage(), userId, id, otherUserImg, "http://localhost:3000/user/profile/" + id);
        sendToClient(userId, notificationFollowDTO);
    }

    //      비공개 유저 팔로우 요청
    public void notifyFollowPrivate(String userName, Long userId, Long id) {
        NotificationFollowDTO notificationFollowDTO = new NotificationFollowDTO("http://localhost:3000/user/profile/" + id, userName + "님이 회원님을 팔로우하였습니다.");
        UserEntity user = new UserEntity();
        user.setUserId(id);
        String otherUserImg = profileRepository.findByUserEntity(user).getProfile_img();
        saveNotification("PRIVATE_FOLLOW_REQUEST", notificationFollowDTO.getMessage(), userId, id, otherUserImg, "http://localhost:3000/user/profile/" + id);
        sendToClient(userId, notificationFollowDTO);
    }

    //  유저 태그시 알람
    public void notifyIdTag(String userName, Long postId, Long id) {
        NotificationFollowDTO notificationFollowDTO = new NotificationFollowDTO("http://localhost:3000/feed/details/" + postId, userName + "님이 게시글에 회원님을 태그하였습니다.");
        Optional<PostEntity> post = postRepository.findById(postId);
        Long postWriterId = post
                .map(p -> p.getUserId().getUserId())
                .orElseThrow(() -> new NoSuchElementException("there is no post"));
        UserEntity user = new UserEntity();
        user.setUserId(postWriterId);
        UserEntity senderUser = userRepository.findByLoginId(userName);
        String otherUserImg = profileRepository.findByUserEntity(user).getProfile_img();
        saveNotification("COMMENT", notificationFollowDTO.getMessage(), id, senderUser.getUserId(), otherUserImg, "http://localhost:3000/feed/details/" + postId);
        sendToClient(id, notificationFollowDTO);
    }

    //      실질적으로 client에게 메세지를 전달해주는 메서드
    private void sendToClient(Long userId, Object data) {

        SseEmitter emitter = emitterRepository.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().id(String.valueOf(userId)).name("sse").data(data));
            } catch (IOException e) {
                emitterRepository.deleteById(userId);
                emitter.completeWithError(e);
            }
        }

    }

    //      더미데이터를 생성하고 저장소에 넣어서 관리
    private SseEmitter createdEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, emitter);

//        emitter의 시간이 종료 되거나 이벤트가 완료되었을 시에 emitterRepository에서 userId를 기준으로 저장된 emitter들을 삭제한다.
        emitter.onCompletion(() -> emitterRepository.deleteById(userId));
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        return emitter;
    }

    //      알람 저장
    private void saveNotification(String type, String message, Long receiverId, Long senderId, String otherUserImg, String link) {
        UserEntity user = new UserEntity();
        user.setUserId(receiverId);
        NotificationEntity notificationEntity = new NotificationEntity().builder()
                .recieverUserId(user.getUserId())
                .senderUserId(senderId)
                .message(message)
                .userImg(otherUserImg)
                .isRead(true)
                .type(Type.valueOf(type))
                .link(link)
                .build();

        notificationRepository.save(notificationEntity);
    }

    //      내 알람 리스트 불러오기
    public List<NotificationDTO> getNotifications(CustomUserDetails user) {

        List<NotificationEntity> notifications = notificationRepository.findByRecieverUserId(user.getUserId());

        List<NotificationDTO> notificationDTOS = new ArrayList<>();

        for (NotificationEntity notificationEntity : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setId(notificationEntity.getId());
            notificationDTO.setSenderId(notificationEntity.getSenderUserId());
            notificationDTO.setOtherUserImg(notificationEntity.getUserImg());
            notificationDTO.setType(String.valueOf(notificationEntity.getType()));
            notificationDTO.setCreatedAt(notificationEntity.getCreatedDate());
            notificationDTO.setMessage(notificationEntity.getMessage());
            notificationDTO.setLink(notificationEntity.getLink());
            notificationDTOS.add(notificationDTO);
        }

        return notificationDTOS;
    }

    public void deleteById(Long notificationId) {
            notificationRepository.deleteById(notificationId);
    }

}

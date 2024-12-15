package com.ss.heartlinkapi.message.service;

import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.message.dto.BlockUserCheckDTO;
import com.ss.heartlinkapi.message.dto.ChatMsgListDTO;
import com.ss.heartlinkapi.message.dto.FriendDTO;
import com.ss.heartlinkapi.message.entity.MessageEntity;
import com.ss.heartlinkapi.message.entity.MessageRoomEntity;
import com.ss.heartlinkapi.message.entity.MsgRoomType;
import com.ss.heartlinkapi.message.repository.MessageRepository;
import com.ss.heartlinkapi.message.repository.MessageRoomRepository;
import com.ss.heartlinkapi.search.service.SearchService;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.ss.heartlinkapi.user.entity.Role.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageRoomService {

    private final MessageRoomRepository messageRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MessageService messageService;
    private final SearchService searchService;
    private final CoupleService coupleService;


    public List<Object> getChatUsers(Long userId) {

        List<Object> chatUsers = new ArrayList<>();

        List<MessageRoomEntity> messageRoomEntities = messageRoomRepository.findByUser1IdOrUser2Id(userId, userId);

        log.info("entities : {}", messageRoomEntities);
        for (MessageRoomEntity entity : messageRoomEntities) {

            HashMap<String, Object> chat = new HashMap<>();
            List<ChatMsgListDTO> messages = new ArrayList();

            Long otherUserId = 0L;

//            대화 상대 userId를 확인하는 조건문
            if (Objects.equals(entity.getUser1Id(), userId)) {
                otherUserId = entity.getUser2Id();
            } else {
                otherUserId = entity.getUser1Id();
            }

//            대화 상대 entity 가져오기
            UserEntity chatUserEntity = userRepository.findById(otherUserId).get();

//            대화 상대 유저이름
            String otherLoginId = chatUserEntity.getLoginId();
            chat.put("otherLoginId", otherLoginId);

//            비공개 채팅방 개설 주체 확인
            if (entity.getUser1Id().equals(userId)) {
                chat.put("openUser", true);
            } else {
                chat.put("openUser", false);
            }

//            대화방 타입 설정
            String msgRoomType = String.valueOf(entity.getMsgRoomType());
            chat.put("msgRoomType", msgRoomType);

//            대화 상대 유저 이미지
            ProfileEntity profileEntity = profileRepository.findByUserEntity(chatUserEntity);
            String otherUserImg = profileEntity.getProfile_img();
            chat.put("otherUserImg", otherUserImg);

//            대화 상대 유저 userId
            chat.put("otherUserId", otherUserId);

//            msg_room_id 가져오기
            Long messageRoomId = entity.getId();
            chat.put("msgRoomId", messageRoomId);

//            마지막 메시지 구하기
            MessageEntity lastMessage = messageRepository.findByMsgRoomIdOrderByCreatedAt(messageRoomId);
            if (lastMessage == null) {
                chat.put("lastMessage", null);
            } else {
                if (lastMessage.getContent() != null)
                    chat.put("lastMessage", lastMessage.getContent());
                else if (lastMessage.getImgUrl() != null)
                    chat.put("lastMessage", "사진을 보냈습니다.");
            }


//            로그인 상태 확인
            chat.put("login", true);


            chatUsers.add(chat);
        }

        return chatUsers;
    }

    public MessageRoomEntity applyMessage(Long userId, Long otherUserId) {

        MessageRoomEntity messageRoomEntity = new MessageRoomEntity();
        messageRoomEntity.setUser1Id(userId);
        messageRoomEntity.setUser2Id(otherUserId);
        messageRoomEntity.setCreatedAt(LocalDateTime.now());
//        수락전은 N으로해서 팔로우 된 사람들과의 대화랑 구분
        messageRoomEntity.setMsgRoomType(MsgRoomType.valueOf("PRIVATE"));
        return messageRoomRepository.save(messageRoomEntity);
    }

    //    msgRoomId를 기준으로 방을 삭제
    public void applyRejection(Long msgRoomId) {
        messageRoomRepository.deleteById(msgRoomId);
    }

    //    msgRoomId를 기준으로 type을 y로 업데이트
    public void applyAccept(Long msgRoomId) {
        MessageRoomEntity messageRoomEntity = messageRoomRepository.findById(msgRoomId).orElseThrow(() -> new RuntimeException("MsgRoomId not found"));
        messageRoomEntity.setMsgRoomType(MsgRoomType.valueOf("PUBLIC"));
        messageRoomRepository.save(messageRoomEntity);
    }

    //    msgRoom 존재 여부
    public boolean existChatRoom(Long userId, Long otherUserId) {
        if (messageRoomRepository.existsByUser1IdAndUser2Id(userId, otherUserId))
            return true;
        else return messageRoomRepository.existsByUser1IdAndUser2Id(otherUserId, userId);
    }

    //    상대방 존재 여부
    public boolean existOtherUser(Long otherUserId) {
        return userRepository.existsById(otherUserId);
    }

    //    채팅방 생성
    public MessageRoomEntity createChatRoom(Long userId, Long otherUserId) {
        MessageRoomEntity messageRoomEntity = new MessageRoomEntity();
        messageRoomEntity.setUser1Id(userId);
        messageRoomEntity.setUser2Id(otherUserId);
        messageRoomEntity.setMsgRoomType(MsgRoomType.valueOf("PUBLIC"));
        return messageRoomRepository.save(messageRoomEntity);
    }

    //  searchName으로 유저를 검색하는 메서드
    public List<FriendDTO> searchUsers(Long userId, String searchName) {

        List<FriendDTO> friends = new ArrayList<>();
        List<UserEntity> users = userRepository.findBySearchName(searchName);

        for (UserEntity user : users) {

            if (Objects.equals(user.getUserId(), userId))
                continue;
            if (user.getRole().equals(ROLE_ADMIN) || user.getRole().equals(ROLE_SINGLE) || user.getRole().equals(ROLE_USER))
                continue;
            if (existChatRoom(userId, user.getUserId()))
                continue;

            FriendDTO friend = new FriendDTO();
            String userImg = profileRepository.findByUserEntity(user).getProfile_img();
            friend.setFriendImg(userImg);
            friend.setFriendName(user.getLoginId());
            friend.setFriendId(user.getUserId());

            friends.add(friend);
        }

        return friends;
    }

    public List<FriendDTO> initSearch(UserEntity user) {

        List<FriendDTO> friends = new ArrayList<>();
        List<Map<String, Object>> list = searchService.mentionIdList(user);

        for (Map<String, Object> map : list) {
            FriendDTO friend = new FriendDTO();
            friend.setFriendName(String.valueOf(map.get("loginId")));
            friend.setFriendImg(String.valueOf(map.get("profileUrl")));
            friend.setFriendId((Long) map.get("userId"));

            BlockUserCheckDTO blockUserCheckDTO = new BlockUserCheckDTO();
            blockUserCheckDTO.setUserId(user.getUserId());
            blockUserCheckDTO.setBlockUserId((Long) map.get("userId"));

            if (messageService.blockMessage(blockUserCheckDTO)) {
                continue;
            }

            friends.add(friend);
        }

        return friends;
    }

    public boolean OtherUserIsPrivate(Long otherUserId) {
        return coupleService.findByUser1_IdOrUser2_Id(otherUserId).getIsPrivate();
    }

    public void deleteMsgRoom(Long msgRoomId) {
        messageRoomRepository.deleteById(msgRoomId);
    }
}

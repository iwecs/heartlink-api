package com.ss.heartlinkapi.message.controller;

import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.message.dto.*;
import com.ss.heartlinkapi.message.entity.MessageRoomEntity;
import com.ss.heartlinkapi.message.service.MessageRoomService;
import com.ss.heartlinkapi.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dm")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageRoomService messageRoomService;
    private final MessageService messageService;

    //          채팅방을 개설하는 핸들러 메서드
    @PostMapping("/new/{otherUserId}")
    public ResponseEntity<String> createChatRoom(@PathVariable Long otherUserId, @AuthenticationPrincipal CustomUserDetails user){

        //        상대방의 존재여부 확인
        if (!messageRoomService.existOtherUser(otherUserId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않거나 탈퇴한 유저입니다.");

        //        이미 만들어진 채팅방인지 확인
        if (messageRoomService.existChatRoom(user.getUserId(), otherUserId))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 대화방이 존재하는 유저입니다.");

        if (messageRoomService.OtherUserIsPrivate(otherUserId)) {
            MessageRoomEntity messageRoomEntity = messageRoomService.applyMessage(user.getUserId(), otherUserId);
            return ResponseEntity.ok(String.valueOf(messageRoomEntity.getId()));
        }

        //        채팅방 생성
        MessageRoomEntity messageRoomEntity = messageRoomService.createChatRoom(user.getUserId(), otherUserId);
        return ResponseEntity.ok(String.valueOf(messageRoomEntity.getId()));

    }

    //        채팅상대방 목록과 나의 정보를 가져오는 핸들러 메서드
    @GetMapping
    public ResponseEntity<HashMap<String, Object>> getChatUsers(@AuthenticationPrincipal CustomUserDetails user) {

        HashMap<String, Object> response = new HashMap<>();

//        내 정보를 저장
        response.put("myLoginId", user.getUsername());
        response.put("myUserId", user.getUserId());

//        채팅상대방 목록과 상대방의 정보를 가져오기
        List<Object> chatUsers = messageRoomService.getChatUsers(user.getUserId());
        response.put("chatUsers", chatUsers);

        return ResponseEntity.ok(response);
    }


//    상대방과의 채팅 내역을 가져오는 핸들러 메서드
    @GetMapping("/{msgRoomId}")
    public ResponseEntity<List<ChatMsgListDTO>> getMessage(@PathVariable Long msgRoomId) {

        List<ChatMsgListDTO> messages = messageService.getMessages(msgRoomId);

        return ResponseEntity.ok(messages);
    }


    //    텍스트 메세지를 저장
    @PostMapping("/messages/text")
    public ResponseEntity<String> createTextMessage(@RequestBody TextMessageDTO textMessageDTO, @RequestParam Long otherUserId) {

        //      상대방이 차단되거나 차단한 유저인지 확인
        if(messageService.blockCheck(textMessageDTO.getSenderId(), otherUserId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("차단 유저에게는 메세지를 보낼 수 없습니다.");

        SaveMsgDTO saveMsgDTO = new SaveMsgDTO().builder()
                .msgRoomId(textMessageDTO.getMsgRoomId())
                .senderId(textMessageDTO.getSenderId())
                .content(textMessageDTO.getContent())
                .messageTime(LocalDateTime.now())
                .isRead(false)
                .build();

        messageService.saveChatMessage(saveMsgDTO);

        return ResponseEntity.ok("save message");
    }

    //    이미지 파일 또는 gif를 메시지 저장
    @PostMapping("/messages/img")
    public ResponseEntity<String> saveImageMessage(@RequestParam("file") MultipartFile multipartFile,
                                                   @RequestParam("msgRoomId") Long msgRoomId,
                                                   @RequestParam("senderId") Long senderId
                                                , @RequestParam("otherUserId") Long otherUserId) {

        //      상대방이 차단되거나 차단한 유저인지 확인
        if (messageService.blockCheck(senderId, otherUserId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("차단 유저에게는 메세지를 보낼 수 없습니다.");

//        이미지가 비었는지 확인
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body("no file");

        } else {
            try {

//                파일 확장자가 이미지 계열인지 확인
                String fileExtension = multipartFile.getOriginalFilename() != null
                        ? multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."))
                        : "";

                if (!fileExtension.matches("(?i)\\.(jpg|jpeg|png|gif)$")) {
                    return ResponseEntity.badRequest().body("not supported");
                }

//            현재 heartlink-api폴더 경로를 가져옴.
                String currentPath = Paths.get("").toAbsolutePath().toString();

//            img파일 위치 경로에 파일 이름을 더해 filePath에 저장
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                String filePath = currentPath + "/images/" + newFileName;


                multipartFile.transferTo(new File(filePath));

//            이미지를 가져올 경로를 저장하는 과정
                String importPath = "http://localhost:9090/images/" + newFileName;

                SaveMsgDTO saveMsgDTO = new SaveMsgDTO().builder()
                        .msgRoomId(msgRoomId)
                        .senderId(senderId)
                        .emoji(null)
                        .imageUrl(importPath)
                        .messageTime(LocalDateTime.now())
                        .isRead(false)
                        .build();

                messageService.saveChatMessage(saveMsgDTO);

                return ResponseEntity.ok(importPath);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return ResponseEntity.ok("save good");
    }


    //    비공개 사용자 메세지 요청 거절
    @DeleteMapping("/message/rejection/{msgRoomId}")
    public ResponseEntity<String> applyRejection(@PathVariable("msgRoomId") Long msgRoomId) {

        messageRoomService.applyRejection(msgRoomId);

        return ResponseEntity.ok("rejection success");
    }

    //    비공개 사용자 메시지 요청 수락
    @PutMapping("/message/accept/{msgRoomId}")
    public ResponseEntity<String> applyAccept(@PathVariable("msgRoomId") Long msgRoomId) {

        messageRoomService.applyAccept(msgRoomId);

        return ResponseEntity.ok("accept success");
    }

    //    사용자가 타 사용자를 차단한 경우 사용자는 타 사용자에게 DM을 보낼 수 없다
    @GetMapping("/message/block")
    public ResponseEntity<String> blockMessage(@RequestBody BlockUserCheckDTO blockUserCheckDTO) {

        boolean result = false;
        result = messageService.blockMessage(blockUserCheckDTO);

        if (result)
            return ResponseEntity.ok("block user");
        else
            return ResponseEntity.ok("nonblock user");
    }

    //  검색기반으로 팔로잉목록 가져오는 핸들러 메서드
    @GetMapping("/friends")
    public ResponseEntity<List<FriendDTO>> searchUsers(@AuthenticationPrincipal CustomUserDetails user, @RequestParam(required = false) String searchName) {

        List<FriendDTO> friends = new ArrayList<>();

        if (searchName == null) {
            friends = messageRoomService.initSearch(user.getUserEntity());
        }
        else{
            friends = messageRoomService.searchUsers(user.getUserId(), searchName);
        }

        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/{msgRoomId}")
    public ResponseEntity<String> deleteMsgRoom(@PathVariable Long msgRoomId) {

        messageRoomService.deleteMsgRoom(msgRoomId);
        messageService.deleteMessages(msgRoomId);

        return ResponseEntity.ok("delete success");
    }

}

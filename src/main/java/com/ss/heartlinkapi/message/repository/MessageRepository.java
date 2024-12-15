package com.ss.heartlinkapi.message.repository;

import com.ss.heartlinkapi.message.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query(value = "SELECT * FROM message m WHERE m.msg_room_id=:id ORDER BY m.created_at desc LIMIT 1", nativeQuery = true)
    MessageEntity findByMsgRoomIdOrderByCreatedAt(@Param("id") Long id);

    List<MessageEntity> findByMsgRoomId(Long msgRoomId);

    void deleteByMsgRoomId(Long msgRoomId);
}

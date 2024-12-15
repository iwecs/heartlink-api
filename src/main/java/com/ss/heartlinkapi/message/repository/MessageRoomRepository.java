package com.ss.heartlinkapi.message.repository;

import com.ss.heartlinkapi.message.entity.MessageRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRoomRepository extends JpaRepository<MessageRoomEntity, Long> {

    List<MessageRoomEntity> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
    boolean existsByUser1IdAndUser2Id(Long userId, Long otherUserId);

}

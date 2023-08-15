package com.ibtehaj.Ecom.Repository;

import com.ibtehaj.Ecom.Models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    ChatRoom findByUser_Id(Long userId);
}

package com.ibtehaj.Ecom.Controller;


import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Models.ChatRoom;
import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Models.UserRole;
import com.ibtehaj.Ecom.Repository.ChatRoomRepository;
import com.ibtehaj.Ecom.Repository.UserRepository;
import com.ibtehaj.Ecom.Response.ErrorResponse;
import com.ibtehaj.Ecom.Utils.AccessTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class ChatRoomController {

    private final AccessTokenUtils accessTokenUtils;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatRoomController(AccessTokenUtils accessTokenUtils, UserRepository userRepository, ChatRoomRepository chatRoomRepository) {
        this.accessTokenUtils = accessTokenUtils;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @GetMapping("/get-chat-room")
    public ResponseEntity<?> getChatRoomForUser() throws CustomAccessDeniedException {
        String username = accessTokenUtils.getUsernameFromAccessToken();
        User user = userRepository.findByUsername(username);
        if (user == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

        ChatRoom chatRoom = chatRoomRepository.findByUser_Id(user.getId());

        if (chatRoom == null) {
            List<User> admins = userRepository.findByRolesContaining(UserRole.ROLE_ADMIN);
            if (!admins.isEmpty()) {
                User randomAdmin = admins.get(new Random().nextInt(admins.size()));

                chatRoom = new ChatRoom(user, randomAdmin);
                chatRoom = chatRoomRepository.save(chatRoom);
            }
        }

        return new ResponseEntity<>(chatRoom,HttpStatus.OK);
    }
}


package com.example.user_ms.controller;

import com.example.user_ms.model.dto.UserTestResponseDto;
import com.example.user_ms.model.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_ms.model.dto.CustomUserPrincipal;
import com.example.user_ms.model.dto.UserMeResponseDto;
import com.example.user_ms.service.FriendshipService;

import java.util.List;

import static com.example.user_ms.mapper.UserMapper.USER_MAPPER;
@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService){
          this.friendshipService=friendshipService;
    }

    @GetMapping("/pending")
    public ResponseEntity<Long> getPendingFriendRequests(@AuthenticationPrincipal CustomUserPrincipal me) {
        System.out.println("holaaa");
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);
        long pendingCount = friendshipService.countPendingFriendRequests(dto.getId());
        return ResponseEntity.ok(pendingCount);
    }
    @GetMapping("/pending/users")
    public ResponseEntity<List<UserMeResponseDto>> getPendingFriendRequestUsers(@AuthenticationPrincipal CustomUserPrincipal me) {
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);
        List<User> users = friendshipService.findUsersThatSentPendingRequests(dto.getId());
        List<UserMeResponseDto> userDtos = USER_MAPPER.toUserMeResponseDtoList(users);
        return ResponseEntity.ok(userDtos);
    }
    @PutMapping("/accept")
    public ResponseEntity<Void> acceptFriendRequest( @AuthenticationPrincipal CustomUserPrincipal me, @RequestParam Long friendId) {
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);
        friendshipService.acceptFriendRequest(dto.getId(), friendId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/Myfriends")
    public ResponseEntity<List<UserMeResponseDto>> getFriends(@AuthenticationPrincipal CustomUserPrincipal me) {
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);
        List<UserMeResponseDto> listUserMeResponseDto=  USER_MAPPER.toUserMeResponseDtoList(friendshipService.getFriends(dto.getId()));
        return ResponseEntity.ok(listUserMeResponseDto);
    }
    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(@AuthenticationPrincipal CustomUserPrincipal me, @RequestParam Long friendId) {
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);
        friendshipService.sendFriendRequest(dto.getId(), friendId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/candidates")
    public ResponseEntity<List<UserMeResponseDto>> getCandidates(@AuthenticationPrincipal CustomUserPrincipal me) {
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);
         List<UserMeResponseDto> listUserMeResponseDto = USER_MAPPER.toUserMeResponseDtoList(friendshipService.getRequestCandidates(dto.getId()));
        return ResponseEntity.ok(listUserMeResponseDto);
    }
}
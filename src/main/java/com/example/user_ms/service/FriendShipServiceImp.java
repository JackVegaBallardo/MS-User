package com.example.user_ms.service;

import com.example.user_ms.model.entity.Friendship;
import com.example.user_ms.model.entity.FriendshipStatus;
import com.example.user_ms.model.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.user_ms.repository.FriendshipRepository;
import com.example.user_ms.repository.FriendshipStatusRepository;
import com.example.user_ms.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class FriendShipServiceImp implements FriendshipService{
    @Autowired
    private  UserRepository userRepository;


    private FriendshipRepository friendshipRepository;
    @Autowired  
    private  FriendshipStatusRepository statusRepository;
    
    public FriendShipServiceImp(FriendshipRepository friendshipRepository){
        this.friendshipRepository=friendshipRepository;
    }

    @Override
    public long countPendingFriendRequests(long UserId) {
         return  friendshipRepository.countPendingFriendRequests(UserId);
    }

    @Override
    public List<User> findUsersThatSentPendingRequests(Long userId) {
        return friendshipRepository.findUsersThatSentPendingRequests(userId);
    }

@Override
@Transactional
public void acceptFriendRequest(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository
            .findPendingRequest(userId, friendId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        FriendshipStatus accepted = statusRepository.findByName("ACCEPTED")
            .orElseThrow(() -> new RuntimeException("Status ACCEPTED no encontrado"));

        friendship.setStatus(accepted);
        friendship.setSince(LocalDateTime.now());
        friendshipRepository.saveAndFlush(friendship);
        log.info("Aceptada original: {} -> {}", friendship.getUser().getId(), friendship.getFriend().getId());

        
        Optional<Friendship> mirrorOpt =
            friendshipRepository.findByUser_IdAndFriend_Id(userId, friendId); 

        if (mirrorOpt.isPresent()) {
            Friendship mirror = mirrorOpt.get();
            if (!"ACCEPTED".equalsIgnoreCase(mirror.getStatus().getName())) {
                mirror.setStatus(accepted);
                if (mirror.getSince() == null) mirror.setSince(LocalDateTime.now());
                friendshipRepository.saveAndFlush(mirror);
                log.info("Actualizado espejo a ACCEPTED: {} -> {}", userId, friendId);
            } else {
                log.info("Espejo ya estaba en ACCEPTED: {} -> {}", userId, friendId);
            }
        } else {
            Friendship mirror = new Friendship();
            mirror.setUser(friendship.getFriend());
            mirror.setFriend(friendship.getUser()); 
            mirror.setStatus(accepted);
            mirror.setSince(LocalDateTime.now());
            friendshipRepository.saveAndFlush(mirror);
            log.info("Creado espejo: {} -> {}", userId, friendId);
        }
    }

@Override
public List<User> getFriends(Long userId) {
     return friendshipRepository.findAcceptedFriends(userId);
  }

    @Transactional
    public void sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo.");
        }

        var pending = statusRepository.findByName("PENDING")
                .orElseThrow(() -> new IllegalStateException("Status PENDING no encontrado"));

        var sameDir = friendshipRepository.findByUser_IdAndFriend_Id(senderId, receiverId);
        if (sameDir.isPresent()) {
            var f = sameDir.get();
            f.setStatus(pending);
            f.setSince(LocalDateTime.now());
            friendshipRepository.save(f);
            return;
        }

        var f = new Friendship();
        f.setUser(userRepository.getReferenceById(senderId));
        f.setFriend(userRepository.getReferenceById(receiverId));
        f.setStatus(pending);
        f.setSince(LocalDateTime.now());
        friendshipRepository.save(f);
    }

    @Override
    public List<User> getRequestCandidates(Long userId) {
        return friendshipRepository.findUsersYouCanRequest(userId);
    }
}

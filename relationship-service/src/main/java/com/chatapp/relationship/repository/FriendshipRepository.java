package com.chatapp.relationship.repository;

import com.chatapp.relationship.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUserIdAndStatus(Long userId, Friendship.FriendshipStatus status);
    
    List<Friendship> findByFriendIdAndStatus(Long friendId, Friendship.FriendshipStatus status);
    
    @Query("SELECT f FROM Friendship f WHERE (f.userId = ?1 AND f.friendId = ?2) OR (f.userId = ?2 AND f.friendId = ?1)")
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    
    boolean existsByUserIdAndFriendIdAndStatus(Long userId, Long friendId, Friendship.FriendshipStatus status);
    
    @Query("SELECT f FROM Friendship f WHERE f.userId = ?1 OR f.friendId = ?1")
    List<Friendship> findAllByUserId(Long userId);
    
    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.userId = ?1 OR f.friendId = ?1) AND f.status = 'ACCEPTED'")
    long countFriendsByUserId(Long userId);
} 
package com.Axiom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.PostLike;

@Repository
public interface PostLikeRepository extends MongoRepository<PostLike, String> {

    Optional<PostLike> findByPostIdAndUserId(String postId, String userId);

    List<PostLike> findAllByPostId(String postId);

    int countByPostId(String postId);

    void deleteAllByPostId(String postId);
}

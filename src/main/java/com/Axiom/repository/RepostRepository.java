package com.Axiom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.Repost;

@Repository
public interface RepostRepository extends MongoRepository<Repost, String> {

    List<Repost> findAllByOriginalPostId(String originalPostId);
    Optional<Repost> findByOriginalPostIdAndUserId(String originalPostId, String userId);

    int countByOriginalPostId(String originalPostId);

    void deleteAllByOriginalPostId(String originalPostId);
}

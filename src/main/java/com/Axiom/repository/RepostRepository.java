package com.Axiom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.Post;
import com.Axiom.entity.Repost;
import com.Axiom.entity.User;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {

    List<Repost> findAllByOriginalPost(Post originalPost);
    Optional<Repost> findByOriginalPostAndUser(Post originalPost, User user);

    int countByOriginalPost(Post originalPost);
}

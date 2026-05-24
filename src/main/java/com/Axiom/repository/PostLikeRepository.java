package com.Axiom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.Post;
import com.Axiom.entity.PostLike;
import com.Axiom.entity.User;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostAndUser(Post post, User user);

    List<PostLike> findAllByPost(Post post);

    int countByPost(Post post);
}

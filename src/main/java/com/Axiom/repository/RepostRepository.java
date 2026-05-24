package com.Axiom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.Post;
import com.Axiom.entity.Repost;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {

    List<Repost> findAllByOriginalPost(Post originalPost);

    int countByOriginalPost(Post originalPost);
}

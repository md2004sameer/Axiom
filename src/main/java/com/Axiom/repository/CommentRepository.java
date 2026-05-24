package com.Axiom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.Comment;
import com.Axiom.entity.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPost(Post post);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post = :post")
    List<Comment> findAllByPostWithAuthor(@Param("post") Post post);
}
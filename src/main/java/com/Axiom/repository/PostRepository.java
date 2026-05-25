package com.Axiom.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Axiom.entity.Post;
import com.Axiom.entity.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author")
    List<Post> findAllWithAuthor();

    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p"
    )
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query(
            value = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.author IN :authors ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.author IN :authors"
    )
    Page<Post> findAllByAuthorInWithAuthor(@Param("authors") List<User> authors, Pageable pageable);
}

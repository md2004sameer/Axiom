package com.Axiom.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Axiom.entity.Comment;
import com.Axiom.entity.Post;
import com.Axiom.entity.PostLike;
import com.Axiom.entity.Repost;
import com.Axiom.entity.User;
import com.Axiom.post.responses.CommentResponse;
import com.Axiom.post.responses.PostResponse;
import com.Axiom.repository.CommentRepository;
import com.Axiom.repository.PostLikeRepository;
import com.Axiom.repository.PostRepository;
import com.Axiom.repository.RepostRepository;
import com.Axiom.repository.UserRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final RepostRepository repostRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository, RepostRepository repostRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.repostRepository = repostRepository;
    }

    @Transactional
    public Post createPost(String username, String text) {
        User user = findUser(username);
        Post post = new Post();
        post.setText(text);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setRepostCount(0);
        user.addPost(post);
        userRepository.save(user);
        return post;
    }

    @Transactional
    public Post likePost(String username, Long postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            return post;
        }

        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);

        post.setLikeCount(post.getLikeCount() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public Comment addComment(String username, Long postId, String text) {
        User user = findUser(username);
        Post post = findPost(postId);

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setText(text);
        Comment saved = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return saved;
    }

    @Transactional
    public Repost repost(String username, Long postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        Repost repost = new Repost();
        repost.setOriginalPost(post);
        repost.setUser(user);
        Repost saved = repostRepository.save(repost);

        post.setRepostCount(post.getRepostCount() + 1);
        postRepository.save(post);
        return saved;
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = findPost(postId);
        return mapToResponse(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> listAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse mapToResponse(Post post) {
        List<String> likedBy = postLikeRepository.findAllByPost(post).stream()
                .map(like -> like.getUser().getUsername())
                .distinct()
                .collect(Collectors.toList());

        List<CommentResponse> comments = commentRepository.findAllByPost(post).stream()
                .map(comment -> new CommentResponse(comment.getId(), comment.getAuthor().getUsername(), comment.getText(), comment.getCreatedAt()))
                .collect(Collectors.toList());

        return new PostResponse(
                post.getId(),
                post.getAuthor().getUsername(),
                post.getText(),
                post.getCreatedAt(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getRepostCount(),
                likedBy,
                comments
        );
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
    }
}

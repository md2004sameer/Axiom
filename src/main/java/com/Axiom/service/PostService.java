package com.Axiom.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       PostLikeRepository postLikeRepository, CommentRepository commentRepository,
                       RepostRepository repostRepository) {
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
        return postRepository.saveAndFlush(post);
    }

    @Transactional
    public void deletePost(String username, Long postId) {
        Post post = findPost(postId);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("You can only delete your own posts");
        }
        postLikeRepository.deleteAllByPost(post);
        commentRepository.deleteAllByPost(post);
        repostRepository.deleteAllByOriginalPost(post);
        postLikeRepository.flush();
        commentRepository.flush();
        repostRepository.flush();
        postRepository.delete(post);
    }

    @Transactional
    public Post likePost(String username, Long postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        if (postLikeRepository.findByPostAndUser(post, user).isPresent()) {
            throw new IllegalStateException("You have already liked this post");
        }

        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.saveAndFlush(like);

        return synchronizeCounters(post);
    }

    @Transactional
    public void unlikePost(String username, Long postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        PostLike like = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalStateException("You have not liked this post"));

        postLikeRepository.delete(like);
        postLikeRepository.flush();
        synchronizeCounters(post);
    }

    @Transactional
    public Comment addComment(String username, Long postId, String text) {
        User user = findUser(username);
        Post post = findPost(postId);

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setText(text);
        Comment saved = commentRepository.saveAndFlush(comment);

        synchronizeCounters(post);
        return saved;
    }

    @Transactional
    public void deleteComment(String username, Long postId, Long commentId) {
        Post post = findPost(postId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("You can only delete your own comments");
        }
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalStateException("Comment does not belong to this post");
        }

        commentRepository.delete(comment);
        commentRepository.flush();
        synchronizeCounters(post);
    }

    @Transactional
    public Repost repost(String username, Long postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        if (repostRepository.findByOriginalPostAndUser(post, user).isPresent()) {
            throw new IllegalStateException("You have already reposted this post");
        }

        Repost repost = new Repost();
        repost.setOriginalPost(post);
        repost.setUser(user);
        Repost saved = repostRepository.saveAndFlush(repost);

        synchronizeCounters(post);
        return saved;
    }

    @Transactional
    public void unrepost(String username, Long postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        Repost repost = repostRepository.findByOriginalPostAndUser(post, user)
                .orElseThrow(() -> new IllegalStateException("You have not reposted this post"));

        repostRepository.delete(repost);
        repostRepository.flush();
        synchronizeCounters(post);
    }

    @Transactional
    public PostResponse getPost(Long postId) {
        return mapToResponse(findPost(postId));
    }

    @Transactional
    public List<PostResponse> listAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllWithAuthor(pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PostResponse> listFeed(String username, int page, int size) {
        User user = findUser(username);
        List<User> followedUsers = List.copyOf(user.getFollowing());
        if (followedUsers.isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByAuthorInWithAuthor(followedUsers, pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse mapToResponse(Post post) {
        Post synchronizedPost = synchronizeCounters(post);

        List<String> likedBy = postLikeRepository.findAllByPostWithUser(post).stream()
                .map(like -> like.getUser().getUsername())
                .distinct()
                .collect(Collectors.toList());

        List<CommentResponse> comments = commentRepository.findAllByPostWithAuthor(post).stream()
                .map(c -> new CommentResponse(c.getId(), c.getAuthor().getUsername(), c.getText(), c.getCreatedAt()))
                .collect(Collectors.toList());

        return new PostResponse(
                synchronizedPost.getId(),
                synchronizedPost.getAuthor().getUsername(),
                synchronizedPost.getText(),
                synchronizedPost.getCreatedAt(),
                synchronizedPost.getLikeCount(),
                synchronizedPost.getCommentCount(),
                synchronizedPost.getRepostCount(),
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

    private Post synchronizeCounters(Post post) {
        int likeCount = postLikeRepository.countByPost(post);
        int commentCount = commentRepository.countByPost(post);
        int repostCount = repostRepository.countByOriginalPost(post);

        if (post.getLikeCount() != likeCount
                || post.getCommentCount() != commentCount
                || post.getRepostCount() != repostCount) {
            post.setLikeCount(likeCount);
            post.setCommentCount(commentCount);
            post.setRepostCount(repostCount);
            return postRepository.save(post);
        }

        return post;
    }
}

package com.Axiom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Post createPost(String username, String text) {
        User user = findUser(username);
        Post post = new Post();
        post.setText(text);
        post.setAuthorId(user.getId());
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setRepostCount(0);
        user.addPost(post);
        return postRepository.save(post);
    }

    public void deletePost(String username, String postId) {
        Post post = findPost(postId);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("You can only delete your own posts");
        }
        postLikeRepository.deleteAllByPostId(post.getId());
        commentRepository.deleteAllByPostId(post.getId());
        repostRepository.deleteAllByOriginalPostId(post.getId());
        postRepository.delete(post);
    }

    public Post likePost(String username, String postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        if (postLikeRepository.findByPostIdAndUserId(post.getId(), user.getId()).isPresent()) {
            throw new IllegalStateException("You have already liked this post");
        }

        PostLike like = new PostLike();
        like.setPostId(post.getId());
        like.setUserId(user.getId());
        postLikeRepository.save(like);

        return synchronizeCounters(post);
    }

    public void unlikePost(String username, String postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        PostLike like = postLikeRepository.findByPostIdAndUserId(post.getId(), user.getId())
                .orElseThrow(() -> new IllegalStateException("You have not liked this post"));

        postLikeRepository.delete(like);
        synchronizeCounters(post);
    }

    public Comment addComment(String username, String postId, String text) {
        User user = findUser(username);
        Post post = findPost(postId);

        Comment comment = new Comment();
        comment.setPostId(post.getId());
        comment.setPost(post);
        comment.setAuthorId(user.getId());
        comment.setAuthor(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        synchronizeCounters(post);
        return saved;
    }

    public void deleteComment(String username, String postId, String commentId) {
        Post post = findPost(postId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("You can only delete your own comments");
        }
        if (!comment.getPostId().equals(postId)) {
            throw new IllegalStateException("Comment does not belong to this post");
        }

        commentRepository.delete(comment);
        synchronizeCounters(post);
    }

    public Repost repost(String username, String postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        if (repostRepository.findByOriginalPostIdAndUserId(post.getId(), user.getId()).isPresent()) {
            throw new IllegalStateException("You have already reposted this post");
        }

        Repost repost = new Repost();
        repost.setOriginalPostId(post.getId());
        repost.setUserId(user.getId());
        repost.setCreatedAt(LocalDateTime.now());
        Repost saved = repostRepository.save(repost);

        synchronizeCounters(post);
        return saved;
    }

    public void unrepost(String username, String postId) {
        User user = findUser(username);
        Post post = findPost(postId);

        Repost repost = repostRepository.findByOriginalPostIdAndUserId(post.getId(), user.getId())
                .orElseThrow(() -> new IllegalStateException("You have not reposted this post"));

        repostRepository.delete(repost);
        synchronizeCounters(post);
    }

    public PostResponse getPost(String postId) {
        return mapToResponse(findPost(postId));
    }

    public List<PostResponse> listAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> listFeed(String username, int page, int size) {
        User user = findUser(username);
        List<String> followedUserIds = user.getFollowing().stream().toList();
        if (followedUserIds.isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByAuthorIdInOrderByCreatedAtDesc(followedUserIds, pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse mapToResponse(Post post) {
        Post synchronizedPost = synchronizeCounters(post);

        List<String> likedBy = postLikeRepository.findAllByPostId(post.getId()).stream()
                .map(like -> {
                    User user = userRepository.findById(like.getUserId()).orElse(null);
                    return user != null ? user.getUsername() : "unknown";
                })
                .distinct()
                .collect(Collectors.toList());

        List<CommentResponse> comments = commentRepository.findAllByPostId(post.getId()).stream()
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
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private Post findPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
    }

    private Post synchronizeCounters(Post post) {
        int likeCount = postLikeRepository.countByPostId(post.getId());
        int commentCount = commentRepository.countByPostId(post.getId());
        int repostCount = repostRepository.countByOriginalPostId(post.getId());

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

package com.Axiom.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Axiom.post.requests.CommentRequest;
import com.Axiom.post.requests.CreatePostRequest;
import com.Axiom.post.responses.PostResponse;
import com.Axiom.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        String username = currentUsername();
        var post = postService.createPost(username, request.getText());
        return ResponseEntity.ok(postService.getPost(post.getId()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(currentUsername(), postId);
        return ResponseEntity.noContent().build();
    }
    

    @PostMapping("/{postId}/like")
    public ResponseEntity<PostResponse> likePost(@PathVariable Long postId) {
        postService.likePost(currentUsername(), postId);
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<PostResponse> unlikePost(@PathVariable Long postId) {
        postService.unlikePost(currentUsername(), postId);
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<PostResponse> commentPost(@PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        postService.addComment(currentUsername(), postId, request.getText());
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<PostResponse> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        postService.deleteComment(currentUsername(), postId, commentId);
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @PostMapping("/{postId}/repost")
    public ResponseEntity<PostResponse> repost(@PathVariable Long postId) {
        postService.repost(currentUsername(), postId);
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.listAllPosts(page, size));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
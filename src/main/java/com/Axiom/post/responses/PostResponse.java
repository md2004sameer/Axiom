package com.Axiom.post.responses;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    private Long id;
    private String author;
    private String text;
    private LocalDateTime createdAt;
    private int likeCount;
    private int commentCount;
    private int repostCount;
    private List<String> likedBy;
    private List<CommentResponse> comments;

    public PostResponse() {
    }

    public PostResponse(Long id, String author, String text, LocalDateTime createdAt, int likeCount, int commentCount, int repostCount, List<String> likedBy, List<CommentResponse> comments) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.repostCount = repostCount;
        this.likedBy = likedBy;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }
}

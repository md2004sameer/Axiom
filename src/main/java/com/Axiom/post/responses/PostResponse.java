package com.Axiom.post.responses;

import java.time.LocalDateTime;
import java.util.List;
public record PostResponse(
    String id,
    String username,
    String text,
    LocalDateTime createdAt,
    int likeCount,
    int commentCount,
    int repostCount,
    List<String> likedBy,
    List<CommentResponse> comments
) {}
package com.Axiom.post.responses;

import java.time.LocalDateTime;
public record CommentResponse(String id, String username, String text, LocalDateTime createdAt) {}
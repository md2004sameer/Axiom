package com.Axiom.post.requests;

import jakarta.validation.constraints.NotBlank;

public class CreatePostRequest {

    @NotBlank
    private String text;

    public CreatePostRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

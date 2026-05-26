package com.Axiom.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String pepper;

    private String profilePictureUrl;

    private String bio;

    private List<String> postIds = new ArrayList<>();

    private Set<String> following = new HashSet<>();

    private Set<String> followers = new HashSet<>();

    public void addPost(Post post) {
        if (post.getId() != null) {
            postIds.add(post.getId());
        }
        post.setAuthor(this);
    }

    public void removePost(Post post) {
        if (post.getId() != null) {
            postIds.remove(post.getId());
        }
        post.setAuthor(null);
    }
}

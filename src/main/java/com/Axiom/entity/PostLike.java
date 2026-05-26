package com.Axiom.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "post_likes")
@CompoundIndexes({
    @CompoundIndex(name = "post_user_idx", def = "{'postId' : 1, 'userId': 1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class PostLike {

    @Id
    private String id;

    private String postId;

    private String userId;
}

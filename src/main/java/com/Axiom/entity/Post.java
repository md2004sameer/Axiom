package com.Axiom.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    private String id;

    private String text;

    private String authorId;

    private User author;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private int likeCount = 0;

    private int commentCount = 0;

    private int repostCount = 0;

}

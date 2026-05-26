package com.Axiom.service;

import org.springframework.stereotype.Service;

import com.Axiom.entity.User;
import com.Axiom.repository.UserRepository;
import com.Axiom.user.requests.UpdateProfileRequest;
import com.Axiom.user.responses.UserProfileResponse;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(String username) {
        return mapToProfile(findUser(username));
    }

    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUser(username);
        user.setBio(request.getBio());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        return mapToProfile(userRepository.save(user));
    }

    public UserProfileResponse follow(String currentUsername, String usernameToFollow) {
        if (currentUsername.equals(usernameToFollow)) {
            throw new IllegalStateException("You cannot follow yourself");
        }

        User currentUser = findUser(currentUsername);
        User userToFollow = findUser(usernameToFollow);
        
        if (!currentUser.getFollowing().add(userToFollow.getId())) {
            throw new IllegalStateException("You already follow this user");
        }
        userToFollow.getFollowers().add(currentUser.getId());

        userRepository.save(currentUser);
        userRepository.save(userToFollow);
        return mapToProfile(userToFollow);
    }

    public UserProfileResponse unfollow(String currentUsername, String usernameToUnfollow) {
        if (currentUsername.equals(usernameToUnfollow)) {
            throw new IllegalStateException("You cannot unfollow yourself");
        }

        User currentUser = findUser(currentUsername);
        User userToUnfollow = findUser(usernameToUnfollow);
        
        if (!currentUser.getFollowing().remove(userToUnfollow.getId())) {
            throw new IllegalStateException("You are not following this user");
        }
        userToUnfollow.getFollowers().remove(currentUser.getId());

        userRepository.save(currentUser);
        userRepository.save(userToUnfollow);
        return mapToProfile(userToUnfollow);
    }

    private UserProfileResponse mapToProfile(User user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getFollowers().size(),
                user.getFollowing().size()
        );
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}

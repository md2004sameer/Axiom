package com.Axiom.user.responses;

public class UserProfileResponse {

    private String username;
    private String bio;
    private String profilePictureUrl;
    private int followerCount;
    private int followingCount;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String username, String bio, String profilePictureUrl, int followerCount, int followingCount) {
        this.username = username;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}

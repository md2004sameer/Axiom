package com.Axiom.user.requests;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 500)
    private String bio;

    @Size(max = 2048)
    private String profilePictureUrl;

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
}

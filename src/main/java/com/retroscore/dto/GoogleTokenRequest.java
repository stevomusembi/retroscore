
package com.retroscore.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleTokenRequest {

    @NotBlank(message = "Google token is required")
    private String googleToken;

    // Optional: include device info for security
    private String deviceType; // "ios", "android", "web"
    private String appVersion;

    // Constructors
    public GoogleTokenRequest() {}

    public GoogleTokenRequest(String googleToken) {
        this.googleToken = googleToken;
    }

    // Getters and setters
    public String getGoogleToken() {
        return googleToken;
    }

    public void setGoogleToken(String googleToken) {
        this.googleToken = googleToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
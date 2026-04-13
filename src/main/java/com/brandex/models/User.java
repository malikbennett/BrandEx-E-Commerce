package com.brandex.models;

import com.brandex.models.enums.UserStatus;

// The ADT User, which extends the ADT Model.
public class User extends Model {
    private String username, email, firstName, lastName, role, phoneNumber, shippingAddress, profileImgURL;
    private UserStatus status;
    private String passwordHash, prevHash1, prevHash2;
    private String otpHash;
    private boolean otpUsed, forcePwChange;

    public User() {
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public String getRole() {
        return this.role;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getShippingAddress() {
        return this.shippingAddress;
    }

    public String getProfileImgURL() {
        return this.profileImgURL;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public String getPrevHash1() {
        return this.prevHash1;
    }

    public String getPrevHash2() {
        return this.prevHash2;
    }

    public String getOtpHash() {
        return this.otpHash;
    }

    public boolean isOtpUsed() {
        return this.otpUsed;
    }

    public boolean isForcePwChange() {
        return this.forcePwChange;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setProfileImgURL(String url) {
        this.profileImgURL = url;
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
    }

    public void setPrevHash1(String hash) {
        this.prevHash1 = hash;
    }

    public void setPrevHash2(String hash) {
        this.prevHash2 = hash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void setOtpUsed(boolean otpUsed) {
        this.otpUsed = otpUsed;
    }

    public void setForcePwChange(boolean force) {
        this.forcePwChange = force;
    }
}

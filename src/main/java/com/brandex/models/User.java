package com.brandex.models;

public class User {
    private String id;
    private String username, email, firstName, lastName, role;
    private String passwordHash, prevHash1, prevHash2;
    private String otpHash;
    private boolean otpUsed, forcePwChange;

    public User() {}

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPasswordHash() { return passwordHash; }
    public String getPrevHash1() { return prevHash1; }
    public String getPrevHash2() { return prevHash2; }
    public String getRole() { return role; }
    public String getOtpHash() { return otpHash; }
    public boolean isOtpUsed() { return otpUsed; }
    public boolean isForcePwChange() { return forcePwChange; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPasswordHash(String hash) { this.passwordHash = hash; }
    public void setPrevHash1(String hash) { this.prevHash1 = hash; }
    public void setPrevHash2(String hash) { this.prevHash2 = hash; }
    public void setRole(String role) { this.role = role; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public void setOtpUsed(boolean otpUsed) { this.otpUsed = otpUsed; }
    public void setForcePwChange(boolean force) { this.forcePwChange = force; }
}

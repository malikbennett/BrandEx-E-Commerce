package com.brandex.service;

import java.util.function.Consumer;

import com.brandex.datastructures.BST;
import com.brandex.models.User;
import com.brandex.models.enums.UserStatus;
import com.brandex.repository.UserRepository;
import com.brandex.utilities.EmailSender;
import com.brandex.utilities.OTPGenerator;
import com.brandex.utilities.PasswordHasher;

public class UserService {
    private static UserService instance;
    private final UserRepository userRepo = UserRepository.getInstance();
    private final BST<User> userTree = new BST<>((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));

    public static UserService getInstance() {
        if (instance == null)
            instance = new UserService();
        return instance;
    }

    public void loadUsers() {
        this.userRepo.listUsers().traverse(user -> {
            this.setInternalUser(user);
        });
    }

    public void forEachUser(Consumer<User> action) {
        this.userTree.traverse(action);
    }

    public User searchById(String id) {
        final User[] found = new User[1];
        this.userTree.traverse(user -> {
            if (user.getId().equals(id)) {
                found[0] = user;
            }
        });
        return found[0];
    }

    public User searchByUsername(String username) {
        final User[] found = new User[1];
        this.userTree.traverse(user -> {
            if (user.getUsername().equalsIgnoreCase(username)) {
                found[0] = user;
            }
        });
        return found[0];
    }

    public void createUser(User user) throws Exception {
        if (user == null)
            throw new Exception("User cannot be null.");
        String generatedId = this.userRepo.createUser(user);
        if (generatedId == null)
            throw new Exception("Failed to save user to database.");
        user.setId(generatedId);
        this.userTree.insert(user);
    }

    public void updateUser(User user, UserStatus status, String role) throws Exception {
        boolean ok = userRepo.updateUserStatusAndRole(user.getId(), status, role);
        if (!ok)
            throw new Exception("Failed to update user in database.");
        user.setStatus(status);
        user.setRole(role);
    }

    public void deleteUser(User user) throws Exception {
        if (user == null)
            throw new Exception("User cannot be null.");
        boolean ok = userRepo.deleteUser(user.getId());
        if (!ok)
            throw new Exception("Failed to delete user from database.");
        this.userTree.remove(user);
    }

    public void resetPassword(User user) throws Exception {
        String tempPassword = OTPGenerator.generate();
        String hash = PasswordHasher.hash(tempPassword);

        boolean ok = userRepo.resetPassword(user.getId(), hash);
        if (!ok)
            throw new Exception("Failed to reset password in database.");

        user.setPasswordHash(hash);
        user.setForcePwChange(true);

        String subject = "Your BrandEx Password Reset";
        String body = String.format("""
                Hello %s,

                An administrator has reset your password.
                Your temporary password is: %s

                Please log in and change your password immediately.

                Regards,
                The BrandEx Team
                """, user.getFirstName(), tempPassword);

        EmailSender.send(user.getEmail(), subject, body);
    }

    public void clearUsers() {
        this.userTree.clear();
    }

    public BST<User> getUsersTree() {
        return this.userTree;
    }

    private void setInternalUser(User user) {
        this.userTree.insert(user);
    }
}

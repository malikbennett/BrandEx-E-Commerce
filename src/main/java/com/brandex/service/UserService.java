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
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void loadUsers() {
        this.userRepo.listUsers().traverse(user -> {
            this.userTree.insert(user);
        });
    }

    public void forEachUser(Consumer<User> action) {
        this.userTree.traverse(action);
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

    public BST<User> getUsersTree() {
        return this.userTree;
    }

    public void updateUser(User user, UserStatus status, String role) {
        userRepo.updateUserStatusAndRole(user.getUsername(), status, role);
        user.setStatus(status);
        user.setRole(role);
    }

    public void resetPassword(User user) throws Exception {
        String tempPassword = OTPGenerator.generate();
        String hash = PasswordHasher.hash(tempPassword);

        userRepo.resetPassword(user.getUsername(), hash);
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

}

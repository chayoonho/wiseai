package com.example.wiseai_dev.user.domain.repository;

import com.example.wiseai_dev.user.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
}

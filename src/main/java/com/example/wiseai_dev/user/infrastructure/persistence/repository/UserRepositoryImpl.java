package com.example.wiseai_dev.user.infrastructure.persistence.repository;

import com.example.wiseai_dev.user.domain.model.User;
import com.example.wiseai_dev.user.domain.repository.UserRepository;
import com.example.wiseai_dev.user.infrastructure.persistence.entity.UserEntity;
import com.example.wiseai_dev.user.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomainModel(user);
        return jpaRepository.save(entity).toDomainModel();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(UserEntity::toDomainModel);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UserEntity::toDomainModel);
    }
}

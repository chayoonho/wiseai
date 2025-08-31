package com.example.wiseai_dev.user.infrastructure.persistence.entity;

import com.example.wiseai_dev.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Entity → Domain 변환
    public User toDomainModel() {
        return User.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .build();
    }

    // Domain → Entity 변환
    public static UserEntity fromDomainModel(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}

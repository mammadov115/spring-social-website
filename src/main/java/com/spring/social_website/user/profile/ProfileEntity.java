package com.spring.social_website.user.profile;

import com.spring.social_website.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity 
@Table(name = "profiles")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@EntityListeners(AuditingEntityListener.class)
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String location;

    @Builder.Default
    @Column(name = "is_email_public", nullable = false)
    private boolean isEmailPublic = false;

    @Builder.Default
    @Column(name = "is_birth_date_public", nullable = false)
    private boolean isBirthDatePublic = false;

    @Builder.Default
    @Column(name = "is_location_public", nullable = true)
    private boolean isLocationPublic = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}

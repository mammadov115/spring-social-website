package com.spring.social_website.user.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;
import com.spring.social_website.user.profile.ProfileEntity;
import com.spring.social_website.user.profile.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public String uploadAvatar(String email, MultipartFile file) throws IOException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ProfileEntity profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        // delete old avatar from Cloudinary if exists
        if (profile.getAvatarUrl() != null) {
            String publicId = extractPublicId(profile.getAvatarUrl());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of(
                "folder",          "avatars",
                "transformation",  "c_fill,w_400,h_400,g_face",
                "allowed_formats", new String[]{"jpg", "jpeg", "png", "webp"}
        ));

        String url = (String) result.get("secure_url");
        profile.setAvatarUrl(url);
        profileRepository.save(profile);

        return url;
    }

    // extracts public_id from Cloudinary URL for deletion
    private String extractPublicId(String url) {
        // e.g. https://res.cloudinary.com/demo/image/upload/v123/avatars/abc.jpg -> avatars/abc
        int uploadIdx = url.indexOf("/upload/");
        String afterUpload = url.substring(uploadIdx + 8);
        // strip version segment if present (v1234567890/)
        if (afterUpload.startsWith("v") && afterUpload.indexOf("/") > 0) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // strip extension
        int dotIdx = afterUpload.lastIndexOf(".");
        return dotIdx != -1 ? afterUpload.substring(0, dotIdx) : afterUpload;
    }
}

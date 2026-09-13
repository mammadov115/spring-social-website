package com.spring.social_website.image;

import com.cloudinary.Cloudinary;
import com.spring.social_website.image.dto.ImageResponseDto;
import com.spring.social_website.image.dto.ImageUploadRequestDto;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Transactional
    public ImageResponseDto upload(String email, ImageUploadRequestDto request,
                                   MultipartFile file) throws IOException {
        UserEntity owner = findUser(email);

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of("folder", "images")
        );
        String imageUrl = (String) result.get("secure_url");

        ImageEntity image = ImageEntity.builder()
                .title(request.title())
                .description(request.description())
                .imageUrl(imageUrl)
                .owner(owner)
                .build();

        return toDto(imageRepository.save(image), owner);
    }

    public Page<ImageResponseDto> getFeed(String email, Pageable pageable) {
        UserEntity me = findUser(email);
        return imageRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(image -> toDto(image, me));
    }

    public ImageResponseDto getById(String email, UUID id) {
        UserEntity me = findUser(email);
        return toDto(findImage(id), me);
    }

    @Transactional
    public void delete(String email, UUID id) {
        UserEntity me = findUser(email);
        ImageEntity image = findImage(id);

        if (!image.getOwner().getId().equals(me.getId())) {
            throw new AccessDeniedException("You can only delete your own images");
        }

        // extract public id and delete from Cloudinary
        String publicId = extractPublicId(image.getImageUrl());
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (IOException e) {
            // log but don't block DB deletion
        }

        imageRepository.delete(image);
    }

    @Transactional
    public ImageResponseDto toggleLike(String email, UUID id) {
        UserEntity me = findUser(email);
        ImageEntity image = findImage(id);

        if (image.getLikedBy().contains(me)) {
            image.getLikedBy().remove(me);
        } else {
            image.getLikedBy().add(me);
        }

        return toDto(imageRepository.save(image), me);
    }

    @Transactional
    public ImageResponseDto toggleBookmark(String email, UUID id) {
        UserEntity me = findUser(email);
        ImageEntity image = findImage(id);

        if (image.getBookmarkedBy().contains(me)) {
            image.getBookmarkedBy().remove(me);
        } else {
            image.getBookmarkedBy().add(me);
        }

        return toDto(imageRepository.save(image), me);
    }

    public Page<ImageResponseDto> getBookmarks(String email, Pageable pageable) {
        UserEntity me = findUser(email);
        return imageRepository.findBookmarkedByUserEmail(email, pageable)
                .map(image -> toDto(image, me));
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private ImageEntity findImage(UUID id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));
    }

    private String extractPublicId(String url) {
        // e.g. .../upload/v123/images/abc.jpg -> images/abc
        int uploadIdx = url.indexOf("/upload/");
        String afterUpload = url.substring(uploadIdx + 8);
        // strip version segment if present
        if (afterUpload.startsWith("v") && afterUpload.indexOf('/') > 0) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }
        // strip extension
        int dotIdx = afterUpload.lastIndexOf('.');
        return dotIdx > 0 ? afterUpload.substring(0, dotIdx) : afterUpload;
    }

    private ImageResponseDto toDto(ImageEntity image, UserEntity me) {
        return new ImageResponseDto(
                image.getId(),
                image.getTitle(),
                image.getDescription(),
                image.getImageUrl(),
                image.getOwner().getEmail(),
                image.getOwner().getFirstName() + " " + image.getOwner().getLastName(),
                image.getLikedBy().size(),
                image.getBookmarkedBy().size(),
                image.getLikedBy().contains(me),
                image.getBookmarkedBy().contains(me),
                image.getCreatedAt()
        );
    }
}

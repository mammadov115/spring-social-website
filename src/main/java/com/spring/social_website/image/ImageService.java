package com.spring.social_website.image;

import com.cloudinary.Cloudinary;
import com.spring.social_website.image.ImageRepository.ImageCount;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        return toDtoSingle(imageRepository.save(image), email);
    }

    @Transactional(readOnly = true)
    public Page<ImageResponseDto> getFeed(String email, Pageable pageable) {
        Page<ImageEntity> page = imageRepository.findAllWithOwner(pageable);
        return toDtoBatch(page, email);
    }

    @Transactional(readOnly = true)
    public ImageResponseDto getById(String email, UUID id) {
        return toDtoSingle(findImage(id), email);
    }

    @Transactional
    public void delete(String email, UUID id) {
        UserEntity me = findUser(email);
        ImageEntity image = findImage(id);

        if (!image.getOwner().getId().equals(me.getId())) {
            throw new AccessDeniedException("You can only delete your own images");
        }

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

        return toDtoSingle(imageRepository.save(image), email);
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

        return toDtoSingle(imageRepository.save(image), email);
    }

    @Transactional(readOnly = true)
    public Page<ImageResponseDto> getBookmarks(String email, Pageable pageable) {
        Page<ImageEntity> page = imageRepository.findBookmarkedByUserEmail(email, pageable);
        return toDtoBatch(page, email);
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private ImageEntity findImage(UUID id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));
    }

    // single image -- getById, toggleLike, toggleBookmark, upload ucun
    private ImageResponseDto toDtoSingle(ImageEntity image, String email) {
        UUID imageId = image.getId();
        return new ImageResponseDto(
                imageId,
                image.getTitle(),
                image.getDescription(),
                image.getImageUrl(),
                image.getOwner().getEmail(),
                image.getOwner().getFirstName() + " " + image.getOwner().getLastName(),
                imageRepository.countLikes(imageId),
                imageRepository.countBookmarks(imageId),
                imageRepository.isLikedBy(imageId, email),
                imageRepository.isBookmarkedBy(imageId, email),
                image.getCreatedAt()
        );
    }

    // batch -- getFeed, getBookmarks ucun; N image ucun sabit 4 query
    private Page<ImageResponseDto> toDtoBatch(Page<ImageEntity> page, String email) {
        List<UUID> ids = page.stream().map(ImageEntity::getId).toList();

        if (ids.isEmpty()) {
            return page.map(image -> toDtoSingle(image, email));
        }

        Map<UUID, Long> likeCounts = toCountMap(imageRepository.countLikesBatch(ids));
        Map<UUID, Long> bookmarkCounts = toCountMap(imageRepository.countBookmarksBatch(ids));
        Set<UUID> likedIds = Set.copyOf(imageRepository.findLikedImageIds(ids, email));
        Set<UUID> bookmarkedIds = Set.copyOf(imageRepository.findBookmarkedImageIds(ids, email));

        return page.map(image -> {
            UUID imageId = image.getId();
            return new ImageResponseDto(
                    imageId,
                    image.getTitle(),
                    image.getDescription(),
                    image.getImageUrl(),
                    image.getOwner().getEmail(),
                    image.getOwner().getFirstName() + " " + image.getOwner().getLastName(),
                    likeCounts.getOrDefault(imageId, 0L).intValue(),
                    bookmarkCounts.getOrDefault(imageId, 0L).intValue(),
                    likedIds.contains(imageId),
                    bookmarkedIds.contains(imageId),
                    image.getCreatedAt()
            );
        });
    }

    private Map<UUID, Long> toCountMap(Collection<ImageCount> counts) {
        return counts.stream()
                .collect(Collectors.toMap(ImageCount::imageId, ImageCount::count));
    }

    private String extractPublicId(String url) {
        int uploadIdx = url.indexOf("/upload/");
        String afterUpload = url.substring(uploadIdx + 8);
        if (afterUpload.startsWith("v") && afterUpload.indexOf('/') > 0) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }
        int dotIdx = afterUpload.lastIndexOf('.');
        return dotIdx > 0 ? afterUpload.substring(0, dotIdx) : afterUpload;
    }
}

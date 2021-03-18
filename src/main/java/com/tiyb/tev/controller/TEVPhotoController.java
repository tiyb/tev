package com.tiyb.tev.controller;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.exception.NoParentPostException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.PhotoRepository;
import com.tiyb.tev.repository.PostRepository;

/**
 * REST controller for working with photos. Uses the
 * {@link com.tiyb.tev.controller.helper.RepoAbstractor RepoAbstractor} class in some instances but
 * not in others, because photos work differently from other types of posts.
 *
 * @author tiyb
 *
 */
@RestController
@RequestMapping("/api")
public class TEVPhotoController {

    /**
     * Used in buffering operations
     */
    private static final int BYTE_BUFFER_LENGTH = 1024;

    private Logger logger = LoggerFactory.getLogger(TEVPhotoController.class);

    /**
     * Autowired post repo
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * Autowired photo repo
     */
    @Autowired
    private PhotoRepository photoRepo;

    /**
     * Autowired Metadata controller
     */
    @Autowired
    private TEVMetadataRestController mdController;

    /**
     * The Tumblr export doesn't always include every image, for some reason. However, in many cases
     * the images referred to in the image URLs from Tumblr's export XML still exist on Tumblr's
     * servers. So, in cases where a post is missing its images, this API can be used to fetch the
     * images from Tumblr's servers (according to their URLs in the XML), download them, and save
     * them to the media directory being used by TEV, using the naming convention Tumblr would have
     * used, if the images had been included in the export.
     *
     * @param blog   Not used
     * @param postId The ID of the post to be "fixed"
     * @return bool indicating whether the action completed successfully or not.
     */
    @GetMapping("/posts/{blog}/{id}/fixPhotos")
    public Boolean fixPhotosForBlogForPost(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        String imageDirectory = mdController.getMetadataForBlog(blog).getBaseMediaPath();
        if (imageDirectory == null || imageDirectory.equals(StringUtils.EMPTY)) {
            return false;
        }

        if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
            imageDirectory = imageDirectory.concat("/");
        }

        final List<Photo> photos = photoRepo.findByPostIdOrderByOffset(postId);
        boolean response = true;

        for (int i = 0; i < photos.size(); i++) {
            final Photo photo = photos.get(i);
            final String url = photo.getUrl1280();
            final String ext = url.substring(url.lastIndexOf('.'));
            try {
                final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                final FileOutputStream out =
                        new FileOutputStream(String.format("%s%s_%d%s", imageDirectory, photo.getPostId(), i, ext));
                final byte[] dataBuffer = new byte[BYTE_BUFFER_LENGTH];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, BYTE_BUFFER_LENGTH)) != -1) {
                    out.write(dataBuffer, 0, bytesRead);
                }
                out.close();
            } catch (IOException e) {
                response = false;
            }
        }
        return response;
    }

    /**
     * GET request for listing all photos for a given blog. Does not use underlying RepoAbstractor
     * class, since the logic is slightly different than from other post types.
     *
     * @param blog Blog for which photos should be retrieved
     * @return {@link java.util.List List} of all photos in the database
     */
    @GetMapping("/posts/{blog}/photos")
    public List<Photo> getAllPhotosForBlog(@PathVariable("blog") final String blog) {
        final List<Post> posts = postRepo.findByTumblelogAndType(blog, Post.POST_TYPE_PHOTO);
        final List<Photo> photos = new ArrayList<Photo>();

        for (Post post : posts) {
            final List<Photo> photoResponse = photoRepo.findByPostIdOrderByOffset(post.getId());
            photos.addAll(photoResponse);
        }

        return photos;
    }

    /**
     * POST request to submit a Tumblr "photo post" into the system for a given blog. Doesn't use
     * the abstractor, since logic is different for photos.
     *
     * @param blog  For validation purposes
     * @param photo The data to be submitted
     * @return The same {@link com.tiyb.tev.datamodel.Photo Photo} object that was submitted
     */
    @PostMapping("/posts/{blog}/photo")
    public Photo createPhotoForBlog(@PathVariable("blog") final String blog, @Valid @RequestBody final Photo photo) {
        final Optional<Post> post = postRepo.findById(photo.getPostId());
        if (!post.isPresent()) {
            logger.error("Tried to submit link for a post that doesn't exist: {}", photo.getPostId());
            throw new NoParentPostException();
        } else {
            assert blog.equals(post.get().getTumblelog());
        }
        return photoRepo.save(photo);
    }

    /**
     * GET to return the photos for a given post
     *
     * @param blog   Not used
     * @param postId The Post ID
     * @return List of photos
     */
    @GetMapping("/posts/{blog}/{id}/photo")
    public List<Photo> getPhotoForBlogById(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId) {
        return photoRepo.findByPostId(postId);
    }

    /**
     * PUT to update a Photo
     *
     * @param blog         not used
     * @param postId       The ID of the post to be updated
     * @param photoDetails The data to be updated
     * @return The same {@link com.tiyb.tev.datamodel.Photo Photo} object that was submitted
     */
    @PutMapping("/posts/{blog}/{id}/photo")
    public Photo updatePhotoForBlog(@PathVariable("blog") final String blog, @PathVariable("id") final String postId,
            @RequestBody final Photo photoDetails) {
        final Photo original = photoRepo.findById(photoDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PHoto", "id", photoDetails.getId()));

        original.updateItem(photoDetails);

        return photoRepo.save(original);
    }

    /**
     * DEL to delete all "photo" posts in the DB for a given blog. Because a given Photo post can
     * actually have multiple photos in it, the logic is to retrieve all photo posts for the given
     * blog, then delete each photo for that post.
     *
     * @param blog Blog for which photos should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/photos")
    public ResponseEntity<?> deleteAllPhotosForBlog(@PathVariable("blog") final String blog) {
        final List<Photo> photos = getAllPhotosForBlog(blog);
        for (Photo p : photos) {
            photoRepo.delete(p);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * DEL to delete a single photo by ID for a given blog
     *
     * @param blog    Not used
     * @param postId  the ID of the post to be deleted
     * @param photoID The ID of the photo to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/posts/{blog}/{id}/photo/{photoId}")
    public ResponseEntity<?> deletePhotoForBlog(@PathVariable("blog") final String blog,
            @PathVariable("id") final String postId, @PathVariable("photoId") final Long photoID) {
        final Photo photo =
                photoRepo.findById(photoID).orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoID));

        photoRepo.delete(photo);

        return ResponseEntity.ok().build();
    }
}

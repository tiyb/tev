package com.tiyb.tev.controller.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.datamodel.TEVCommonItems;
import com.tiyb.tev.exception.NoParentPostException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.repository.PostRepository;

/**
 * Generic class for working with underlying repos for the various post types (Answer, Regular,
 * ...).
 *
 * @author tiyb
 *
 * @param <T> The Type of post (Answer, REgular, ...) being worked with.
 */
public class RepoAbstractor<T extends TEVCommonItems<T>> {

    private Logger logger = LoggerFactory.getLogger(RepoAbstractor.class);

    /**
     * Generic repo for working with T posts
     */
    private JpaRepository<T, String> typeRepo;

    /**
     * The specific repo for working with Posts
     */
    private PostRepository postRepo;

    /**
     * Name of the type being worked with
     */
    private String typeName;

    /**
     * Constructor for the object
     *
     * @param instanceOfTypeRepo an instance of the repo being worked with
     * @param theTypeName        The name of the type of post being worked with
     */
    public RepoAbstractor(final JpaRepository<T, String> instanceOfTypeRepo, final String theTypeName,
            final PostRepository instanceOfPostRepo) {
        this.typeName = theTypeName;
        this.postRepo = instanceOfPostRepo;
        this.typeRepo = instanceOfTypeRepo;
    }

    /**
     * Creates a T post in the DB for the given blog and Post ID
     *
     * @param blog   Name of the blog
     * @param itemId ID of the item
     * @param item   Actual T item
     * @return T item as saved in the DB
     */
    public T createForBlog(final String blog, final String itemId, final T item) {
        final Optional<Post> post = postRepo.findById(itemId);
        if (!post.isPresent()) {
            logger.error("Tried to submit {} item for post that doesn't exist: {}", this.typeName, itemId);
            throw new NoParentPostException();
        } else {
            assert blog.equals(post.get().getTumblelog());
        }

        item.setPostId(itemId);
        return typeRepo.save(item);
    }

    /**
     * Gets all posts of T type
     *
     * @param blog The name of the blog to search
     * @return List of T posts
     */
    public List<T> getAllForBlog(final String blog) {
        final List<Post> posts = postRepo.findByTumblelogAndType(blog, typeName);
        final List<T> returnList = new ArrayList<T>();

        for (Post p : posts) {
            final Optional<T> response = typeRepo.findById(p.getId());
            if (response.isPresent()) {
                returnList.add(response.get());
            }
        }

        return returnList;
    }

    /**
     * Returns an instance of a T post by blog and ID
     *
     * @param id ID of the item to be returned
     * @return T type post
     * @throws ResourceNotFoundException If the item can't be found in the DB
     */
    public T getItemById(final String id) throws ResourceNotFoundException {
        final Optional<T> response = typeRepo.findById(id);

        if (response.isPresent()) {
            return response.get();
        }

        throw new ResourceNotFoundException(this.typeName, "id", id);
    }

    /**
     * Updates a T item in the DB
     *
     * @param postId      ID of the post to which this T item belongs
     * @param itemDetails Updates to be made
     * @return Updated item
     */
    public T updateItem(final String postId, final T itemDetails) {
        final T original =
                typeRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException(typeName, "id", postId));

        original.updateItem(itemDetails);

        return typeRepo.save(original);
    }

    /**
     * Deletes all T items from the DB for a given blog
     *
     * @param blog Blog for which T items should be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    public ResponseEntity<?> deleteAllItemsForBlog(final String blog) {
        final List<Post> posts = postRepo.findByTumblelogAndType(blog, typeName);

        for (Post p : posts) {
            final Optional<T> response = typeRepo.findById(p.getId());
            if (!response.isPresent()) {
                logger.error("Attempting to delete {} item that doesn't exist: {}", typeName, p.getId());
                return ResponseEntity.badRequest().build();
            }

            typeRepo.delete(response.get());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Deletes an item from the DB
     *
     * @param itemId ID of item to be deleted
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    public ResponseEntity<?> deleteItem(final String itemId) {
        final T item =
                typeRepo.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(typeName, "id", itemId));

        typeRepo.delete(item);

        return ResponseEntity.ok().build();
    }

}

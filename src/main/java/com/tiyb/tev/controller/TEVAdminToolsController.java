package com.tiyb.tev.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Photo;
import com.tiyb.tev.datamodel.Post;
import com.tiyb.tev.exception.InvalidTypeException;
import com.tiyb.tev.repository.PostRepository;

/**
 * <p>
 * A REST controller with some administration tools for working with the database behind the scenes.
 * </p>
 *
 * <p>
 * If TEV were ever to become anything more than just a local application, proper security would be
 * needed around these operations, but since that's not the case the security is lax. None of these
 * APIs are ever intended to be used by the UI.
 * </p>
 *
 * @author tiyb
 */
@RestController
@RequestMapping("/admintools")
public class TEVAdminToolsController {

    /**
     * Used for logging errors related to bad image directories
     */
    private static final String INVALID_IMAGE_DIRECTORY = "Invalid image directory: {}";

    private Logger logger = LoggerFactory.getLogger(TEVAdminToolsController.class);

    /**
     * The JDBC template to be used for operations within the controller.
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * The REST controller used for working with posts
     */
    @Autowired
    private TEVPostRestController postController;

    /**
     * The REST controller used for working with settings/metadata.
     */
    @Autowired
    private TEVMetadataRestController mdController;

    /**
     * The underlying repo used for posts
     */
    @Autowired
    private PostRepository postRepo;

    /**
     * Success message returned from the API.
     */
    @Value("${controllers.admintools.success}")
    private String successMessage;

    /**
     * Localized message returned from the API when the images directory in metadata is invalid.
     */
    @Value("${controllers.admintools.invalidImagesDirectory}")
    private String invalidImagesMessage;

    /**
     * Localized message used when the source images directory is not valid
     */
    @Value("${controllers.admintools.invalidSourceImagesDirectory}")
    private String invalidSourceImagesMessage;

    /**
     * Localized message used when an error is encountered copying media files
     */
    @Value("${controllers.admintools.errorCopyingFiles}")
    private String errorCopyingFilesMessage;

    /**
     * Used to compact the database upon shutdown. This causes shutdown to take longer, but it's not
     * very noticeable for an application of this size with a local DB.
     */
    @PreDestroy
    public void preDestroy() {
        try {
            jdbcTemplate.execute("SHUTDOWN COMPACT");
        } catch (DataAccessException e) {
            logger.error("Error encountered in preDestroy method: ", e);
        }
    }

    /**
     * GET to return a list of posts by a particular type for a given blog
     *
     * @param blog Blog for which posts should be returned
     * @param type The type of post to return
     * @return List of Posts
     */
    @GetMapping("/posts/{blog}/type/{type}")
    public List<Post> getPostsByBlogByType(@PathVariable("blog") final String blog,
            @PathVariable("type") final String type) {
        final List<String> allTypeNames = mdController.getAllTypes();

        if (!TEVMetadataRestController.isValidType(type, allTypeNames)) {
            logger.error("Invalid type name: {}", type);
            throw new InvalidTypeException();
        }

        return postRepo.findByTumblelogAndType(blog, type);
    }

    /**
     * GET request to mark all posts in the database as read for a given blog
     *
     * @param blog Blog for which the posts should be marked read
     * @return Success indicator
     */
    @GetMapping("/posts/{blog}/markAllRead")
    public ResponseEntity<String> markAllPostsReadForBlog(@PathVariable("blog") final String blog) {
        final List<Post> posts = postController.getAllPostsForBlog(blog);

        for (Post post : posts) {
            postController.markPostReadForBlog(blog, post.getId());
        }

        return new ResponseEntity<String>(successMessage, null, HttpStatus.OK);
    }

    /**
     * GET request to mark all posts in the database as unread
     *
     * @param blog Name of the blog for which posts should be marked unread
     * @return Success indicator
     */
    @GetMapping("/posts/{blog}/markAllUnread")
    public ResponseEntity<String> markAllPostsUnreadForBlog(@PathVariable("blog") final String blog) {
        final List<Post> posts = postController.getAllPostsForBlog(blog);

        for (Post post : posts) {
            postController.markPostUnreadForBlog(blog, post.getId());
        }

        return new ResponseEntity<String>(successMessage, null, HttpStatus.OK);
    }

    /**
     * GET request to delete all of the duplicate images that end up on the hard drive, coming from
     * the Tumblr export, for a given blog. For photo posts, when the images are actually in the
     * export, Tumblr includes duplicates of every image, such that there are twice as many images
     * stored on the HD as necessary. This helper API retrieves all Photo posts, checks how many
     * images are supposed to be there, then looks at the HD to see the number of files there. If
     * there are exactly twice as many images as there should be, the duplicates are deleted.
     *
     * @param blog Blog for which images should be cleaned
     * @return String indicating the success or failure of the process
     */
    @GetMapping("/posts/{blog}/cleanImagesOnHD")
    public ResponseEntity<String> cleanImagesOnHDForBlog(@PathVariable("blog") final String blog) {
        final ArrayList<String> allCleanFiles = new ArrayList<String>();
        String imageDirectory = mdController.getMetadataForBlog(blog).getBaseMediaPath();
        if (imageDirectory == null || imageDirectory.equals(StringUtils.EMPTY)) {
            logger.error(INVALID_IMAGE_DIRECTORY, imageDirectory);
            return new ResponseEntity<String>(invalidImagesMessage, null, HttpStatus.BAD_REQUEST);
        }

        if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
            imageDirectory = imageDirectory.concat("/");
        }

        final File folder = new File(imageDirectory);

        final List<Post> photoPosts = getPostsByBlogByType(blog, Post.POST_TYPE_PHOTO);
        for (Post post : photoPosts) {
            final List<Photo> photosForPost =
                    postController.getPhotoController().getPhotoForBlogById(post.getTumblelog(), post.getId());
            final int numPhotos = photosForPost.size();

            final File[] imagesForPost = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.startsWith(post.getId());
                }
            });
            if (imagesForPost.length == (numPhotos * 2)) {
                for (int i = numPhotos; i < imagesForPost.length; i++) {
                    imagesForPost[i].delete();
                }
            }
            for (int i = 0; i < imagesForPost.length; i++) {
                allCleanFiles.add(imagesForPost[i].getName());
            }
        }

        final File[] remainingImages = folder.listFiles();
        for (int i = 0; i < remainingImages.length; i++) {
            if (!allCleanFiles.contains(remainingImages[i].getName())) {
                remainingImages[i].delete();
            }
        }
        return new ResponseEntity<String>(successMessage, null, HttpStatus.OK);
    }

    /**
     * Copies all files from a source directory (posted as the body of the request) to the images
     * directory stored in the metadata. Calls the {@link #cleanImagesOnHDForBlog(String)
     * cleanImagesOnHDForBlog()} method when it's done, to clean the directory back up.
     *
     * @param blog      The name of the blog for which images shoudl be imported
     * @param imagePath The path of the source directory, from whence images should be retrieved.
     * @return Success/failure message
     */
    @PostMapping("/posts/{blog}/importImages")
    public ResponseEntity<String> importImagesForBlog(@PathVariable("blog") final String blog,
            @RequestBody final String imagePath) {
        String imageDirectory = mdController.getMetadataForBlog(blog).getBaseMediaPath();
        if (imageDirectory == null || imageDirectory.equals(StringUtils.EMPTY)) {
            logger.error(INVALID_IMAGE_DIRECTORY, imageDirectory);
            return new ResponseEntity<String>(invalidImagesMessage, null, HttpStatus.BAD_REQUEST);
        }

        if (imageDirectory.charAt(imageDirectory.length() - 1) != '/') {
            imageDirectory = imageDirectory.concat("/");
        }

        final File destinationFolder = new File(imageDirectory);
        if (!destinationFolder.isDirectory()) {
            logger.error(INVALID_IMAGE_DIRECTORY, imageDirectory);
            return new ResponseEntity<String>(invalidImagesMessage, null, HttpStatus.BAD_REQUEST);
        }
        final Path destinationFolderPath = destinationFolder.toPath();

        final File sourceFolder = new File(imagePath);
        if (!sourceFolder.isDirectory()) {
            logger.error(INVALID_IMAGE_DIRECTORY, imagePath);
            return new ResponseEntity<String>(invalidSourceImagesMessage, null, HttpStatus.BAD_REQUEST);
        }

        final File[] sourceFiles = sourceFolder.listFiles();
        for (File inputFile : sourceFiles) {
            final Path inputFilePath = inputFile.toPath();
            try {
                Files.copy(inputFilePath, destinationFolderPath.resolve(inputFilePath.getFileName()));
            } catch (FileAlreadyExistsException e) {
                logger.debug("File already exists: {}", inputFilePath.getFileName());
                // Skip copying files that don't exist
            } catch (IOException e) {
                logger.error("Error copying file from source to destination: {}", inputFilePath.getFileName());
                logger.debug("File Copy Error:", e);
                return new ResponseEntity<String>(errorCopyingFilesMessage, null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return cleanImagesOnHDForBlog(blog);
    }

}

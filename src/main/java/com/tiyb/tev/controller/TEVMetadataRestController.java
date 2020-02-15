package com.tiyb.tev.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiyb.tev.datamodel.Metadata;
import com.tiyb.tev.datamodel.helpers.StaticListData;
import com.tiyb.tev.exception.NoMetadataFoundException;
import com.tiyb.tev.exception.ResourceNotFoundException;
import com.tiyb.tev.exception.UnableToDeleteMetadataException;
import com.tiyb.tev.repository.MetadataRepository;

/**
 * REST controller for working with the application's Metadata -- it's settings. Settings are kept
 * on a blog-by-blog basis. Whenever a Metadata object is encountered that doesn't have a valid
 * <b>theme</b> set, the default is applied; not having a theme is a fatal bug for the application,
 * so extra care is taken to guard agaisnt that eventuality.
 *
 * @author tiyb
 */
@RestController
@RequestMapping("/api")
public class TEVMetadataRestController {

    /**
     * List of all post types available from Tumblr/TEV
     */
    public static final List<String> POST_TYPES =
            new ArrayList<String>(Arrays.asList("answer", "link", "photo", "regular", "video"));

    private Logger logger = LoggerFactory.getLogger(TEVMetadataRestController.class);

    /**
     * The repo for working with Metadata
     */
    @Autowired
    private MetadataRepository metadataRepo;

    /**
     * For working with Posts
     */
    @Autowired
    private TEVPostRestController postController;

    /**
     * For working with Conversations
     */
    @Autowired
    private TEVConvoRestController convoController;

    /**
     * GET to return all Types stored in the system. This implementation is <i>very</i> hard-coded,
     * because there are only a specific number of types actually supported by Tumblr. The
     * {@link #POST_TYPES} member is public, but this API is still useful for JavaScript uses.
     *
     * @return {@link java.util.List List} of type strings
     */
    @GetMapping("/types")
    public List<String> getAllTypes() {
        return POST_TYPES;
    }

    /**
     * Helper function to determine if a "type" is valid or not, per Tumblr/TEV list of types.
     * Though it is only used within this class, it was made public just in case other code ever
     * needs it.
     *
     * @param typeName       Name of the type to be validated
     * @param validTypeNames List of all of the valid types in the system
     * @return True/False
     */
    public static boolean isValidType(final String typeName, final List<String> validTypeNames) {
        return validTypeNames.contains(typeName);
    }

    /**
     * <p>
     * GET to return all metadata objects stored in the system, for all blogs.
     * </p>
     *
     * <p>
     * If there is no data in the database, a default object is returned based on
     * {@link com.tiyb.tev.datamodel.Metadata#newDefaultMetadata() Metadata#newDefaultMetadata()}.
     * </p>
     *
     * @return The {@link com.tiyb.tev.datamodel.Metadata Metadata} object stored in the database,
     *         or an empty object if the table has no data.
     */
    @GetMapping("/metadata")
    public List<Metadata> getAllMetadata() {
        final List<Metadata> list = metadataRepo.findAll();

        if (list.size() < 1) {
            list.add(Metadata.newDefaultMetadata());
            return list;
        }

        for (Metadata md : list) {
            if (!Metadata.THEMES.contains(md.getTheme())) {
                md.setTheme(Metadata.DEFAULT_THEME);
            }
        }

        return list;
    }

    /**
     * GET to return the metadata for a particular blog based on the Metadata ID.
     *
     * @param id The ID of the MD to return
     * @return {@link com.tiyb.tev.datamodel.Metadata Metadata} object containing the settings for
     *         the blog in question
     */
    @GetMapping("/metadata/{id}")
    public Metadata getMetadataByID(@PathVariable("id") final Integer id) {
        final Optional<Metadata> response = metadataRepo.findById(id);
        if (!response.isPresent()) {
            return null;
        }

        final Metadata md = response.get();

        if (!Metadata.THEMES.contains(md.getTheme())) {
            md.setTheme(Metadata.DEFAULT_THEME);
        }

        return md;
    }

    /**
     * PUT to mark a blog as the default blog by setting <code>isDefault</code> to true for its MD,
     * and false to all other MDs.
     *
     * @param id ID of the Metadata objec to be marked as the default
     * @return The updated Metadata object
     */
    @PutMapping("/metadata/{id}/markAsDefault")
    public Metadata markBlogAsDefault(@PathVariable("id") final Integer id) {
        final List<Metadata> allMDs = metadataRepo.findAll();
        Metadata response = null;

        for (Metadata md : allMDs) {
            if (id.equals(md.getId())) {
                md.setIsDefault(true);
                metadataRepo.save(md);
                response = md;
            } else {
                md.setIsDefault(false);
                metadataRepo.save(md);
            }
        }

        return response;
    }

    /**
     * Gets the default Metadata object. If none is set as the default, the first is set as the
     * default (and saved), then returned. If no MD exists at <i>all,</i> one is created and saved
     * and returned.
     *
     * @return The default MD object
     */
    @GetMapping("/metadata/default")
    public Metadata getDefaultMetadata() {
        return metadataRepo.findByIsDefaultTrue();
    }

    /**
     * Helper API for just getting the default blog name -- i.e. getting the default MD, and
     * returning that MD's blog property.
     *
     * @return String of the blog's name
     */
    @GetMapping("/metadata/default/blogName")
    public String getDefaultBlogName() {
        try {
            final Metadata defaultMD = getDefaultMetadata();
            return defaultMD.getBlog();
        } catch (NullPointerException e) {
            throw new NoMetadataFoundException();
        }
    }

    /**
     * GET to return the Metadata for a given blog, by name
     *
     * @param blog Name of the blog in question
     * @return Metadata for the blog
     */
    @GetMapping("/metadata/byBlog/{blog}")
    public Metadata getMetadataForBlog(@PathVariable("blog") final String blog) {
        return metadataRepo.findByBlog(blog);
    }

    /**
     * Specialized method to return the Metadata for a given blog, <i>or</i> the default Metadata
     * object (which is also inserted into the DB, with the given blog name), if it doesn't yet
     * exist.
     *
     * @param blog Name of the blog to be returned
     * @return Metadata for the blog, or the default MD object if it didn't originally exist
     */
    @GetMapping("/metadata/byBlog/{blog}/orDefault")
    public Metadata getMetadataForBlogOrDefault(@PathVariable("blog") final String blog) {
        final List<Metadata> all = metadataRepo.findAll();

        for (Metadata md : all) {
            if (blog.equals(md.getBlog())) {
                return md;
            }
        }

        final Metadata defaultMD = Metadata.newDefaultMetadata();
        defaultMD.setBlog(blog);
        if (all.size() < 1) {
            defaultMD.setIsDefault(true);
        }
        return metadataRepo.save(defaultMD);
    }

    /**
     * GET to return static data used to populate drop-down lists, for the user to set things like
     * preferred sort order, filters, etc.
     *
     * @return object containing the different lists of strings available
     */
    @GetMapping("/metadata/staticListData")
    public StaticListData getStaticListData() {
        final StaticListData sld = new StaticListData();

        for (String s : Metadata.FILTER_TYPES) {
            sld.getFilterTypes().add(s);
        }
        for (String s : Metadata.SORT_COLUMNS) {
            sld.getSortColumns().add(s);
        }
        for (String s : Metadata.SORT_ORDERS) {
            sld.getSortOrders().add(s);
        }
        for (String s : Metadata.FAV_FILTERS) {
            sld.getFavFilters().add(s);
        }
        for (Integer i : Metadata.PAGE_LENGTHS) {
            sld.getPageLengths().add(i);
        }
        for (String s : Metadata.CONVERSATION_DISPLAY_STYLES) {
            sld.getConversationStyles().add(s);
        }
        for (String s : Metadata.CONVERSATION_SORT_COLUMNS) {
            sld.getConversationSortColumns().add(s);
        }
        for (String s : Metadata.THEMES) {
            sld.getThemes().add(s);
        }

        return sld;
    }

    /**
     * PUT to update metadata details in the database. Assertions have been added around ensuring
     * proper themes are saved.
     *
     * @param id              The ID of the MD being saved
     * @param metadataDetails The data to be updated in the DB
     * @return The updated {@link com.tiyb.tev.datamodel.Metadata Metadata} object
     */
    @PutMapping("/metadata/{id}")
    public Metadata updateMetadata(@PathVariable("id") final Integer id, @RequestBody final Metadata metadataDetails) {
        Assert.hasText(metadataDetails.getTheme(), "Theme must not be null");
        Assert.isTrue(Metadata.THEMES.contains(metadataDetails.getTheme()),
                "Theme must be one of the pre-defined values");

        return metadataRepo.save(metadataDetails);
    }

    /**
     * Removes a Metadata object (and therefore the blog, and all of its settings) from the DB.
     *
     * <ol>
     * <li>Validates that the MD being deleted exists, and isn't the only MD in the system</li>
     * <li>Removes all conversation messages / conversations</li>
     * <li>Removes all posts</li>
     * <li>Removes the Metadata itself</li>
     * <li>Ensures there is at least one MD object left in the system marked as 'default' or marks
     * the first one as default otherwise</li>
     * </ol>
     *
     * @param id Metadata to be removed
     * @return {@link org.springframework.http.ResponseEntity ResponseEntity} with the response
     *         details
     */
    @DeleteMapping("/metadata/{id}")
    public ResponseEntity<?> deleteMetadata(@PathVariable("id") final Integer id) {
        final Optional<Metadata> omd = metadataRepo.findById(id);
        if (!omd.isPresent()) {
            logger.error("Unable to find MD with this ID: {}", id);
            throw new ResourceNotFoundException("Metadata", "ID", id);
        }

        List<Metadata> allMDs = getAllMetadata();
        if (allMDs.size() < 2) {
            logger.error("Attempting to delete the only MD left in the system");
            throw new UnableToDeleteMetadataException();
        }

        final Metadata md = omd.get();

        convoController.deleteAllConvoMsgsForBlog(md.getBlog());
        convoController.deleteAllConversationsForBlog(md.getBlog());

        postController.getRegController().deleteAllRegularsForBlog(md.getBlog());
        postController.getAnswerController().deleteAllAnswersForBlog(md.getBlog());
        postController.getLinkController().deleteAllLinksForBlog(md.getBlog());
        postController.getPhotoController().deleteAllPhotosForBlog(md.getBlog());
        postController.getVideoController().deleteAllVideosForBlog(md.getBlog());
        postController.deleteAllPostsForBlog(md.getBlog());
        postController.deleteAllHashtagsForBlog(md.getBlog());

        metadataRepo.deleteById(id);

        allMDs = getAllMetadata();
        boolean aDefaultExists = false;
        for (Metadata m : allMDs) {
            if (m.getIsDefault()) {
                aDefaultExists = true;
            }
        }
        if (!aDefaultExists) {
            markBlogAsDefault(allMDs.get(0).getId());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Package-private method to delete all MD objects in the DB. Never used by TEV; only used by
     * JUnit test cases.
     */
    void deleteAllMD() {
        metadataRepo.deleteAll();
    }

}

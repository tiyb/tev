/**
 * <p>
 * This package contains <b>Controllers</b> for the application (both REST and
 * UI). There are three main REST controllers in use (
 * {@link com.tiyb.tev.controller.TEVPostRestController TEVPostRestController},
 * {@link com.tiyb.tev.controller.TEVConvoRestController
 * TEVConvoRestController}, and
 * {@link com.tiyb.tev.controller.TEVMetadataRestController
 * TEVMetadataRestController}), as well as a "hidden" controller for APIs not
 * intended for use by the application's UI
 * ({@link com.tiyb.tev.controller.TEVAdminToolsController
 * TEVAdminToolsController})
 * </p>
 * 
 * <p>
 * A "more is better than less" approach has been taken -- some APIs have been
 * created even if they aren't used by the TEV application, just in case.
 * </p>
 * 
 * <p>
 * In general, the structure of web and API calls supported is as follows:
 * </p>
 * 
 * <pre>
 * web
 *  ├─ / or /index:        The main page for the website
 *  ├─ /metadata:          A settings page where the XML can be uploaded, and 
 *  │                      other configurations made to the application
 *  ├─ /postDataUpload:    For uploading the Tumblr XML for Post exports
 *  ├─ /conversationDataUpload:    
 *  │                      For uploading the Tumblr XML for Conversation exports
 *  ├─ /postViewer:        The viewer for Tumblr posts, as rendered by TEV
 *  ├─ /viewerMedia
 *  │    └─ /{imageName}:  Returns a binary image from the file system
 *  ├─ /viewerVideo
 *  │    └─ /{videoName}:  Returns a binary video from the file system
 *  ├─ /footer:            Returns the standard footer
 *  └─ /header:            Returns the standard header
 * 
 * REST API
 *  └─ /api
 *    ├─ /posts:            for working with generic "posts" (GET, POST, DEL)
 *    │   ├─ {id}:          for working with a particular post, by ID (GET, 
 *    │   │ │               PUT, DEL)
 *    │   │ ├─ /{TYPE}:     for working with data for specific types of posts 
 *    │   │ │               (e.g. answer, link, ...) (GET, POST, PUT, DEL)
 *    │   │ ├─ /markRead:   to mark a particular post read (PUT)
 *    │   │ ├─ /markUnread: to mark a particular post unread (PUT)
 *    │   │ ├─ /markFavourite: 
 *    │   │ │               To mark a post as a "favourite" post
 *    │   │ ├─ /markNonFavourite:
 *    │   │ └─ /fixPhotos:  For cases where the export didn't have all of the
 *    │   │                 photos: causes the API to go to Tumblr and download
 *    │   │                 the images for this post, and save them to the 
 *    │   │                 images directory used by the application
 *    │   │                 To un-mark a post as a "favourite" post 
 *    │   ├─ /{TYPE}:       for getting or deleting all data for specific types 
 *    │   │                 (answer, link, etc.) (GET)
 *    │   ├─ /types:        returns all types (GET)
 *    │   └─ /metadata:     for working with metadata stored in the system (GET, 
 *    │                     PUT)
 *    └─ /conversations:    for working with messaging conversations (GET, POST, 
 *        │                 DEL)
 *        └─ /messages:     for working with messages (GET, POST, PUT, DEL)
 * 
 * HELPER ADMIN API
 *  └─ /admintools:
 *    ├─ /compressDB:        Compresses the HSQLDB data file
 *    └─ /posts:
 *        ├─ /markAllRead:   Marks all posts in the dB as unread
 *        └─ /markAllUnread: Marks all posts in the DB as read
 * </pre>
 * 
 * @author tiyb
 *
 */
package com.tiyb.tev.controller;
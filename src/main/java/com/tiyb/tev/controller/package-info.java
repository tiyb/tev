/**
 * <p>
 * This package contains <b>Controllers</b> for the application (both REST and
 * UI). In general, the strucure of web and API calls supported is as follows:
 * </p>
 * 
 * <pre>
 * web
 *  ├ / or /index:        the main page for the website
 *  ├ /messages:          A listing of conversations from the messages export
 *  ├ /metadata:          A config page where the XML can be uploaded, and 
 *  │                     other configurations made
 *  ├ /postDataUpload:    For uploading the Tumblr XML
 *  ├ /postViewer:        The viewer for Tumblr posts, as rendered by TEV
 *  ├ /viewerMedia
 *  │    └ /{imageName}:  Returns a binary image from the file system
 *  ├ /viewerVideo
 *  │    └ /{videoName}:  Returns a binary video from the file system
 *  ├ /footer:            Returns the standard footer
 *  └ /header:            Returns the standard header
 * 
 * REST API
 *  └ /api
 *    ├ /posts:          for working with generic "posts" (GET, POST, DEL)
 *    │ ├ {id}:          for working with a particular post, by ID (GET, 
 *    │ │ │              PUT, DEL)
 *    │ │ ├ /TYPE:       for working with data for specific types of posts 
 *    │ │ │              (e.g. answer, link, ...) (GET, POST, PUT, DEL)
 *    │ │ ├ /markRead:   to mark a particular post read (PUT)
 *    │ │ └ /markUnread: to mark a particular post unread (PUT)
 *    │ ├ /TYPE:         for getting or deleting all data for specific types 
 *    │ │                (answer, link, etc.) (GET)
 *    │ ├ /types:        returns all types (GET)
 *    │ └ /metadata:     for working with metadata stored in the system (GET, 
 *    │                  PUT)
 *    └ /conversations:  for working with messaging conversations (GET, POST, 
 *      │                DEL)
 *      └ /messages:     for working with messages (GET, POST, PUT, DEL)
 * </pre>
 * 
 * @author tiyb
 *
 */
package com.tiyb.tev.controller;
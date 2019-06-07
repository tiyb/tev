/**
 * <p>
 * This package contains the {@link javax.persistence.Entity Entity} classes for
 * working with data in the DB.
 * </p>
 * 
 * <p>
 * The data model is built around the concept of <b>posts</b>; in Tumblr, there
 * are various <i>kinds</i> of post (Answer, Link, Photo, Regular, or Video);
 * with common data that occurs for every post (e.g. URL of the post, date/time
 * the post went live), as well as content that is specific to the individual
 * type. For this reason, the classes here include a
 * {@link com.tiyb.tev.datamodel.Post Post} class, which is used for every post,
 * as well as individual classes for the post types.
 * </p>
 * 
 * <p>
 * To properly enact this, there are tables in the underlying database to
 * represent each type of post, as well as a central table for all posts. The
 * individual tables link back to the main post entry, via <b>foreign keys</b>.
 * The application "cheats" here, a little, bit, whereby the foreign key for
 * each child table is also the primary key for that table; this is possible
 * since there is a one-to-one relationship between the tables (each record in
 * the individual tables links back to one and only one post). (The exception is
 * the table for holding photos, since there can be multiple photos associated
 * with a given post. For this reason, the photo entity has a primary key, as
 * well as the foreign key to the post.)
 * </p>
 * 
 * <p>
 * The other tables/classes represented here are for <b>metadata</b> (data that
 * didn't come from the Tumblr export, but which is needed for the application
 * to run), as well as a list of post <b>types</b>. The latter could have been
 * hard-coded into the application -- there are only 5 -- but it was decided to
 * keep things generic, and make the types table-driven.
 * </p>
 * 
 * @author tiyb
 *
 */
package com.tiyb.tev.datamodel;
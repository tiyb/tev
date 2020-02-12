package com.tiyb.tev.datamodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.repository.HashtagRepository;
import com.tiyb.tev.repository.PostRepository;

/**
 * Unit Tests for working with the Post Repo. A bit more testing is done here
 * than is done for most XX Repo unit testing, just to verify that things are
 * working as they should. In theory, if Spring Boot libraries are updated in
 * such a way that it would break the application, these Unit Tests would
 * highlight that.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class PostRepositoryUnitTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private PostRepository postRepo;

	@Autowired
	private HashtagRepository hashtagRepo;

	/**
	 * Verify that posts, once in the DB, can be retrieved via the Post Repo
	 * implementation
	 */
	@Test
	public void findPost() {
		// given
		Post post = new Post();
		post.setDate("Jan 1, 2019");
		post.setDateGmt("123456");
		post.setId(1L);
		post.setIsFavourite(false);
		post.setIsRead(false);
		post.setIsReblog(true);
		post.setReblogKey("abc");
		post.setSlug("slug");
		post.setTags("tag 1, tag 2");
		post.setType("regular");
		post.setUrl("http://url.com");
		post.setUrlWithSlug("http://url.com/slug");

		entityManager.persist(post);
		entityManager.flush();

		// when
		Optional<Post> found = postRepo.findById(1L);

		// then
		assertThat(found.isPresent());
		assertThat(found.get().getDate()).isEqualTo(post.getDate());
		assertThat(found.get().getDateGmt()).isEqualTo(post.getDateGmt());
		assertThat(found.get().getId()).isEqualTo(post.getId());
		assertThat(found.get().getIsFavourite()).isEqualTo(post.getIsFavourite());
		assertThat(found.get().getIsRead()).isEqualTo(post.getIsRead());
		assertThat(found.get().getIsReblog()).isEqualTo(post.getIsReblog());
		assertThat(found.get().getReblogKey()).isEqualTo(post.getReblogKey());
		assertThat(found.get().getSlug()).isEqualTo(post.getSlug());
		assertThat(found.get().getTags()).isEqualTo(post.getTags());
		assertThat(found.get().getType()).isEqualTo(post.getType());
		assertThat(found.get().getUrl()).isEqualTo(post.getUrl());
		assertThat(found.get().getUrlWithSlug()).isEqualTo(post.getUrlWithSlug());
	}

	/**
	 * Verifies that posts can be properly created in the DB via the Repo
	 * implementation
	 */
	@Test
	public void createPost() {
		Post post = new Post();
		post.setId(2L);
		post.setDate("Jan 1, 2019");

		Post returnPost = postRepo.save(post);

		assertThat(returnPost.getId()).isEqualTo(post.getId());
		assertThat(returnPost.getDate()).isEqualTo(post.getDate());
	}

	/**
	 * Verifies that the repo implementation's <code>findAll()</code> functions
	 * correctly
	 */
	@Test
	public void findAllPosts() {
		Post post1 = new Post();
		post1.setId(1L);
		post1.setDate("Jan 1, 2019");
		entityManager.persist(post1);
		Post post2 = new Post();
		post2.setId(2L);
		post2.setDate("Jan 2, 2019");
		entityManager.persist(post2);
		entityManager.flush();

		List<Post> returnedPosts = postRepo.findAll();

		assertThat(returnedPosts.size()).isEqualTo(2);
	}

	/**
	 * Tests that posts can be properly retrieved by type
	 */
	@Test
	public void findPostsByType() {
		Post post1 = new Post();
		post1.setId(1L);
		post1.setType("answer");
		post1.setDate("Jan 1, 2019");
		post1.setTumblelog("blog");
		entityManager.persist(post1);
		Post post2 = new Post();
		post2.setId(2L);
		post2.setType("answer");
		post2.setDate("Jan 2, 2019");
		post2.setTumblelog("blog");
		entityManager.persist(post2);
		Post post3 = new Post();
		post3.setId(3L);
		post3.setType("link");
		post3.setDate("Jan 3, 2019");
		post3.setTumblelog("blog");
		entityManager.persist(post3);
		entityManager.flush();

		List<Post> returnedPosts = postRepo.findAll();
		assertThat(returnedPosts.size()).isEqualTo(3);

		List<Post> answerPosts = postRepo.findByTumblelogAndType("blog", "answer");
		assertThat(answerPosts).isNotNull();
		assertThat(answerPosts.size()).isEqualTo(2);
		assertThat(answerPosts.get(0).getId()).isEqualTo(1L);
		assertThat(answerPosts.get(0).getDate()).isEqualTo("Jan 1, 2019");
		assertThat(answerPosts.get(1).getId()).isEqualTo(2L);
		assertThat(answerPosts.get(1).getDate()).isEqualTo("Jan 2, 2019");

		List<Post> linkPosts = postRepo.findByTumblelogAndType("blog", "link");
		assertThat(linkPosts).isNotNull();
		assertThat(linkPosts.size()).isEqualTo(1);
		assertThat(linkPosts.get(0).getId()).isEqualTo(3L);
		assertThat(linkPosts.get(0).getDate()).isEqualTo("Jan 3, 2019");
	}

	/**
	 * Verifies that the post repo's "delete all" functionality works
	 */
	@Test
	public void deleteAllPosts() {
		Post post1 = new Post();
		post1.setId(1L);
		post1.setDate("Jan 1, 2019");
		entityManager.persist(post1);
		Post post2 = new Post();
		post2.setId(2L);
		post2.setDate("Jan 2, 2019");
		entityManager.persist(post2);
		entityManager.flush();

		postRepo.deleteAll();

		List<Post> returnedPosts = postRepo.findAll();

		assertThat(returnedPosts.size()).isEqualTo(0);
	}

	/**
	 * Verifies that the Hashtag repo's "delete all" functionality works. Didn't
	 * bother creating a whole new class just for this one test.
	 */
	@Test
	public void deleteAllHashtags() {
		Hashtag tag1 = new Hashtag("tag1", 1, "blog");
		entityManager.persist(tag1);
		Hashtag tag2 = new Hashtag("tag2", 2, "blog");
		entityManager.persist(tag2);
		entityManager.flush();

		hashtagRepo.deleteByBlog("blog");

		List<Hashtag> hashtags = hashtagRepo.findByBlog("blog");

		assertThat(hashtags.size()).isEqualTo(0);
	}

}

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

import com.tiyb.tev.repository.PostRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PostRepositoryUnitTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private PostRepository postRepo;
	
	@Test
	public void findPost() {
		// given
		Post post = new Post();
		post.setDate("Jan 1, 2019");
		post.setDateGmt("123456");
		post.setId((long)1);
		post.setIsFavourite(false);
		post.setIsRead(false);
		post.setIsReblog(true);
		post.setReblogKey("abc");
		post.setSlug("slug");
		post.setTags("tag 1, tag 2");
		post.setType((long)1);
		post.setUrl("http://url.com");
		post.setUrlWithSlug("http://url.com/slug");
		
		entityManager.persist(post);
		entityManager.flush();
		
		//when
		Optional<Post> found = postRepo.findById((long)1);
		
		//then
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
	
	@Test
	public void createPost() {
		Post post = new Post();
		post.setId((long)2);
		post.setDate("Jan 1, 2019");
		
		Post returnPost = postRepo.save(post);
		
		assertThat(returnPost.getId()).isEqualTo(post.getId());
		assertThat(returnPost.getDate()).isEqualTo(post.getDate());
	}
	
	@Test
	public void findAllPosts() {
		Post post1 = new Post();
		post1.setId((long) 1);
		post1.setDate("Jan 1, 2019");
		entityManager.persist(post1);
		Post post2 = new Post();
		post2.setId((long) 2);
		post2.setDate("Jan 2, 2019");
		entityManager.persist(post2);
		entityManager.flush();
		
		List<Post> returnedPosts = postRepo.findAll();
		
		assertThat(returnedPosts.size()).isEqualTo(2);
	}
	
	@Test
	public void deleteAllPosts() {
		Post post1 = new Post();
		post1.setId((long) 1);
		post1.setDate("Jan 1, 2019");
		entityManager.persist(post1);
		Post post2 = new Post();
		post2.setId((long) 2);
		post2.setDate("Jan 2, 2019");
		entityManager.persist(post2);
		entityManager.flush();
		
		postRepo.deleteAll();
		
		List<Post> returnedPosts = postRepo.findAll();
		
		assertThat(returnedPosts.size()).isEqualTo(0);
	}
	
}

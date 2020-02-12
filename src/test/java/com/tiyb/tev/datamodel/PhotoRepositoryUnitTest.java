package com.tiyb.tev.datamodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tiyb.tev.repository.PhotoRepository;

/**
 * Unit Tests for working with the Photo Repo. Only methods with some logic
 * tested (i.e. not testing Spring Boot)
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class PhotoRepositoryUnitTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private PhotoRepository photoRepo;

	/**
	 * Verifies that photos can be retrieved via Post ID
	 */
	@Test
	public void testFindByPostID() {
		Photo photo1 = new Photo();
		photo1.setPostId(1L);
		photo1.setCaption("Photo 1");
		photo1.setOffset("o2");
		entityManager.persist(photo1);
		Photo photo2 = new Photo();
		photo2.setPostId(1L);
		photo2.setCaption("PHoto 2");
		photo2.setOffset("o1");
		entityManager.persist(photo2);
		entityManager.flush();

		List<Photo> returnedPhotos = photoRepo.findByPostIdOrderByOffset(1L);

		assertThat(returnedPhotos.size()).isEqualTo(2);
		assertThat(returnedPhotos.get(0).getCaption()).isEqualTo(photo2.getCaption());
		assertThat(returnedPhotos.get(1).getCaption()).isEqualTo(photo1.getCaption());
		assertThat(returnedPhotos.get(0).getPostId()).isEqualTo(1L);
		assertThat(returnedPhotos.get(1).getPostId()).isEqualTo(1L);
	}

}

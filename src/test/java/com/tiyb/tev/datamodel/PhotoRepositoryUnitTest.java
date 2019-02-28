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

@RunWith(SpringRunner.class)
@DataJpaTest
public class PhotoRepositoryUnitTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private PhotoRepository photoRepo;
	
	@Test
	public void testFindByPostID() {
		Photo photo1 = new Photo();
		photo1.setPostId((long) 1);
		photo1.setCaption("Photo 1");
		photo1.setOffset("o2");
		entityManager.persist(photo1);
		Photo photo2 = new Photo();
		photo2.setPostId((long) 1);
		photo2.setCaption("PHoto 2");
		photo2.setOffset("o1");
		entityManager.persist(photo2);
		entityManager.flush();
		
		List<Photo> returnedPhotos = photoRepo.findByPostIdOrderByOffset((long) 1);
		
		assertThat(returnedPhotos.size()).isEqualTo(2);
		assertThat(returnedPhotos.get(0).getCaption()).isEqualTo(photo2.getCaption());
		assertThat(returnedPhotos.get(1).getCaption()).isEqualTo(photo1.getCaption());
		assertThat(returnedPhotos.get(0).getPostId()).isEqualTo((long)1);
		assertThat(returnedPhotos.get(1).getPostId()).isEqualTo((long)1);
	}
	
}

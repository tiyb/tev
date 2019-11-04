package com.tiyb.tev;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>
 * This is the main class for the <b>Tumblr Export Viewer (TEV)</b> application.
 * As the name implies, the intent is to be able to read in Tumblr export files
 * and provide a UI for viewing posts and conversations from that export. As an
 * extra feature, the application will allow posts to be marked as "read" as
 * well as marked as "favourites," for use cases where the user wants to read
 * through a blog and keep track of what has been read and what hasn't.
 * </p>
 * 
 * <p>
 * There are two main components to the application:
 * </p>
 * 
 * <ol>
 * <li>The RESTful API, exposing all of the data that has been imported
 * (controlled by {@link com.tiyb.tev.controller.TEVPostRestController
 * TEVPostRestController}, {@link com.tiyb.tev.controller.TEVConvoRestController
 * TEVConvoRestController}, and
 * {@link com.tiyb.tev.controller.TEVMetadataRestController
 * TEVMetadataRestController})</li>
 * <li>The jQuery-based HTML interface (controlled by
 * {@link com.tiyb.tev.controller.TEVUIController TEVUIController}})</li>
 * </ol>
 * 
 * <p>
 * The application leverages an <b>HSQLDB</b> database for storage. By default,
 * Spring Boot leverages HSQLDB in an in-memory format (and doesn't allow this
 * to be overridden, even when the appropriate properties are set in
 * <code>application.properties</code>, but TEV needs it to be persisted. For
 * this reason, the application is configured to ignore auto-configuration for
 * <code>DataSourceAutoConfiguration</code>, and instead supplies the data
 * source in a hard-coded fashion.
 * </p>
 * 
 * @author tiyb
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class TevApplication {

	@Value("${spring.datasource.username}")
	private String dsUserName;

	@Value("${spring.datasource.password}")
	private String dsPassword;

	@Value("${spring.datasource.url}")
	private String dsUrl;

	@Value("${spring.datasource.driver-class-name}")
	private String dsDriverClassName;

	/**
	 * The main function for starting the application, called by Spring Boot at
	 * boot-up. No custom implementation, the standard implementation for Spring
	 * Boot is used.
	 * 
	 * @param args Command-line arguments, if any. (TEV doesn't expect/use any.)
	 */
	public static void main(String[] args) {
		SpringApplication.run(TevApplication.class, args);
	}

	/**
	 * Returns the {@link javax.sql.DataSource DataSource} bean for working with the
	 * database. This is the only way the HSQL data source can be forced to use a
	 * file-based database, instead of in-memory.
	 * 
	 * @return Standard Java {@link javax.sql.DataSource DataSource}
	 */
	@Bean
	public DataSource primaryDataSource() {
		return DataSourceBuilder.create().username(this.dsUserName).password(this.dsPassword).url(this.dsUrl)
				.driverClassName(this.dsDriverClassName).build();
	}

	/**
	 * Used for custom DB scripts that need to be executed by admin tools
	 * 
	 * @param dataSource The DS used by the application
	 * @return a {@link org.springframework.jdbc.core.JdbcTemplate JdbcTemplate} for
	 *         this application/data source
	 */
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

}

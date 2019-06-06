package com.tiyb.tev;

import javax.sql.DataSource;

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
 * As the name implies, the intent is to be able to read in a Tumblr export XML
 * document and provide a view of the posts within that XML. As an extra
 * feature, the application will allow posts to be marked as "read," for use
 * cases where the user wants to read through a blog and keep track of what has
 * been read and what hasn't.
 * </p>
 * 
 * <p>
 * There are two main components to the application:
 * </p>
 * 
 * <ol>
 * <li>The RESTful API, exposing all of the data that has been imported
 * (controlled by {@link com.tiyb.tev.controller.TEVRestController}}</li>
 * <li>The jQuery-based HTML interface (controlled by
 * {@link com.tiyb.tev.controller.TEVUIController}})</li>
 * </ol>
 * 
 * <p>
 * The application leverages an HSQLDB database for storage. By default, Spring
 * Boot leverages HSQLDB in an in-memory format (and doesn't allow this to be
 * overridden, even when the appropriate properteis are set in
 * <code>application.properties</code>, but TEV needs it to be persisted. For
 * this reason, the application is configured to ignore auto-configuration for
 * <code>DataSourceAutoConfiguration</code>, and instead supplies the data
 * source in a hard-coded fashion.
 * </p>
 * 
 * @author tiyb
 * @apiviz.landmark
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class TevApplication {

	/**
	 * The main function for starting the application, called by Spring Boot at
	 * boot-up.
	 * 
	 * @param args Command-line arguments, if any. (TEV doesn't expect/use any.)
	 */
	public static void main(String[] args) {
		SpringApplication.run(TevApplication.class, args);
	}

	/**
	 * Used for creation of a <code>DataSource</code> for the database, since this
	 * is the only way the HSQL datasource can be forced to use a file-based
	 * database, instead of in-memory.
	 * 
	 * @return Standard Java <code>DataSource</code>
	 */
	@Bean
	public DataSource primaryDataSource() {
		return DataSourceBuilder.create().username("sa").password("").url("jdbc:hsqldb:file:hsql/tev.db")
				.driverClassName("org.hsqldb.jdbc.JDBCDriver").build();
	}
	
	/**
	 * Used for custom DB scripts that need to be executed by admin tools
	 * 
	 * @param dataSource The DS used by the application
	 * @return a <code>JdbcTemplate</code> for this application/data source
	 */
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

}

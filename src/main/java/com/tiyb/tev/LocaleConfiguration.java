package com.tiyb.tev;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Configuration class, used for internationalization (i18n) of the TEV
 * application.
 * 
 * @author tiyb
 *
 */
@Configuration
@PropertySource("classpath:messages.properties")
public class LocaleConfiguration implements WebMvcConfigurer {

	/**
	 * @return Default locale set by the user
	 */
	@Bean(name = "localeResolver")
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.US);
		return slr;
	}

	/**
	 * an interceptor bean that will switch to a new locale based on the value of
	 * the lang parameter appended to a request:
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang"); 
		registry.addInterceptor(lci);
	}
}

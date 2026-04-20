package com.tamtam.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Larry
 */
@EntityScan("com.tamtam.core")
@EnableJpaRepositories("com.tamtam.core")
@EnableJpaAuditing
@Configuration
public class CoreConfig {

}

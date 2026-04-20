package com.tamtam.api.config;


import com.tamtam.api.global.config.SpringSecurityConfig;
import com.tamtam.api.global.config.UnauthorizedHandler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({SpringSecurityConfig.class})
@ComponentScan(basePackageClasses = UnauthorizedHandler.class)
public class TestSecurityConfig {


}

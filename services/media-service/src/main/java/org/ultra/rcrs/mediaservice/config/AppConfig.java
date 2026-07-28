package org.ultra.rcrs.mediaservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.ultra.rcrs.utils.ImageConfigBase;

@Configuration
@EnableConfigurationProperties(MediaConfigurationProperties.class)
@Import(ImageConfigBase.class)
public class AppConfig {

}

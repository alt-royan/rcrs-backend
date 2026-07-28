package org.ultra.rcrs.metadata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.ultra.rcrs.utils.ImageConfigBase;

@Configuration
@Import(ImageConfigBase.class)
public class AppConfig {

}

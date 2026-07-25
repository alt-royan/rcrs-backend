package org.ultra.rcrs.workflow.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.okhttp.OkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Objects;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 3000, 3);
    }

    @Bean
    public RequestInterceptor requestInterceptor(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        return requestTemplate -> {
            var accessToken = getAccessToken(oAuth2AuthorizedClientManager);
            requestTemplate.header("Authorization", "Bearer ${accessToken?.tokenValue}");
            log.info(accessToken.getTokenValue());
        };
    }

    private OAuth2AccessToken getAccessToken(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        var request = OAuth2AuthorizeRequest
                .withClientRegistrationId("metadata-write-service")
                .principal("principal-name")
                .build();
        return Objects.requireNonNull(oAuth2AuthorizedClientManager.authorize(request)).getAccessToken();
    }
}

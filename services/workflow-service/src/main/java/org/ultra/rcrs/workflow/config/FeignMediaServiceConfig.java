package org.ultra.rcrs.workflow.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Objects;

@Slf4j
@Configuration
public class FeignMediaServiceConfig {

    @Bean("requestInterceptorMedia")
    public RequestInterceptor requestInterceptorMedia(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        return requestTemplate -> {
            var accessToken = getAccessToken(oAuth2AuthorizedClientManager);
            requestTemplate.header("Authorization", "Bearer ${accessToken?.tokenValue}");
            log.info(accessToken.getTokenValue());
        };
    }

    private OAuth2AccessToken getAccessToken(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        var request = OAuth2AuthorizeRequest
                .withClientRegistrationId("media-service")
                .principal("principal-name")
                .build();
        return Objects.requireNonNull(oAuth2AuthorizedClientManager.authorize(request)).getAccessToken();
    }
}

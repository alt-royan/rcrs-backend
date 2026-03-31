package org.ultra.rcrs.catalogservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.EnableReactiveCassandraAuditing;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

@Configuration
@EnableReactiveCassandraAuditing
@EnableReactiveCassandraRepositories()
public class CassandraConfig {

/*    @Bean
    public ReactiveAuditorAware<AuditableUser> myAuditorProvider() {
        return new AuditorAwareImpl();
    }*/
}

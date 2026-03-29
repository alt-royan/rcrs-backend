package org.ultra.rcrs.catalogservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.ultra.rcrs.utils.Base62Utils;

import java.util.UUID;

class CatalogServiceApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(Base62Utils.encode(UUID.randomUUID()));
    }

}

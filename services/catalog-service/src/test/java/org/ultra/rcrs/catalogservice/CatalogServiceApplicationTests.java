package org.ultra.rcrs.catalogservice;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

class CatalogServiceApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(Url62.encode(UUID.fromString("576b37f5-a599-48fd-9c03-50e3d328586b")));
    }

}

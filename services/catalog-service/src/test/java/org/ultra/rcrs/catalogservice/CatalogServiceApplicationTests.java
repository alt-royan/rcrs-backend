package org.ultra.rcrs.catalogservice;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

class CatalogServiceApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(Url62.encode(UUID.fromString("44260878-e50b-42fd-ac1f-31162e9ec505")));
    }

}

package org.ultra.rcrs.searchservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.ultra.rcrs.searchservice.config.ElasticsearchClientConfig,org.ultra.rcrs.searchservice.config.KafkaConfig",
        "spring.cloud.discovery.client.enabled=false"
})
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.12.0")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false");

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    private static RestClient restClient;

    @Autowired
    protected MockMvc mockMvc;

    protected static RestClient getRestClient() {
        if (restClient == null) {
            restClient = RestClient.builder().baseUrl(elasticsearch.getHttpHostAddress()).build();
        }
        return restClient;
    }

    private void indexDoc(String index, String id, Map<String, Object> body) {
        try {
            Request request = new Request("PUT", "/" + index + "/_doc/" + id);
            request.setJsonEntity(MAPPER.writeValueAsString(body));
            getRestClient().
        } catch (Exception e) {
            throw new RuntimeException("Failed to index doc into " + index, e);
        }
    }

    protected void indexArtistPublicDoc(String id, String name, List<String> tags,
                                         String availability, List<Map<String, String>> albums,
                                         List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("name", name);
        body.put("tags", tags);
        body.put("availability", availability);
        if (albums != null) body.put("albums", albums);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("artists-public", id, body);
    }

    protected void indexAlbumPublicDoc(String id, String title, String year, String availability,
                                        List<Map<String, String>> artists,
                                        List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("year", year);
        body.put("availability", availability);
        if (artists != null) body.put("artists", artists);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("albums-public", id, body);
    }

    protected void indexTrackPublicDoc(String id, String title, String availability,
                                        List<Map<String, String>> artists,
                                        Map<String, String> album) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("availability", availability);
        if (artists != null) body.put("artists", artists);
        if (album != null) body.put("album", album);
        indexDoc("tracks-public", id, body);
    }

    protected void indexArtistAdminDoc(String id, String name, List<String> tags,
                                        String availability, String lifecycleStatus,
                                        List<Map<String, String>> albums,
                                        List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("name", name);
        body.put("tags", tags);
        body.put("availability", availability);
        if (albums != null) body.put("albums", albums);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("artists-admin", id, body);
    }

    protected void indexAlbumAdminDoc(String id, String title, String year, String availability,
                                       String lifecycleStatus,
                                       List<Map<String, String>> artists,
                                       List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("year", year);
        body.put("availability", availability);
        body.put("lifecycleStatus", lifecycleStatus);
        if (artists != null) body.put("artists", artists);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("albums-admin", id, body);
    }

    protected void indexTrackAdminDoc(String id, String title, String availability, String lifecycleStatus,
                                       List<Map<String, String>> artists,
                                       Map<String, String> album) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("availability", availability);
        body.put("lifecycleStatus", lifecycleStatus);
        if (artists != null) body.put("artists", artists);
        if (album != null) body.put("album", album);
        indexDoc("tracks-admin", id, body);
    }

    protected void refreshAllIndices() {
        try {
            Request request = new Request("POST", "/_refresh");
            getRestClient().perform(request);
            Thread.sleep(1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static Map<String, String> nested(String id, String name) {
        return Map.of("id", id, "name", name);
    }

    protected static Map<String, String> nestedAlbum(String id, String title) {
        return Map.of("id", id, "title", title);
    }
}

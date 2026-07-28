package org.ultra.rcrs.libraryservice.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.mongo.ListenEvent;

/**
 * Batch ingest and the keyset-paginated timeline are driven through
 * {@code MongoTemplate} in {@code ListenService}; this interface exists for the
 * simple cases and for tests.
 */
@Repository
public interface ListenEventRepository extends MongoRepository<ListenEvent, String> {

    long countByUserId(String userId);

    void deleteAllByUserId(String userId);
}

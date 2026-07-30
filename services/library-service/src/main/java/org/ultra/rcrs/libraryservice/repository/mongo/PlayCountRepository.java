package org.ultra.rcrs.libraryservice.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.mongo.PlayCount;
import org.ultra.rcrs.libraryservice.model.mongo.PlayCountId;

@Repository
public interface PlayCountRepository extends MongoRepository<PlayCount, PlayCountId> {
}

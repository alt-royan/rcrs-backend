package org.ultra.rcrs.libraryservice.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.mongo.RecentlyPlayed;
import org.ultra.rcrs.libraryservice.model.mongo.RecentlyPlayedId;

@Repository
public interface RecentlyPlayedRepository extends MongoRepository<RecentlyPlayed, RecentlyPlayedId> {
}

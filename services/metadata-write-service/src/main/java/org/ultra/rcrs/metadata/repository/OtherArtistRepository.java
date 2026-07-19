package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.OtherArtist;

import java.util.UUID;

@Repository
public interface OtherArtistRepository extends JpaRepository<OtherArtist, UUID> {

}

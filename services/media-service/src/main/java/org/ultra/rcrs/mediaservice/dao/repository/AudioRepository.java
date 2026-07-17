package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.mediaservice.dao.model.Audio;

import java.util.UUID;

@Repository
public interface AudioRepository extends JpaRepository<Audio, UUID> {

}

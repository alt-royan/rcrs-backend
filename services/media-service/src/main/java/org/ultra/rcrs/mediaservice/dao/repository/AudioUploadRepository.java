package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;

@Repository
public interface AudioUploadRepository extends JpaRepository<AudioUpload, String> {

}

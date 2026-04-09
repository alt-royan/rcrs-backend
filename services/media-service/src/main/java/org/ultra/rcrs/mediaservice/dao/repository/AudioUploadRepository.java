package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;

@Repository
public interface AudioUploadRepository extends JpaRepository<AudioUpload, String> {

    @Modifying
    @Query("UPDATE AudioUpload SET status=:status WHERE uid = :uid")
    void updateStatusByUid(FileStatus status, String uid);
}

package org.ultra.rcrs.libraryservice.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.jpa.Like;
import org.ultra.rcrs.libraryservice.model.jpa.LikePK;

import java.util.Collection;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, LikePK> {

    List<Like> findByUserIdAndEntityType(String userId, LibraryEntityType entityType, Pageable pageable);

    long countByUserIdAndEntityType(String userId, LibraryEntityType entityType);

    /**
     * Backs the batch "which of these are liked?" check. The caller groups the
     * requested items by type so a screen full of mixed entities costs one query
     * per distinct type — at most four — rather than one per item.
     */
    @Query("SELECT l.entityId FROM Like l " +
            "WHERE l.userId = :userId AND l.entityType = :entityType AND l.entityId IN :entityIds")
    List<String> findLikedEntityIds(@Param("userId") String userId,
                                    @Param("entityType") LibraryEntityType entityType,
                                    @Param("entityIds") Collection<String> entityIds);

    @Query("SELECT l.entityType, count(l) FROM Like l WHERE l.userId = :userId GROUP BY l.entityType")
    List<Object[]> countByTypeForUser(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}

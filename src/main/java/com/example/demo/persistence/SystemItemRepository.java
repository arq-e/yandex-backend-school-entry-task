package com.example.demo.persistence;

import com.example.demo.model.SystemItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SystemItemRepository extends JpaRepository<SystemItem, String> {

    @Query(value = "SELECT * from main where parent_id = :id", nativeQuery = true)
    List<SystemItem> findByParentId(String id);

    @Query(value = "SELECT * FROM main WHERE (date <= :datetime AND date >= :previousDay)", nativeQuery = true)
    List<SystemItem> findRecent(@Param("datetime") OffsetDateTime datetime, @Param("previousDay") OffsetDateTime previousDay);
}

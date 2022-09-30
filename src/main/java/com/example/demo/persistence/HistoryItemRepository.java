package com.example.demo.persistence;

import com.example.demo.model.SystemItemHistoryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface HistoryItemRepository extends JpaRepository<SystemItemHistoryNote, String> {


    @Query(value = "select * from history where id = :id and (date >= :startDate and date < :endDate)", nativeQuery = true)
    List<SystemItemHistoryNote> findHistoryByItemId(String id, @Param("startDate") OffsetDateTime startDate,
                                                    @Param("endDate") OffsetDateTime endDate);

    @Query(value = "select * from history where id = :id and date >= :startDate ", nativeQuery = true)
    List<SystemItemHistoryNote> findHistoryByItemId(String id, @Param("startDate")OffsetDateTime startDate);
}

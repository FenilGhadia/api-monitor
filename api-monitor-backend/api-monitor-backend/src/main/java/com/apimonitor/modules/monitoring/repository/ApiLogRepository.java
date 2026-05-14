package com.apimonitor.modules.monitoring.repository;

import com.apimonitor.modules.monitoring.entity.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiLogRepository
        extends JpaRepository<ApiLog, Long>, JpaSpecificationExecutor<ApiLog> {


    @Query("""
    SELECT l FROM ApiLog l
    WHERE (:endpoint    IS NULL OR LOWER(l.endpoint) LIKE :endpoint)
      AND (:httpMethod  IS NULL OR l.httpMethod = :httpMethod)
      AND (:statusCode  IS NULL OR l.statusCode = :statusCode)
      AND (:fromDate    IS NULL OR l.timestamp >= :fromDate)
      AND (:toDate      IS NULL OR l.timestamp <= :toDate)
      AND (:userId      IS NULL OR l.userId = :userId)
      AND (:slowOnly    IS NULL OR l.slow = TRUE)
      AND (:errorOnly   IS NULL OR l.error = TRUE)
""")
    Page<ApiLog> findWithFilters(
            @Param("endpoint")   String endpoint,
            @Param("httpMethod") String httpMethod,
            @Param("statusCode") Integer statusCode,
            @Param("fromDate")   LocalDateTime fromDate,
            @Param("toDate")     LocalDateTime toDate,
            @Param("userId")     Long userId,
            @Param("slowOnly")   Boolean slowOnly,
            @Param("errorOnly")  Boolean errorOnly,
            Pageable pageable
    );


    @Query("""
    SELECT COUNT(l),
           COALESCE(AVG(l.responseTimeMs), 0.0),
           COALESCE(SUM(CASE WHEN l.error IS TRUE THEN 1 ELSE 0 END), 0),
           COALESCE(SUM(CASE WHEN l.slow  IS TRUE THEN 1 ELSE 0 END), 0)
    FROM ApiLog l
    WHERE l.timestamp >= :from
      AND l.timestamp <= :to
""")
    Object[] getSummaryStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );


    @Query("""
        SELECT COUNT(l)
        FROM ApiLog l
        WHERE l.timestamp >= :from AND l.timestamp <= :to
    """)
    long countInRange(@Param("from") LocalDateTime from,
                      @Param("to")   LocalDateTime to);

    @Query("""
        SELECT COALESCE(AVG(l.responseTimeMs), 0)
        FROM ApiLog l
        WHERE l.timestamp >= :from AND l.timestamp <= :to
    """)
    Double avgResponseTimeInRange(@Param("from") LocalDateTime from,
                                  @Param("to")   LocalDateTime to);

    @Query("""
        SELECT COUNT(l)
        FROM ApiLog l
        WHERE l.error = true
          AND l.timestamp >= :from AND l.timestamp <= :to
    """)
    long countErrorsInRange(@Param("from") LocalDateTime from,
                            @Param("to")   LocalDateTime to);

    @Query("""
        SELECT COUNT(l)
        FROM ApiLog l
        WHERE l.slow = true
          AND l.timestamp >= :from AND l.timestamp <= :to
    """)
    long countSlowInRange(@Param("from") LocalDateTime from,
                          @Param("to")   LocalDateTime to);


    @Query("""
        SELECT l.endpoint, COUNT(l)
        FROM ApiLog l
        WHERE l.timestamp >= :from AND l.timestamp <= :to
        GROUP BY l.endpoint
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findTopEndpoints(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT l.endpoint, AVG(l.responseTimeMs), COUNT(l)
        FROM ApiLog l
        WHERE l.timestamp >= :from AND l.timestamp <= :to
        GROUP BY l.endpoint
    """)
    List<Object[]> findSlowestEndpoints(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT l.endpoint,
               COUNT(l),
               COALESCE(AVG(l.responseTimeMs), 0.0),
               COALESCE(SUM(CASE WHEN l.error = true THEN 1 ELSE 0 END), 0),
               COALESCE(SUM(CASE WHEN l.slow  = true THEN 1 ELSE 0 END), 0)
        FROM ApiLog l
        WHERE l.timestamp >= :from AND l.timestamp <= :to
        GROUP BY l.endpoint
        ORDER BY COUNT(l) DESC
    """)
    List<Object[]> findEndpointStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );


    @Query(value = """
    SELECT DATE_TRUNC('hour', l.timestamp) AS bucket,
           COUNT(*) AS request_count,
           AVG(l.response_time_ms) AS avg_response_ms,
           COALESCE(SUM(CASE WHEN l.is_error THEN 1 ELSE 0 END), 0) AS error_count
    FROM api_logs l
    WHERE l.timestamp >= :from AND l.timestamp <= :to
    GROUP BY bucket
    ORDER BY bucket ASC
""", nativeQuery = true)
    List<Object[]> findHourlyStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    @Query(value = """
    SELECT DATE_TRUNC('day', l.timestamp) AS bucket,
           COUNT(*) AS request_count,
           AVG(l.response_time_ms) AS avg_response_ms,
           COALESCE(SUM(CASE WHEN l.is_error THEN 1 ELSE 0 END), 0) AS error_count
    FROM api_logs l
    WHERE l.timestamp >= :from AND l.timestamp <= :to
    GROUP BY bucket
    ORDER BY bucket ASC
""", nativeQuery = true)
    List<Object[]> findDailyStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );


    @Query("""
        SELECT l.statusCode, COUNT(l)
        FROM ApiLog l
        WHERE l.timestamp >= :from AND l.timestamp <= :to
        GROUP BY l.statusCode
        ORDER BY l.statusCode ASC
    """)
    List<Object[]> findStatusCodeDistribution(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );
}
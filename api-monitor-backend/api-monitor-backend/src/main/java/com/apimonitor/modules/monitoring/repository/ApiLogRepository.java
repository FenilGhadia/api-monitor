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

/**
 * Data access layer for API logs.
 *
 * Implements JpaSpecificationExecutor to support dynamic filtering
 * (status code range, endpoint contains, date range) without
 * writing a separate repository method for every combination.
 *
 * All analytics queries use JPQL projections to avoid loading
 * full entities just for aggregate computation.
 */
@Repository
public interface ApiLogRepository
        extends JpaRepository<ApiLog, Long>, JpaSpecificationExecutor<ApiLog> {

    // ── Filtering ─────────────────────────────────────────────────────────

    /**
     * Flexible log search with optional filters.
     * All parameters are nullable — null = "no filter applied".
     */
    // Fix #1: ORDER BY removed — Pageable owns sorting (avoids duplicate ORDER BY clause
    //         when controller passes @PageableDefault(sort="timestamp")).
    // Fix #2: boolean → Boolean so null can represent "filter off" explicitly and
    //         JPQL comparison against IS NULL/false is unambiguous across JPA providers.
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

    // ── Summary analytics ─────────────────────────────────────────────────

    // Fix #2: Single combined query replaces 4 separate round-trips.
    // Row shape: [totalRequests(Long), avgResponseMs(Double|null), errorCount(Long), slowCount(Long)]
    @Query("""
        SELECT COUNT(l),
               COALESCE(AVG(l.responseTimeMs), 0.0),
               SUM(CASE WHEN l.error = true THEN 1 ELSE 0 END),
               SUM(CASE WHEN l.slow  = true THEN 1 ELSE 0 END)
        FROM ApiLog l
        WHERE l.timestamp BETWEEN :from AND :to
        """)
    Object[] getSummaryStats(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Total request count in a time window */
    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.timestamp BETWEEN :from AND :to")
    long countInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Average response time in a time window */
    @Query("SELECT COALESCE(AVG(l.responseTimeMs), 0) FROM ApiLog l WHERE l.timestamp BETWEEN :from AND :to")
    Double avgResponseTimeInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Count of error (4xx/5xx) requests in a time window */
    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.error = true AND l.timestamp BETWEEN :from AND :to")
    long countErrorsInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Count of slow requests in a time window */
    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.slow = true AND l.timestamp BETWEEN :from AND :to")
    long countSlowInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ── Endpoint analytics ────────────────────────────────────────────────

    /**
     * Most-used endpoints: returns [endpoint, count] ordered by count desc.
     * Projection: Object[] { endpoint(String), count(Long) }
     */
    @Query("""
        SELECT l.endpoint, COUNT(l) AS requestCount
        FROM ApiLog l
        WHERE l.timestamp BETWEEN :from AND :to
        GROUP BY l.endpoint
        ORDER BY requestCount DESC
        """)
    List<Object[]> findTopEndpoints(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    /**
     * Slowest endpoints by avg response time — Fix #3: ORDER BY removed.
     * Sort is expressed via the Pageable argument in AnalyticsService to avoid
     * the Hibernate 6 dual-sort conflict (hardcoded ORDER BY + Pageable sort).
     */
    @Query("""
        SELECT l.endpoint, AVG(l.responseTimeMs) AS avgTime, COUNT(l) AS requestCount
        FROM ApiLog l
        WHERE l.timestamp BETWEEN :from AND :to
        GROUP BY l.endpoint
        """)
    List<Object[]> findSlowestEndpoints(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    /**
     * Per-endpoint full stats — Fix #4: Pageable added to prevent unbounded
     * result sets on systems with many distinct endpoints. Caller passes
     * PageRequest.of(0, 200) as a hard cap.
     */
    @Query("""
        SELECT l.endpoint,
               COUNT(l)                                              AS totalRequests,
               AVG(l.responseTimeMs)                                AS avgResponseTime,
               SUM(CASE WHEN l.error  = true THEN 1 ELSE 0 END)    AS errorCount,
               SUM(CASE WHEN l.slow   = true THEN 1 ELSE 0 END)    AS slowCount
        FROM ApiLog l
        WHERE l.timestamp BETWEEN :from AND :to
        GROUP BY l.endpoint
        ORDER BY totalRequests DESC
        """)
    List<Object[]> findEndpointStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable              // Fix #4: was missing
    );

    // ── Time-series analytics ─────────────────────────────────────────────

    /**
     * Hourly bucketed request counts.
     * Returns [hour(LocalDateTime), count] for chart rendering.
     */
    @Query(value = """
        SELECT DATE_TRUNC('hour', l.timestamp)   AS bucket,
               COUNT(*)                          AS request_count,
               AVG(l.response_time_ms)           AS avg_response_ms,
               SUM(CASE WHEN l.is_error THEN 1 ELSE 0 END) AS error_count
        FROM api_logs l
        WHERE l.timestamp BETWEEN :from AND :to
        GROUP BY bucket
        ORDER BY bucket ASC
        """, nativeQuery = true)
    List<Object[]> findHourlyStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    /**
     * Daily bucketed request counts.
     */
    @Query(value = """
        SELECT DATE_TRUNC('day', l.timestamp)    AS bucket,
               COUNT(*)                          AS request_count,
               AVG(l.response_time_ms)           AS avg_response_ms,
               SUM(CASE WHEN l.is_error THEN 1 ELSE 0 END) AS error_count
        FROM api_logs l
        WHERE l.timestamp BETWEEN :from AND :to
        GROUP BY bucket
        ORDER BY bucket ASC
        """, nativeQuery = true)
    List<Object[]> findDailyStats(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    // ── Status code distribution ──────────────────────────────────────────

    /**
     * Returns [statusCode, count] for pie/bar chart rendering.
     */
    @Query("""
        SELECT l.statusCode, COUNT(l)
        FROM ApiLog l
        WHERE l.timestamp BETWEEN :from AND :to
        GROUP BY l.statusCode
        ORDER BY l.statusCode ASC
        """)
    List<Object[]> findStatusCodeDistribution(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );
}
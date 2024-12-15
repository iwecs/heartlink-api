package com.ss.heartlinkapi.linkmatch.repository;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchAnswerEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CoupleMatchAnswerRepository extends JpaRepository<LinkMatchAnswerEntity, Long> {

    // 매칭 성공 실패 여부 확인(오늘 날짜 기준 매칭 성공 시 1, 실패 시 0, 한 사용자만 등록했을 경우 2 반환)
    @Query("SELECT CASE WHEN COUNT(DISTINCT a.choice) = 1 AND COUNT(a.choice) > 1 THEN 1 " + // 두 사용자가 동일한 선택을 했을 경우
            "WHEN COUNT(DISTINCT a.choice) > 1 THEN 0 ELSE 2 END FROM LinkMatchAnswerEntity a " +
            "WHERE a.coupleId.coupleId = :coupleId AND DATE(a.createdAt) = CURRENT_DATE")
    int checkTodayMatching(@Param("coupleId") Long coupleId);

    // 매치 답변 내역 조회
    List<LinkMatchAnswerEntity> findByCoupleId(CoupleEntity coupleId);

    // 통계 - 성별 별 선택답변 조회 (id값으로 한 질문에 대해 조회)
    @Query(value = "SELECT m.link_match_id, g.gender, c.choice, " +
            "COALESCE(COUNT(l.id), 0) AS count " +
            "FROM (SELECT 'M' AS gender UNION SELECT 'F') AS g " +
            "CROSS JOIN (SELECT 0 AS choice UNION SELECT 1) AS c " +
            "CROSS JOIN match_answer m " +
            "LEFT JOIN users u ON u.id = m.users_id AND u.gender = g.gender " +
            "LEFT JOIN match_answer l " +
            "    ON l.link_match_id = m.link_match_id " +
            "    AND l.choice = c.choice " +
            "    AND l.users_id = u.id " +
            "GROUP BY m.link_match_id, g.gender, c.choice " +
            "having m.link_match_id = :matchId " +
            "ORDER BY m.link_match_id, g.gender, c.choice", nativeQuery = true)
    List<Object[]> matchCountGenderById(@Param("matchId") Long matchId);

    // 통계 - 오늘 매치 답변에 응답한 인원 수 커플 아이디로 그룹지어 조회하기
    @Query(value = "select count(*) from match_answer where created_at = :today group by couple_id", nativeQuery = true)
    List<Integer> todayTotalAnswerCountGroupByCoupleId(@Param("today") LocalDate today);

    // 통계 - 오늘 매치 답변에 매치성공한 커플 쌍의 수
    @Query(value = "SELECT COUNT(*) AS matching_couples " +
            "FROM ( " +
            "    SELECT couple_id " +
            "    FROM match_answer " +
            "    WHERE created_at = CURDATE() " +
            "    GROUP BY couple_id, choice " +
            "    HAVING COUNT(*) = 2 " +
            ") AS matched", nativeQuery = true)
    int todaySuccessMatchCount();

    // 통계 - 월별 모든 커플의 매치 성공 횟수
    @Query(value = "SELECT COUNT(*) FROM (" +
            "SELECT COUNT(*) AS count " +
            "FROM match_answer " +
            "WHERE created_at >= :startDate " +
            "AND created_at < :endDate " +
            "GROUP BY couple_id, link_match_id, choice " +
            "HAVING COUNT(*) > 1) AS monthMatchCount",
            nativeQuery = true)
    int monthSuccessMatchCount(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // 통계 - 월별 한 커플의 매치 성공 횟수
    @Query(value = "SELECT COUNT(*) AS matching_choice_count "+
            "FROM match_answer AS ma1 "+
            "JOIN match_answer AS ma2 ON ma1.couple_id = ma2.couple_id " +
            "WHERE ma1.link_match_id = ma2.link_match_id " +
            "  AND YEAR(ma1.created_at) = :year " +
            "  AND MONTH(ma1.created_at) = :month " +
            "  AND ma1.couple_id = :coupleId " +
            "  AND ma1.choice = ma2.choice " +
            "  AND ma1.id < ma2.id", nativeQuery = true)
    int monthSuccessMatchCountByCoupleId(@Param("coupleId") Long coupleId, @Param("year")int year, @Param("month")int month);

    // 통계 - 한 달동안 매치에 답변한 커플 쌍의 수(중복없이)
    @Query(value = "SELECT COUNT(DISTINCT couple_id) AS unique_couple_count " +
            "FROM match_answer " +
            "WHERE YEAR(created_at) = :year " +
            "  AND MONTH(created_at) = :month", nativeQuery = true)
    int attendMatchCoupleCount(@Param("year") int year, @Param("month") int month);

    void deleteAllByCoupleId(CoupleEntity couple);

    // 유저와 날짜에 맞는 객체가 있다면 조회 후 반환
    LinkMatchAnswerEntity findByUserIdAndCreatedAt(UserEntity user, LocalDate createdAt);

    // 유저아이디로 링크매치답변 조회
    List<LinkMatchAnswerEntity> findAllByUserId(UserEntity user);


}

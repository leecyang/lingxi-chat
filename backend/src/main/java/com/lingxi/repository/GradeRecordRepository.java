package com.lingxi.repository;

import com.lingxi.entity.GradeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 成绩记录数据访问接口
 */
@Repository
public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {

    /**
     * 根据用户ID查找成绩记录
     */
    List<GradeRecord> findByUser_IdOrderByExamDateDesc(Long userId);

    /**
     * 根据用户ID和科目查找成绩记录
     */
    List<GradeRecord> findByUser_IdAndSubjectOrderByExamDateDesc(Long userId, String subject);

    /**
     * 根据用户ID和成绩类型查找成绩记录
     */
    List<GradeRecord> findByUser_IdAndGradeTypeOrderByExamDateDesc(Long userId, GradeRecord.GradeType gradeType);

    /**
     * 根据科目查找成绩记录
     */
    List<GradeRecord> findBySubjectOrderByExamDateDesc(String subject);

    /**
     * 根据成绩类型查找成绩记录
     */
    List<GradeRecord> findByGradeTypeOrderByExamDateDesc(GradeRecord.GradeType gradeType);

    /**
     * 查找用户最近的成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.examDate >= :since ORDER BY gr.examDate DESC")
    List<GradeRecord> findRecentGradesByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 查找用户在特定时间范围内的成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.examDate BETWEEN :startDate AND :endDate ORDER BY gr.examDate ASC")
    List<GradeRecord> findByUser_IdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 查找用户特定科目在时间范围内的成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject AND gr.examDate BETWEEN :startDate AND :endDate ORDER BY gr.examDate ASC")
    List<GradeRecord> findByUser_IdAndSubjectAndDateRange(@Param("userId") Long userId, @Param("subject") String subject, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 查找用户最新的成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.examDate DESC")
    List<GradeRecord> findLatestGradeByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找用户特定科目的最新成绩
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject ORDER BY gr.examDate DESC")
    List<GradeRecord> findLatestGradeByUserAndSubject(@Param("userId") Long userId, @Param("subject") String subject, Pageable pageable);

    /**
     * 查找用户的最高分记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.score DESC")
    List<GradeRecord> findHighestScoresByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找用户特定科目的最高分
     */
    @Query("SELECT MAX(gr.score) FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject")
    Optional<Double> findMaxScoreByUserAndSubject(@Param("userId") Long userId, @Param("subject") String subject);

    /**
     * 查找用户特定科目的最低分
     */
    @Query("SELECT MIN(gr.score) FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject")
    Optional<Double> findMinScoreByUserAndSubject(@Param("userId") Long userId, @Param("subject") String subject);

    /**
     * 计算用户特定科目的平均分
     */
    @Query("SELECT AVG(gr.score) FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject")
    Optional<Double> findAverageScoreByUserAndSubject(@Param("userId") Long userId, @Param("subject") String subject);

    /**
     * 计算用户的总平均分
     */
    @Query("SELECT AVG(gr.score) FROM GradeRecord gr WHERE gr.user.id = :userId")
    Optional<Double> findAverageScoreByUser(@Param("userId") Long userId);

    /**
     * 查找分数在特定范围内的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.score BETWEEN :minScore AND :maxScore ORDER BY gr.examDate DESC")
    List<GradeRecord> findByUser_IdAndScoreRange(@Param("userId") Long userId, @Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * 查找有预测结果的成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.predictedNextScore IS NOT NULL ORDER BY gr.examDate DESC")
    List<GradeRecord> findRecordsWithPrediction(@Param("userId") Long userId);

    /**
     * 查找预测趋势为上升的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.trendDirection = 'IMPROVING' ORDER BY gr.examDate DESC")
    List<GradeRecord> findUpwardTrendRecords(@Param("userId") Long userId);

    /**
     * 查找预测趋势为下降的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.trendDirection = 'DECLINING' ORDER BY gr.examDate DESC")
    List<GradeRecord> findDownwardTrendRecords(@Param("userId") Long userId);

    /**
     * 统计用户各科目的记录数量
     */
    @Query("SELECT gr.subject, COUNT(gr) FROM GradeRecord gr WHERE gr.user.id = :userId GROUP BY gr.subject")
    List<Object[]> countRecordsBySubject(@Param("userId") Long userId);

    /**
     * 统计用户各成绩类型的记录数量
     */
    @Query("SELECT gr.gradeType, COUNT(gr) FROM GradeRecord gr WHERE gr.user.id = :userId GROUP BY gr.gradeType")
    List<Object[]> countRecordsByGradeType(@Param("userId") Long userId);

    /**
     * 查找班级排名前N的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.classRank <= :topN ORDER BY gr.classRank ASC")
    List<GradeRecord> findTopRankingRecords(@Param("userId") Long userId, @Param("topN") Integer topN);

    /**
     * 查找年级排名前N的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.gradeRank <= :topN ORDER BY gr.gradeRank ASC")
    List<GradeRecord> findTopGradeRankingRecords(@Param("userId") Long userId, @Param("topN") Integer topN);

    /**
     * 查找进步最大的记录 (暂时注释掉，因为实体中没有improvementPoints字段)
     */
    // @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.improvementPoints IS NOT NULL ORDER BY gr.improvementPoints DESC")
    // List<GradeRecord> findMostImprovedRecords(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找退步最大的记录 (暂时注释掉，因为实体中没有improvementPoints字段)
     */
    // @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.improvementPoints IS NOT NULL ORDER BY gr.improvementPoints ASC")
    // List<GradeRecord> findMostDeclinedRecords(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找特定考试的所有记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.examName = :examName ORDER BY gr.score DESC")
    List<GradeRecord> findByExamName(@Param("examName") String examName);

    /**
     * 查找特定学期的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.semester = :semester ORDER BY gr.examDate DESC")
    List<GradeRecord> findByUser_IdAndSemester(@Param("userId") Long userId, @Param("semester") String semester);

    /**
     * 查找特定学年的记录 (暂时注释掉，因为实体中没有academicYear字段)
     */
    // @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.academicYear = :academicYear ORDER BY gr.examDate DESC")
    // List<GradeRecord> findByUserIdAndAcademicYear(@Param("userId") Long userId, @Param("academicYear") String academicYear);

    /**
     * 统计今日新增成绩记录数
     */
    @Query("SELECT COUNT(gr) FROM GradeRecord gr WHERE gr.createdAt >= :startOfDay")
    long countTodayRecords(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 统计用户今日新增成绩记录数
     */
    @Query("SELECT COUNT(gr) FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.createdAt >= :startOfDay")
    long countTodayRecordsByUser(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 查找需要预测的记录（没有预测结果的）
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.predictedNextScore IS NULL ORDER BY gr.createdAt ASC")
    List<GradeRecord> findRecordsNeedingPrediction();

    /**
     * 查找预测准确度高的记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.predictionConfidence IS NOT NULL AND gr.predictionConfidence >= :minAccuracy ORDER BY gr.predictionConfidence DESC")
    List<GradeRecord> findHighAccuracyPredictions(@Param("minAccuracy") Double minAccuracy);

    /**
     * 删除指定时间之前的成绩记录
     */
    @Query("DELETE FROM GradeRecord gr WHERE gr.createdAt < :cutoffTime")
    int deleteOldGradeRecords(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 删除用户的成绩记录
     */
    @Query("DELETE FROM GradeRecord gr WHERE gr.user.id = :userId")
    int deleteGradeRecordsByUserId(@Param("userId") Long userId);

    /**
     * 查找用户科目成绩趋势
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject ORDER BY gr.examDate ASC")
    List<GradeRecord> findGradeTrendByUserAndSubject(@Param("userId") Long userId, @Param("subject") String subject);

    /**
     * 查找用户所有科目列表
     */
    @Query("SELECT DISTINCT gr.subject FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.subject")
    List<String> findSubjectsByUser(@Param("userId") Long userId);

    /**
     * 查找用户所有学期列表
     */
    @Query("SELECT DISTINCT gr.semester FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.semester")
    List<String> findSemestersByUser(@Param("userId") Long userId);

    /**
     * 查找用户所有学年列表 (暂时注释掉，因为实体中没有academicYear字段)
     */
    // @Query("SELECT DISTINCT gr.academicYear FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.academicYear")
    // List<String> findAcademicYearsByUser(@Param("userId") Long userId);

    // 分页查询方法
    Page<GradeRecord> findByUser_IdOrderByExamDateDesc(Long userId, Pageable pageable);
    Page<GradeRecord> findByUser_IdAndSubjectOrderByExamDateDesc(Long userId, String subject, Pageable pageable);
    Page<GradeRecord> findByUser_IdAndGradeTypeOrderByExamDateDesc(Long userId, GradeRecord.GradeType gradeType, Pageable pageable);
    
    // LocalDate 版本的日期范围查询
    Page<GradeRecord> findByUser_IdAndExamDateBetweenOrderByExamDateDesc(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    List<GradeRecord> findByUser_IdAndExamDateBetweenOrderByExamDateDesc(Long userId, LocalDate startDate, LocalDate endDate);
    List<GradeRecord> findByUser_IdAndSubjectAndExamDateBetweenOrderByExamDateAsc(Long userId, String subject, LocalDate startDate, LocalDate endDate);
    List<GradeRecord> findByUser_IdAndSubjectAndExamDateBetweenOrderByExamDateDesc(Long userId, String subject, LocalDate startDate, LocalDate endDate);
    
    // 考试名称相关查询
    List<GradeRecord> findByUser_IdAndSubjectAndExamNameOrderByExamDateDesc(Long userId, String subject, String examName);
    
    // 获取用户的科目和考试列表
    @Query("SELECT DISTINCT gr.subject FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.subject")
    List<String> findDistinctSubjectsByUser_Id(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT gr.examName FROM GradeRecord gr WHERE gr.user.id = :userId ORDER BY gr.examName")
    List<String> findDistinctExamNamesByUser_Id(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT gr.examName FROM GradeRecord gr WHERE gr.user.id = :userId AND gr.subject = :subject ORDER BY gr.examName")
    List<String> findDistinctExamNamesByUser_IdAndSubject(@Param("userId") Long userId, @Param("subject") String subject);
    
    // 删除方法
    void deleteByUser_IdAndSubject(Long userId, String subject);
    void deleteByUser_Id(Long userId);
}
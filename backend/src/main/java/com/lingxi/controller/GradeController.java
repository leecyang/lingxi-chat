package com.lingxi.controller;

import com.lingxi.entity.GradeRecord;
import com.lingxi.service.GradeService;
import com.lingxi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 成绩管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GradeController {

    private final GradeService gradeService;
    private final JwtUtil jwtUtil;

    /**
     * 添加成绩记录
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addGradeRecord(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody GradeRecord gradeRecord) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            GradeRecord savedRecord = gradeService.addGradeRecord(gradeRecord, userId);
            
            log.info("Grade record added for user: {} subject: {}", userId, gradeRecord.getSubject());
            return ResponseEntity.ok(Map.of(
                    "message", "成绩记录添加成功",
                    "gradeRecord", savedRecord
            ));
            
        } catch (Exception e) {
            log.error("Error adding grade record", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 批量添加成绩记录
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> addGradeRecords(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody List<GradeRecord> gradeRecords) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            List<GradeRecord> savedRecords = gradeService.addGradeRecords(gradeRecords, userId);
            
            log.info("Batch grade records added for user: {} count: {}", userId, gradeRecords.size());
            return ResponseEntity.ok(Map.of(
                    "message", "批量成绩记录添加成功",
                    "gradeRecords", savedRecords,
                    "count", savedRecords.size()
            ));
            
        } catch (Exception e) {
            log.error("Error adding batch grade records", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新成绩记录
     */
    @PutMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> updateGradeRecord(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long recordId,
            @Valid @RequestBody GradeRecord gradeRecord) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            GradeRecord updatedRecord = gradeService.updateGradeRecord(recordId, gradeRecord, userId);
            
            log.info("Grade record updated: {} by user: {}", recordId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "成绩记录更新成功",
                    "gradeRecord", updatedRecord
            ));
            
        } catch (Exception e) {
            log.error("Error updating grade record: {}", recordId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除成绩记录
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Map<String, String>> deleteGradeRecord(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long recordId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            gradeService.deleteGradeRecord(recordId, userId);
            
            log.info("Grade record deleted: {} by user: {}", recordId, userId);
            return ResponseEntity.ok(Map.of("message", "成绩记录删除成功"));
            
        } catch (Exception e) {
            log.error("Error deleting grade record: {}", recordId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取成绩记录详情
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<Map<String, Object>> getGradeRecord(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long recordId) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Optional<GradeRecord> record = gradeService.getGradeRecord(recordId, userId);
            
            if (record.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of("gradeRecord", record.get()));
            
        } catch (Exception e) {
            log.error("Error getting grade record: {}", recordId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取用户的所有成绩记录
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserGradeRecords(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String gradeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "examDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<GradeRecord> recordPage;
            
            if (subject != null && !subject.isEmpty()) {
                recordPage = gradeService.getGradeRecordsBySubject(userId, subject, pageable);
            } else if (gradeType != null && !gradeType.isEmpty()) {
                GradeRecord.GradeType type = GradeRecord.GradeType.valueOf(gradeType.toUpperCase());
                recordPage = gradeService.getGradeRecordsByType(userId, type, pageable);
            } else if (startDate != null && endDate != null) {
                recordPage = gradeService.getGradeRecordsByDateRange(userId, startDate, endDate, pageable);
            } else {
                recordPage = gradeService.getUserGradeRecords(userId, pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                    "gradeRecords", recordPage.getContent(),
                    "total", recordPage.getTotalElements(),
                    "totalPages", recordPage.getTotalPages(),
                    "currentPage", recordPage.getNumber(),
                    "size", recordPage.getSize()
            ));
            
        } catch (Exception e) {
            log.error("Error getting user grade records", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取科目成绩统计
     */
    @GetMapping("/stats/subject")
    public ResponseEntity<Map<String, Object>> getSubjectStats(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String subject) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Map<String, Object> stats = gradeService.getSubjectStatistics(userId, subject);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting subject stats for: {}", subject, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取整体成绩统计
     */
    @GetMapping("/stats/overall")
    public ResponseEntity<Map<String, Object>> getOverallStats(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Map<String, Object> stats = gradeService.getOverallStatistics(userId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting overall stats", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取成绩趋势分析
     */
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getGradeTrend(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String subject,
            @RequestParam(defaultValue = "6") int months) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Map<String, Object> trend = gradeService.getGradeTrend(userId, subject, months);
            
            return ResponseEntity.ok(trend);
            
        } catch (Exception e) {
            log.error("Error getting grade trend for subject: {}", subject, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 预测成绩趋势
     */
    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> predictGradeTrend(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            String subject = (String) request.get("subject");
            Integer months = request.get("months") != null ? 
                    Integer.valueOf(request.get("months").toString()) : 3;
            
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "科目不能为空"));
            }
            
            Map<String, Object> prediction = gradeService.predictGradeTrend(userId, subject, months);
            
            log.info("Grade trend predicted for user: {} subject: {}", userId, subject);
            return ResponseEntity.ok(prediction);
            
        } catch (Exception e) {
            log.error("Error predicting grade trend", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取排名信息
     */
    @GetMapping("/ranking")
    public ResponseEntity<Map<String, Object>> getRanking(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String subject,
            @RequestParam(required = false) String examName) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Map<String, Object> ranking = gradeService.getRanking(userId, subject, examName);
            
            return ResponseEntity.ok(ranking);
            
        } catch (Exception e) {
            log.error("Error getting ranking for subject: {}", subject, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取用户的科目列表
     */
    @GetMapping("/subjects")
    public ResponseEntity<Map<String, Object>> getUserSubjects(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            List<String> subjects = gradeService.getUserSubjects(userId);
            
            return ResponseEntity.ok(Map.of(
                    "subjects", subjects,
                    "total", subjects.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting user subjects", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取考试列表
     */
    @GetMapping("/exams")
    public ResponseEntity<Map<String, Object>> getUserExams(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String subject) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            List<String> exams = gradeService.getUserExams(userId, subject);
            
            return ResponseEntity.ok(Map.of(
                    "exams", exams,
                    "total", exams.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting user exams", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 导出成绩数据
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportGradeData(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            List<GradeRecord> records = gradeService.exportGradeData(userId, subject, startDate, endDate);
            
            log.info("Grade data exported for user: {} count: {}", userId, records.size());
            return ResponseEntity.ok(Map.of(
                    "gradeRecords", records,
                    "total", records.size(),
                    "exportTime", java.time.LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Error exporting grade data", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取成绩分析报告
     */
    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getGradeAnalysis(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "6") int months) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            Map<String, Object> analysis = gradeService.getGradeAnalysis(userId, subject, months);
            
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("Error getting grade analysis", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 清空成绩记录
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearGradeRecords(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String subject) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "无效的认证信息"));
            }
            
            gradeService.clearGradeRecords(userId, subject);
            
            log.info("Grade records cleared for user: {} subject: {}", userId, subject);
            return ResponseEntity.ok(Map.of("message", 
                    subject != null ? "科目成绩记录清空成功" : "所有成绩记录清空成功"));
            
        } catch (Exception e) {
            log.error("Error clearing grade records", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从token中提取用户ID
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        
        return jwtUtil.getUserIdFromToken(token);
    }
}
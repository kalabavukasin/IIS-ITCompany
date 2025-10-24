package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PlSqlReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Transactional(readOnly = true)
    public Map<String, Object> calculateRecruitmentMetrics(LocalDate startDate, LocalDate endDate, Long jobPostingId) {
        String sql = "SELECT * FROM calculate_recruitment_metrics(?, ?, ?)";
        
        return jdbcTemplate.queryForMap(sql, startDate, endDate, jobPostingId);
    }

    @Transactional(readOnly = true)
    public List<ReportSection> generateComprehensiveReport(LocalDate startDate, LocalDate endDate) {
        try {
            String sql = "SELECT * FROM generate_comprehensive_recruitment_report(?, ?)";
            
            return jdbcTemplate.query(sql, new ReportSectionRowMapper(), startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error in generateComprehensiveReport: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("PL/SQL function error: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void setCurrentUserId(Long userId) {
        String sql = "SELECT set_current_user_id(?)";
        jdbcTemplate.queryForObject(sql, Long.class, userId);
    }

    @Transactional
    public void createTestData() {
        String sql = "SELECT create_test_data_for_performance()";
        jdbcTemplate.queryForObject(sql, Void.class);
    }

    @Transactional(readOnly = true)
    public PerformanceTestResult testIndexPerformance() {
        PerformanceTestResult result = new PerformanceTestResult();
        
        long startTime = System.currentTimeMillis();
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM applications a " +
            "JOIN application_status_history ash ON a.id = ash.application_id " +
            "WHERE a.application_status = 'HIRED' AND ash.entered_at > NOW() - INTERVAL '30 days'",
            Long.class
        );
        result.setTimeWithIndex(System.currentTimeMillis() - startTime);
        
        return result;
    }

    public static class ReportSection {
        private String reportSection;
        private String metricName;
        private Double metricValue;
        private String additionalInfo;

        public ReportSection() {}

        public ReportSection(String reportSection, String metricName, Double metricValue, String additionalInfo) {
            this.reportSection = reportSection;
            this.metricName = metricName;
            this.metricValue = metricValue;
            this.additionalInfo = additionalInfo;
        }

        public String getReportSection() { return reportSection; }
        public void setReportSection(String reportSection) { this.reportSection = reportSection; }

        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }

        public Double getMetricValue() { return metricValue; }
        public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }

        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }

    public static class PerformanceTestResult {
        private long timeWithIndex;
        private double improvementPercentage;

        public PerformanceTestResult() {}

        public long getTimeWithIndex() { return timeWithIndex; }
        public void setTimeWithIndex(long timeWithIndex) { 
            this.timeWithIndex = timeWithIndex;
        }

        public double getImprovementPercentage() { return improvementPercentage; }
        public void setImprovementPercentage(double improvementPercentage) { 
            this.improvementPercentage = improvementPercentage;
        }
    }

    private static class ReportSectionRowMapper implements RowMapper<ReportSection> {
        @Override
        public ReportSection mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReportSection(
                rs.getString("report_section"),
                rs.getString("metric_name"),
                rs.getDouble("metric_value"),
                rs.getString("additional_info")
            );
        }
    }
}

package com.synapse.engagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flyway-validation;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class MigrationSmokeTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationsCreateStep1ToStep8Tables() {
        assertThat(countRows("badges")).isEqualTo(3);
        assertThat(tableExists("groups")).isTrue();
        assertThat(tableExists("group_members")).isTrue();
        assertThat(tableExists("shared_contents")).isTrue();
        assertThat(tableExists("reports")).isTrue();
        assertThat(tableExists("user_profiles_gamification")).isTrue();
        assertThat(tableExists("xp_events")).isTrue();
        assertThat(tableExists("user_badges")).isTrue();
        assertThat(tableExists("user_streaks")).isTrue();
    }

    @Test
    void reportMigrationRejectsInvalidStatusAndTargetType() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO reports (reporter_id, target_type, target_id, reason, status, created_at)
                VALUES (1, 'STUDY_GROUP', 10, 'bad', 'DONE', CURRENT_TIMESTAMP)
                """)).satisfies(error -> assertThat(error.getMessage().toLowerCase())
                .contains("chk_reports_status"));

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO reports (reporter_id, target_type, target_id, reason, status, created_at)
                VALUES (1, 'POST', 10, 'bad', 'PENDING', CURRENT_TIMESTAMP)
                """)).satisfies(error -> assertThat(error.getMessage().toLowerCase())
                .contains("chk_reports_target_type"));
    }

    private int countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }
}

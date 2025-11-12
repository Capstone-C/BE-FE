package com.capstone.web.refrigerator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * Ensures the refrigerator_items table has the correct unique index policy:
 * Unique by (member_id, name, expiration_date).
 * Drops any legacy unique index that was only on (member_id, name).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefrigeratorSchemaFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String schema = conn.getCatalog(); // current database name
            String table = "refrigerator_items";

            if (schema == null) {
                log.warn("[SchemaFixer] Could not determine current schema; skipping index check.");
                return;
            }

            // Load unique indexes and their columns on the table
            String sql = "SELECT INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND NON_UNIQUE = 0 ORDER BY INDEX_NAME, SEQ_IN_INDEX";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, schema, table);

            Map<String, List<String>> uniqueIndexes = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                String indexName = Objects.toString(row.get("INDEX_NAME"));
                String columnName = Objects.toString(row.get("COLUMN_NAME"));
                uniqueIndexes.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnName);
            }

            // Find legacy unique index on (member_id, name)
            List<String> legacyIndexNames = new ArrayList<>();
            boolean hasDesired = false;
            for (Map.Entry<String, List<String>> e : uniqueIndexes.entrySet()) {
                List<String> cols = e.getValue();
                if (cols.size() == 2 && cols.get(0).equalsIgnoreCase("member_id") && cols.get(1).equalsIgnoreCase("name")) {
                    legacyIndexNames.add(e.getKey());
                }
                if (cols.size() == 3 && cols.get(0).equalsIgnoreCase("member_id") &&
                        cols.get(1).equalsIgnoreCase("name") && cols.get(2).equalsIgnoreCase("expiration_date")) {
                    hasDesired = true;
                }
            }

            // Drop legacy unique indexes
            for (String idx : legacyIndexNames) {
                try {
                    String dropSql = "ALTER TABLE `" + table + "` DROP INDEX `" + idx + "`";
                    jdbcTemplate.execute(dropSql);
                    log.info("[SchemaFixer] Dropped legacy unique index {} on {}.{}", idx, schema, table);
                } catch (Exception ex) {
                    log.warn("[SchemaFixer] Failed to drop index {}: {}", idx, ex.getMessage());
                }
            }

            // Ensure desired unique index exists
            if (!hasDesired) {
                String newIdxName = "uk_refrigerator_items_member_name_expiration";
                try {
                    String createSql = "ALTER TABLE `" + table + "` ADD UNIQUE INDEX `" + newIdxName +
                            "` (`member_id`, `name`, `expiration_date`)";
                    jdbcTemplate.execute(createSql);
                    log.info("[SchemaFixer] Created unique index {} on (member_id, name, expiration_date)", newIdxName);
                } catch (Exception ex) {
                    log.warn("[SchemaFixer] Failed to create desired unique index: {}", ex.getMessage());
                }
            } else {
                log.info("[SchemaFixer] Desired unique index on (member_id, name, expiration_date) already present.");
            }
        } catch (Exception e) {
            log.warn("[SchemaFixer] Skipping refrigerator index fix due to error: {}", e.getMessage());
        }
    }
}


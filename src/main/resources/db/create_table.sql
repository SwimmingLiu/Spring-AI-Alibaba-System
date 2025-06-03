CREATE TABLE ${tableName} (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              conversation_id VARCHAR(256) NULL,
                              messages TEXT NULL,
                              type varchar(100) NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
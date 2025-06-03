SELECT COUNT(*) > 0
FROM information_schema.tables
WHERE table_schema = DATABASE()
AND table_name = '${tableName}';
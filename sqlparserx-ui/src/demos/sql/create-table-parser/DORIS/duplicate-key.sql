-- demo-title: Doris 明细表 + 分桶
CREATE TABLE IF NOT EXISTS example_tbl_duplicate
(
    log_time DATETIME NOT NULL,
    log_type INT NOT NULL,
    error_code INT,
    error_msg VARCHAR(1024),
    op_id BIGINT,
    op_time DATETIME
)
DUPLICATE KEY(log_time, log_type, error_code)
DISTRIBUTED BY HASH(log_type) BUCKETS 10;

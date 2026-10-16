-- demo-title: CTAS + JOIN 字段血缘
CREATE TABLE user_order_summary AS
SELECT
    u.id AS user_id,
    u.name,
    SUM(o.amount) AS total_amount,
    COUNT(o.id) AS order_count
FROM users u
JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name;

-- demo-title: INSERT + JOIN 聚合血缘
INSERT INTO demo_user_summary (user_id, user_name, total_amount)
SELECT u.id, u.name, SUM(o.amount) AS total_amount
FROM demo_users u
JOIN demo_orders o ON o.user_id = u.id
GROUP BY u.id, u.name;

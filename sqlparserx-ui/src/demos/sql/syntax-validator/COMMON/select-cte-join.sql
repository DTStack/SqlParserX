-- demo-title: CTE + JOIN 查询
WITH order_totals AS (
    SELECT user_id, SUM(amount) AS total_amount
    FROM demo_orders
    GROUP BY user_id
)
SELECT u.id, u.name, COALESCE(o.total_amount, 0) AS total_amount
FROM demo_users u
LEFT JOIN order_totals o ON o.user_id = u.id
WHERE u.status = 'ACTIVE';

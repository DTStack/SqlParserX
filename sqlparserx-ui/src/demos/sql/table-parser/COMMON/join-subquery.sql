-- demo-title: JOIN + 子查询
SELECT u.id, u.name, o.total_amount
FROM demo_users u
JOIN (
    SELECT user_id, SUM(amount) AS total_amount
    FROM demo_orders
    GROUP BY user_id
) o ON o.user_id = u.id
WHERE u.status = 'ACTIVE';

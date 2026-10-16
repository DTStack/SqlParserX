-- demo-title: 多条查询语句
SELECT id, name FROM demo_users;

SELECT user_id, SUM(amount) AS total_amount
FROM demo_orders
GROUP BY user_id;

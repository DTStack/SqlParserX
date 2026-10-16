-- demo-title: 单参数条件
SELECT u.id, u.name
FROM users u
WHERE u.id = ${userId}
  AND u.status IN (${statuses});

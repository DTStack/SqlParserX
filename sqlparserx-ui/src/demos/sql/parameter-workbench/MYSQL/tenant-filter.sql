-- demo-title: 租户 + 状态集合筛选
SELECT u.id, u.name
FROM users u
WHERE u.tenant_id = ${tenantId}
  AND u.status IN (${states});

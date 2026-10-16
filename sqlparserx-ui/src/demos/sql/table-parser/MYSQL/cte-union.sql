-- demo-title: CTE + 多层 JOIN + UNION 表提取
WITH t8 AS (
    SELECT * FROM table8
)
INSERT INTO new_table
SELECT * FROM table1 AS t1
LEFT JOIN (
    SELECT * FROM table3 AS tt3 LEFT JOIN table4 AS tt4 ON tt3.id = tt4.id
    UNION
    SELECT * FROM table2
) AS t34 ON t1.id = t34.id
UNION
SELECT * FROM table1
UNION
SELECT * FROM table4 AS t4
UNION
SELECT id, (SELECT MAX(col1) FROM table5 AS t5) AS col2,
       (SELECT COUNT(*) FROM table6 AS t6 WHERE t6.age > 10) AS col3
FROM table7
UNION
SELECT * FROM t8;

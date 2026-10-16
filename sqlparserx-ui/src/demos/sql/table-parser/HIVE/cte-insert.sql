-- demo-title: CTE + INSERT 表提取
WITH t1 AS (SELECT * FROM table1),
     t2 AS (SELECT * FROM table2)
INSERT INTO new_table
SELECT * FROM t1;

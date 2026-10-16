-- demo-title: 多层子查询 + 聚合 + CASE + UNION 血缘
-- demo-min-relations: 15
CREATE TABLE tz AS
SELECT
    CURDATE() AS STAT_DATE,
    '6' AS STAT_PRD,
    f.SECURITYCODE,
    f.SECURITYNAME,
    f.all_num,
    f.buy_num,
    f.incr_num,
    f.neut_num,
    f.redu_num,
    f.sell_num,
    f.tg_num,
    f.td_num,
    f.kp_num,
    f.rat_score,
    CASE
        WHEN f.rat_score >= 4.5 THEN '买入'
        WHEN f.rat_score >= 3.5 THEN '增持'
        WHEN f.rat_score >= 2.5 THEN '中性'
        WHEN f.rat_score >= 1.5 THEN '减持'
        ELSE '卖出'
    END AS rat_desc
FROM (
    SELECT
        p.SECURITYCODE,
        p.SECURITYNAME,
        p.all_num,
        p.buy_num,
        p.incr_num,
        p.neut_num,
        p.redu_num,
        p.sell_num,
        p.tg_num,
        p.td_num,
        p.kp_num,
        (p.buy_num * 5 + p.incr_num * 4 + p.neut_num * 3 + p.redu_num * 2 + p.sell_num) / p.all_num AS rat_score
    FROM (
        SELECT
            t.SECURITYCODE,
            t.SECURITYNAME,
            COUNT(DISTINCT t.COMPANYCODE) AS all_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 1 THEN 1 ELSE 0 END) AS buy_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 2 THEN 1 ELSE 0 END) AS incr_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 3 THEN 1 ELSE 0 END) AS neut_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 4 THEN 1 ELSE 0 END) AS redu_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 5 THEN 1 ELSE 0 END) AS sell_num,
            SUM(CASE WHEN t.RATINGCHANGE IN (0, 2) THEN 1 ELSE 0 END) AS tg_num,
            SUM(CASE WHEN t.RATINGCHANGE = 1 THEN 1 ELSE 0 END) AS td_num,
            SUM(CASE WHEN t.RATINGCHANGE NOT IN (0, 1, 2) OR t.RATINGCHANGE IS NULL THEN 1 ELSE 0 END) AS kp_num
        FROM (
            SELECT DISTINCT
                a.SECURITYCODE,
                a.SECURITYNAME,
                a.EMRATINGVALUE,
                a.EMRATINGNAME,
                c.RATINGCHANGE,
                d.PARAMCHNAME,
                a.COMPANYCODE,
                a.COMPANYNAME
            FROM INFO_RE_RATINGINFO a
            INNER JOIN (
                SELECT SECURITYCODE, COMPANYCODE, MAX(PUBLISHDATE) AS PUBLISHDATE
                FROM INFO_RE_RATINGINFO
                WHERE PUBLISHDATE >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
                GROUP BY SECURITYCODE, COMPANYCODE
            ) b ON a.SECURITYCODE = b.SECURITYCODE
                AND a.COMPANYCODE = b.COMPANYCODE
                AND a.PUBLISHDATE = b.PUBLISHDATE
            LEFT JOIN INFO_RE_BASINFO c ON a.INFOCODE = c.INFOCODE
            LEFT JOIN CFP_PVALUE d ON c.RATINGCHANGE = d.PARAMCODE
                AND d.NIPMID = '138000000404610438'
            WHERE a.PUBLISHDATE >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
        ) t
        GROUP BY t.SECURITYCODE, t.SECURITYNAME
    ) p
) f

UNION ALL

SELECT
    CURDATE() AS STAT_DATE,
    '3' AS STAT_PRD,
    f.SECURITYCODE,
    f.SECURITYNAME,
    f.all_num,
    f.buy_num,
    f.incr_num,
    f.neut_num,
    f.redu_num,
    f.sell_num,
    f.tg_num,
    f.td_num,
    f.kp_num,
    f.rat_score,
    CASE
        WHEN f.rat_score >= 4.5 THEN '买入'
        WHEN f.rat_score >= 3.5 THEN '增持'
        WHEN f.rat_score >= 2.5 THEN '中性'
        WHEN f.rat_score >= 1.5 THEN '减持'
        ELSE '卖出'
    END AS rat_desc
FROM (
    SELECT
        p.SECURITYCODE,
        p.SECURITYNAME,
        p.all_num,
        p.buy_num,
        p.incr_num,
        p.neut_num,
        p.redu_num,
        p.sell_num,
        p.tg_num,
        p.td_num,
        p.kp_num,
        (p.buy_num * 5 + p.incr_num * 4 + p.neut_num * 3 + p.redu_num * 2 + p.sell_num) / p.all_num AS rat_score
    FROM (
        SELECT
            t.SECURITYCODE,
            t.SECURITYNAME,
            COUNT(DISTINCT t.COMPANYCODE) AS all_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 1 THEN 1 ELSE 0 END) AS buy_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 2 THEN 1 ELSE 0 END) AS incr_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 3 THEN 1 ELSE 0 END) AS neut_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 4 THEN 1 ELSE 0 END) AS redu_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 5 THEN 1 ELSE 0 END) AS sell_num,
            SUM(CASE WHEN t.RATINGCHANGE IN (0, 2) THEN 1 ELSE 0 END) AS tg_num,
            SUM(CASE WHEN t.RATINGCHANGE = 1 THEN 1 ELSE 0 END) AS td_num,
            SUM(CASE WHEN t.RATINGCHANGE NOT IN (0, 1, 2) OR t.RATINGCHANGE IS NULL THEN 1 ELSE 0 END) AS kp_num
        FROM (
            SELECT DISTINCT
                a.SECURITYCODE,
                a.SECURITYNAME,
                a.EMRATINGVALUE,
                a.EMRATINGNAME,
                c.RATINGCHANGE,
                d.PARAMCHNAME,
                a.COMPANYCODE,
                a.COMPANYNAME
            FROM INFO_RE_RATINGINFO a
            INNER JOIN (
                SELECT SECURITYCODE, COMPANYCODE, MAX(PUBLISHDATE) AS PUBLISHDATE
                FROM INFO_RE_RATINGINFO
                WHERE PUBLISHDATE >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
                GROUP BY SECURITYCODE, COMPANYCODE
            ) b ON a.SECURITYCODE = b.SECURITYCODE
                AND a.COMPANYCODE = b.COMPANYCODE
                AND a.PUBLISHDATE = b.PUBLISHDATE
            LEFT JOIN INFO_RE_BASINFO c ON a.INFOCODE = c.INFOCODE
            LEFT JOIN CFP_PVALUE d ON c.RATINGCHANGE = d.PARAMCODE
                AND d.NIPMID = '138000000404610438'
            WHERE a.PUBLISHDATE >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        ) t
        GROUP BY t.SECURITYCODE, t.SECURITYNAME
    ) p
) f

UNION ALL

SELECT
    CURDATE() AS STAT_DATE,
    '1' AS STAT_PRD,
    f.SECURITYCODE,
    f.SECURITYNAME,
    f.all_num,
    f.buy_num,
    f.incr_num,
    f.neut_num,
    f.redu_num,
    f.sell_num,
    f.tg_num,
    f.td_num,
    f.kp_num,
    f.rat_score,
    CASE
        WHEN f.rat_score >= 4.5 THEN '买入'
        WHEN f.rat_score >= 3.5 THEN '增持'
        WHEN f.rat_score >= 2.5 THEN '中性'
        WHEN f.rat_score >= 1.5 THEN '减持'
        ELSE '卖出'
    END AS rat_desc
FROM (
    SELECT
        p.SECURITYCODE,
        p.SECURITYNAME,
        p.all_num,
        p.buy_num,
        p.incr_num,
        p.neut_num,
        p.redu_num,
        p.sell_num,
        p.tg_num,
        p.td_num,
        p.kp_num,
        (p.buy_num * 5 + p.incr_num * 4 + p.neut_num * 3 + p.redu_num * 2 + p.sell_num) / p.all_num AS rat_score
    FROM (
        SELECT
            t.SECURITYCODE,
            t.SECURITYNAME,
            COUNT(DISTINCT t.COMPANYCODE) AS all_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 1 THEN 1 ELSE 0 END) AS buy_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 2 THEN 1 ELSE 0 END) AS incr_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 3 THEN 1 ELSE 0 END) AS neut_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 4 THEN 1 ELSE 0 END) AS redu_num,
            SUM(CASE WHEN t.EMRATINGVALUE = 5 THEN 1 ELSE 0 END) AS sell_num,
            SUM(CASE WHEN t.RATINGCHANGE IN (0, 2) THEN 1 ELSE 0 END) AS tg_num,
            SUM(CASE WHEN t.RATINGCHANGE = 1 THEN 1 ELSE 0 END) AS td_num,
            SUM(CASE WHEN t.RATINGCHANGE NOT IN (0, 1, 2) OR t.RATINGCHANGE IS NULL THEN 1 ELSE 0 END) AS kp_num
        FROM (
            SELECT DISTINCT
                a.SECURITYCODE,
                a.SECURITYNAME,
                a.EMRATINGVALUE,
                a.EMRATINGNAME,
                c.RATINGCHANGE,
                d.PARAMCHNAME,
                a.COMPANYCODE,
                a.COMPANYNAME
            FROM INFO_RE_RATINGINFO a
            INNER JOIN (
                SELECT SECURITYCODE, COMPANYCODE, MAX(PUBLISHDATE) AS PUBLISHDATE
                FROM INFO_RE_RATINGINFO
                WHERE PUBLISHDATE >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
                GROUP BY SECURITYCODE, COMPANYCODE
            ) b ON a.SECURITYCODE = b.SECURITYCODE
                AND a.COMPANYCODE = b.COMPANYCODE
                AND a.PUBLISHDATE = b.PUBLISHDATE
            LEFT JOIN INFO_RE_BASINFO c ON a.INFOCODE = c.INFOCODE
            LEFT JOIN CFP_PVALUE d ON c.RATINGCHANGE = d.PARAMCODE
                AND d.NIPMID = '138000000404610438'
            WHERE a.PUBLISHDATE >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
        ) t
        GROUP BY t.SECURITYCODE, t.SECURITYNAME
    ) p
) f;

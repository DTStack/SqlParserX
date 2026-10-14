import React from 'react';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

function CapabilityIcon({type}) {
  const common = {
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.7,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
    'aria-hidden': true,
  };

  if (type === 'type') return <svg {...common}><path d="M5 3h10l4 4v14H5zM15 3v5h4M8 12h8M8 16h5"/><path className={styles.iconAccent} d="m8 8 1.5 1.5L12 7"/></svg>;
  if (type === 'structure') return <svg {...common}><ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v6c0 1.7 3.6 3 8 3s8-1.3 8-3V5M4 11v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6"/><path className={styles.iconAccent} d="M8 9h8"/></svg>;
  if (type === 'tableLineage') return <svg {...common}><rect x="2.5" y="8.5" width="6" height="7" rx="1.5"/><rect x="15.5" y="3" width="6" height="7" rx="1.5"/><rect x="15.5" y="14" width="6" height="7" rx="1.5"/><path className={styles.iconAccent} d="M8.5 12H12V6.5h3.5M12 12v5.5h3.5"/></svg>;
  if (type === 'columnLineage') return <svg {...common}><rect x="2" y="3" width="7" height="18" rx="1.5"/><rect x="15" y="3" width="7" height="18" rx="1.5"/><path d="M4.5 7h2M4.5 11h2M17.5 7h2M17.5 11h2M17.5 15h2"/><path className={styles.iconAccent} d="M6.5 15h4l3-6h3M14 7l2.5 2L14 11"/></svg>;
  if (type === 'function') return <svg {...common}><path d="M10.5 4.5c-2.2 0-3.4 1.25-3.85 3.75L5.3 16c-.42 2.35-1.45 3.5-3.3 3.5M4.5 11.5h6"/><path className={styles.iconAccent} d="M14 8.5l3 3.5-3 3.5M22 8.5l-3 3.5 3 3.5"/></svg>;
  return <svg {...common}><path d="M7 3H5a2 2 0 0 0-2 2v4l-2 3 2 3v4a2 2 0 0 0 2 2h2M17 3h2a2 2 0 0 1 2 2v4l2 3-2 3v4a2 2 0 0 1-2 2h-2"/><path className={styles.iconAccent} d="M9 8h6M9 12h6M9 16h6"/></svg>;
}

const CARDS = [
  {
    icon: 'type', key: 'type', titleId: 'homepage.feature.type.title', title: 'SQL 类型识别',
    descId: 'homepage.feature.type.desc', desc: '识别查询、写入、建表、SHOW 与 EXPLAIN 等语句类型。',
  },
  {
    icon: 'structure', key: 'table', titleId: 'homepage.feature.table.title', title: '真实表与建表结构',
    descId: 'homepage.feature.table.desc', desc: '穿透 JOIN、CTE 与子查询，解析字段、键、分区及分桶结构。',
  },
  {
    icon: 'tableLineage', key: 'tableLineage', titleId: 'homepage.feature.tableLineage.title', title: '表级血缘',
    descId: 'homepage.feature.tableLineage.desc', desc: '从 INSERT 与 CTAS 追溯来源表到目标表的数据流转关系。',
  },
  {
    icon: 'columnLineage', key: 'columnLineage', titleId: 'homepage.feature.columnLineage.title', title: '字段级血缘',
    descId: 'homepage.feature.columnLineage.desc', desc: '分析表达式、函数与 CASE 中来源字段到目标字段的映射。',
  },
  {
    icon: 'function', key: 'function', titleId: 'homepage.feature.function.title', title: '函数名提取',
    descId: 'homepage.feature.function.desc', desc: '提取普通、聚合与窗口函数名称，支持多语句解析与去重。',
  },
  {
    icon: 'parameter', key: 'parameter', titleId: 'homepage.feature.parameter.title', title: '参数提取与预编译',
    descId: 'homepage.feature.parameter.desc', desc: '提取 SELECT 动态参数，生成 JDBC 占位符 SQL 与有序绑定值。',
  },
];

export default function Features() {
  return (
    <section className={styles.section}>
      <div className={styles.header}>
        <span className={styles.eyebrow}>
          <Translate id="homepage.features.eyebrow">核心能力</Translate>
        </span>
        <h2 className={styles.heading}>
          <Translate id="homepage.features.heading">
            一个引擎，覆盖 SQL 解析全链路
          </Translate>
        </h2>
        <p className={styles.subheading}>
          <Translate id="homepage.features.subheading">
            从方言语法到业务语义，统一契约、统一结果，开箱即用。
          </Translate>
        </p>
      </div>

      <div className={styles.grid}>
        {CARDS.map((card) => (
          <article key={card.key} className={styles.card}>
            <div className={styles.iconWrap}>
              <CapabilityIcon type={card.icon} />
            </div>
            <h3 className={styles.cardTitle}>
              <Translate id={card.titleId}>{card.title}</Translate>
            </h3>
            <p className={styles.cardDesc}>
              <Translate id={card.descId}>{card.desc}</Translate>
            </p>
          </article>
        ))}
      </div>
    </section>
  );
}

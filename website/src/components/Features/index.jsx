import React from 'react';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

const CARDS = [
  {
    icon: '🧩',
    key: 'dialect',
    titleId: 'homepage.feature.dialect.title',
    title: '多方言解析',
    descId: 'homepage.feature.dialect.desc',
    desc: '通过 DbType 路由 Druid 方言，统一 AST，覆盖 15+ 数据源。',
  },
  {
    icon: '🔗',
    key: 'lineage',
    titleId: 'homepage.feature.lineage.title',
    title: '表与字段血缘',
    descId: 'homepage.feature.lineage.desc',
    desc: '覆盖 JOIN、UNION、子查询、聚合与 CTE 的表级与字段级血缘。',
  },
  {
    icon: '🗂️',
    key: 'table',
    titleId: 'homepage.feature.table.title',
    title: '表与 DDL 解析',
    descId: 'homepage.feature.table.desc',
    desc: '识别真实表、CTE、CREATE TABLE、分区与表属性。',
  },
  {
    icon: '🧮',
    key: 'function',
    titleId: 'homepage.feature.function.title',
    title: '函数解析',
    descId: 'homepage.feature.function.desc',
    desc: '提取普通、聚合、窗口与嵌套函数及其参数表达式。',
  },
  {
    icon: '✂️',
    key: 'split',
    titleId: 'homepage.feature.split.title',
    title: 'SQL 拆分与校验',
    descId: 'homepage.feature.split.desc',
    desc: '拆分多语句脚本，并进行语法校验与行级权限改写。',
  },
  {
    icon: '🏗️',
    key: 'arch',
    titleId: 'homepage.feature.arch.title',
    title: '两层扩展架构',
    descId: 'homepage.feature.arch.desc',
    desc: '语法差异扩展在 syntax 层，业务差异扩展在 core 层，扩展点统一。',
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
              <span className={styles.icon}>{card.icon}</span>
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
import React from 'react';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

export default function Intro() {
  return (
    <header className={styles.hero}>
      <div className={styles.gridBg} aria-hidden="true" />
      <div className={styles.glowA} aria-hidden="true" />
      <div className={styles.glowB} aria-hidden="true" />

      <div className={styles.inner}>
        <div className={styles.left}>
          <div className={styles.badge}>
            <span className={styles.badgeDot} />
            <Translate id="homepage.hero.badge">
              Java 8 · Druid 1.2.28 · 15+ 方言
            </Translate>
          </div>

          <h1 className={styles.title}>
            <span className={styles.titleGrad}>SqlParserX</span>
          </h1>

          <p className={styles.tagline}>
            <Translate id="homepage.hero.tagline">
              多方言 SQL 解析引擎与工具包
            </Translate>
          </p>

          <p className={styles.desc}>
            <Translate id="homepage.hero.desc">
              表与字段血缘 · 表/DDL 解析 · 函数提取 · SQL 拆分与行级权限改写
            </Translate>
          </p>

          <div className={styles.actions}>
            <Link
              className={`button button--primary button--lg ${styles.ctaPrimary}`}
              to="/docs/quickstart/setup">
              <Translate id="homepage.hero.cta.primary">快速开始</Translate>
              <span aria-hidden="true" className={styles.arrow}>
                →
              </span>
            </Link>
            <Link
              className={`button button--secondary button--lg ${styles.ctaSecondary}`}
              to="/docs/about/introduction">
              <Translate id="homepage.hero.cta.secondary">项目介绍</Translate>
            </Link>
            <Link
              className={styles.ghLink}
              href="https://github.com/DTStack/SqlParserX"
              aria-label="GitHub repository">
              GitHub
            </Link>
          </div>

          <ul className={styles.stats}>
            <li>
              <strong>15+</strong>
              <span>
                <Translate id="homepage.hero.stat.datasource">数据源方言</Translate>
              </span>
            </li>
            <li>
              <strong>4</strong>
              <span>
                <Translate id="homepage.hero.stat.capability">核心解析能力</Translate>
              </span>
            </li>
            <li>
              <strong>2</strong>
              <span>
                <Translate id="homepage.hero.stat.layer">层统一架构</Translate>
              </span>
            </li>
            <li>
              <strong>100%</strong>
              <span>
                <Translate id="homepage.hero.stat.compat">Java 8 兼容</Translate>
              </span>
            </li>
          </ul>
        </div>

        <div className={styles.right}>
          <div className={styles.window}>
            <div className={styles.windowBar}>
              <span className={styles.dots}>
                <i />
                <i />
                <i />
              </span>
              <span className={styles.windowTitle}>LineagePreview</span>
            </div>

            <div className={styles.pane}>
              <div className={styles.paneLabel}>
                <Translate id="homepage.hero.pane.input">SQL 输入</Translate>
              </div>
              <div className={styles.code}>
                <div className={styles.line}>
                  <span className={styles.kw}>INSERT INTO</span>{' '}
                  <span className={styles.tbl}>dwd.order_di</span>
                </div>
                <div className={styles.line}>
                  <span className={styles.kw}>SELECT</span> o.id, u.name,
                  o.amount
                </div>
                <div className={styles.line}>
                  <span className={styles.kw}>FROM</span> ods.orders o
                </div>
                <div className={styles.line}>
                  <span className={styles.kw}>JOIN</span> ods.users u{' '}
                  <span className={styles.kw}>ON</span> o.uid = u.id;
                </div>
              </div>
            </div>

            <div className={styles.divider}>
              <span className={styles.dividerDot} />
              <Translate id="homepage.hero.pane.output">解析结果</Translate>
            </div>

            <div className={styles.pane}>
              <div className={styles.result}>
                <div className={styles.resLine}>
                  <span className={styles.check}>✓</span>
                  <span className={styles.tag}>
                    <Translate id="homepage.hero.tag.table">表血缘</Translate>
                  </span>
                  ods.orders <span className={styles.arrowSm}>→</span>{' '}
                  dwd.order_di
                </div>
                <div className={styles.resLine}>
                  <span className={styles.check}>✓</span>
                  <span className={styles.tag}>表血缘</span>
                  ods.users <span className={styles.arrowSm}>→</span>{' '}
                  dwd.order_di
                </div>
                <div className={styles.resLine}>
                  <span className={styles.check}>✓</span>
                  <span className={styles.tagWarm}>
                    <Translate id="homepage.hero.tag.field">字段血缘</Translate>
                  </span>
                  ods.orders.amount <span className={styles.arrowSm}>→</span>{' '}
                  dwd.order_di.amount
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
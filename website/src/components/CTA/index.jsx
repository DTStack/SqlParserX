import React from 'react';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

export default function CTA() {
  return (
    <section className={styles.band}>
      <div className={styles.inner}>
        <h2 className={styles.title}>
          <Translate id="homepage.cta.title">
            准备好开始解析你的 SQL 了吗？
          </Translate>
        </h2>
        <p className={styles.desc}>
          <Translate id="homepage.cta.desc">
            几分钟完成接入，体验多方言解析、血缘追溯与参数预编译能力。
          </Translate>
        </p>
        <div className={styles.actions}>
          <Link
            className={`button button--lg ${styles.primary}`}
            to="/docs/quickstart/setup">
            <Translate id="homepage.cta.primary">快速开始</Translate>
          </Link>
          <Link
            className={`button button--lg ${styles.secondary}`}
            href="https://github.com/DTStack/SqlParserX">
            <Translate id="homepage.cta.secondary">查看源码</Translate>
          </Link>
        </div>
      </div>
    </section>
  );
}

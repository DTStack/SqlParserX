import React from 'react';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import styles from './community.module.css';

const channels = [
  {
    icon: '💬',
    id: 'homepage.community.forum.title',
    title: '论坛',
    idDesc: 'homepage.community.forum.desc',
    desc: '在 GitHub Discussions 中提问、分享使用经验与最佳实践。',
    href: 'https://github.com/DTStack/SqlParserX/discussions',
    label: '前往讨论区',
  },
  {
    icon: '🐙',
    id: 'homepage.community.github.title',
    title: 'GitHub',
    idDesc: 'homepage.community.github.desc',
    desc: '提交 Issue、参与 Pull Request，一起完善多方言解析能力。',
    href: 'https://github.com/DTStack/SqlParserX',
    label: '访问仓库',
  },
  {
    icon: '❓',
    id: 'homepage.community.qa.title',
    title: '问答',
    idDesc: 'homepage.community.qa.desc',
    desc: '遇到使用问题，可在 Issues 中检索或发起新的讨论。',
    href: 'https://github.com/DTStack/SqlParserX/issues',
    label: '查看 Issues',
  },
];

export default function Community() {
  return (
    <Layout
      title="Community"
      description="SqlParserX 社区：论坛、GitHub、问答">
      <main className={styles.main}>
        <header className={styles.hero}>
          <h1 className={styles.title}>
            <Translate id="community.title">加入 SqlParserX 社区</Translate>
          </h1>
          <p className={styles.subtitle}>
            <Translate id="community.subtitle">
              与开发者一起交流多方言 SQL 解析、血缘与治理实践。
            </Translate>
          </p>
        </header>
        <section className={styles.grid}>
          {channels.map((c) => (
            <div key={c.id} className={styles.card}>
              <div className={styles.icon}>{c.icon}</div>
              <h3 className={styles.cardTitle}>
                <Translate id={c.id}>{c.title}</Translate>
              </h3>
              <p className={styles.cardDesc}>
                <Translate id={c.idDesc}>{c.desc}</Translate>
              </p>
              <Link className="button button--primary" href={c.href}>
                {c.label}
              </Link>
            </div>
          ))}
        </section>
      </main>
    </Layout>
  );
}
import React from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

const STEPS = [
  {
    id: 'homepage.case.step1',
    label: '接入 SQL + DbType',
  },
  {
    id: 'homepage.case.step2',
    label: '语法层解析为 Druid AST',
  },
  {
    id: 'homepage.case.step3',
    label: '业务层统一解析',
  },
  {
    id: 'homepage.case.step4',
    label: '表 / 血缘 / 函数 / 参数解析结果',
  },
];

export default function Case() {
  const baseUrl = useBaseUrl('/');

  return (
    <section className={styles.section}>
      <div className={styles.row}>
        <div className={styles.info}>
          <span className={styles.eyebrow}>
            <Translate id="homepage.case.arch.eyebrow">架构设计</Translate>
          </span>
          <h3 className={styles.title}>
            <Translate id="homepage.case.arch.title">
              两层统一扩展架构
            </Translate>
          </h3>
          <p className={styles.desc}>
            <Translate id="homepage.case.arch.desc">
              语法层基于 Druid 扩展方言 Lexer / Parser / AST，业务层通过
              ISqlParser 契约扩展表解析、血缘与改写逻辑，两层由 DbType 与 AST
              对齐。
            </Translate>
          </p>
          <ol className={styles.steps}>
            {STEPS.map((step, i) => (
              <li key={step.id} className={styles.step}>
                <span className={styles.stepNo}>{i + 1}</span>
                <Translate id={step.id}>{step.label}</Translate>
              </li>
            ))}
          </ol>
        </div>
        <div className={styles.showcase}>
          <img
            src={`${baseUrl}img/architecture/SqlParserX两层架构.png`}
            alt="SqlParserX 两层架构"
            loading="lazy"
          />
        </div>
      </div>

      <div className={`${styles.row} ${styles.reverse}`}>
        <div className={styles.info}>
          <span className={styles.eyebrow}>
            <Translate id="homepage.case.docs.eyebrow">文档站</Translate>
          </span>
          <h3 className={styles.title}>
            <Translate id="homepage.case.version.title">
              版本 / 多语言共存
            </Translate>
          </h3>
          <p className={styles.desc}>
            <Translate id="homepage.case.version.desc">
              右侧导航栏即可切换版本（1.0 / Next）与语言（简体中文 /
              English），互不干扰。
            </Translate>
          </p>
        </div>
        <div className={styles.showcase}>
          <img
            src={`${baseUrl}img/case/version.svg`}
            alt="version"
            loading="lazy"
          />
        </div>
      </div>
    </section>
  );
}

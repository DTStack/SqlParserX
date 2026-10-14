import React from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

const DATASOURCES = [
  ['mysql', 'MySQL'], ['hive', 'Hive'], ['doris', 'Doris'],
  ['starrocks', 'StarRocks'], ['clickhouse', 'ClickHouse'],
  ['oracle', 'Oracle'], ['postgresql', 'PostgreSQL'], ['spark', 'Spark'],
  [null, 'Trino'], [null, 'DM'], ['oceanbase', 'OceanBase'],
  [null, 'OceanBase Oracle'], ['sqlserver', 'SQL Server'],
  ['greenplum', 'Greenplum'], ['tidb', 'TiDB'],
];

export default function Datasource() {
  const baseUrl = useBaseUrl('/');

  return (
    <section className={styles.section}>
      <p className={styles.lead}>
        <Translate id="homepage.datasource.lead">
          已适配 15+ 数据源方言
        </Translate>
      </p>
      <div className={styles.row}>
        {DATASOURCES.map(([file, name]) => (
          <div key={file} className={styles.item} title={name}>
            {file ? <img
              src={`${baseUrl}img/datasource/${file}.png`}
              alt=""
              loading="lazy"
            /> : <span className={styles.fallbackIcon} aria-hidden="true">◆</span>}
            <span>{name}</span>
          </div>
        ))}
      </div>
    </section>
  );
}

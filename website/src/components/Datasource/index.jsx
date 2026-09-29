import React from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import Translate from '@docusaurus/Translate';
import styles from './styles.module.css';

const DATASOURCES = [
  ['mysql', 'MySQL'],
  ['doris', 'Doris'],
  ['starrocks', 'StarRocks'],
  ['hive', 'Hive'],
  ['clickhouse', 'ClickHouse'],
  ['postgresql', 'PostgreSQL'],
  ['oracle', 'Oracle'],
  ['sqlserver', 'SQL Server'],
  ['spark', 'Spark'],
  ['tidb', 'TiDB'],
  ['oceanbase', 'OceanBase'],
  ['greenplum', 'Greenplum'],
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
            <img
              src={`${baseUrl}img/datasource/${file}.png`}
              alt={name}
              loading="lazy"
            />
            <span>{name}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
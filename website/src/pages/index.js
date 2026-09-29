import React from 'react';
import Layout from '@theme/Layout';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Intro from '@site/src/components/Intro';
import Datasource from '@site/src/components/Datasource';
import Features from '@site/src/components/Features';
import Case from '@site/src/components/Case';
import CTA from '@site/src/components/CTA';
import styles from './index.module.css';

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout title={siteConfig.title} description={siteConfig.tagline}>
      <Intro />
      <main className={styles.main}>
        <Datasource />
        <Features />
        <Case />
        <CTA />
      </main>
    </Layout>
  );
}
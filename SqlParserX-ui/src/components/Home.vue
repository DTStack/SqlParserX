<template>
  <div class="home">
    <h1>SQL 工具集</h1>
    <div class="nav-links">
      <router-link to="/syntax-validator" class="nav-card">
        <div class="card-content">
          <h2>在线语法解析验证工具</h2>
          <p>验证SQL语句的语法正确性</p>
        </div>
      </router-link>
      <router-link to="/lineage" class="nav-card">
        <div class="card-content">
          <h2>血缘解析可视化工具</h2>
          <p>可视化展示SQL语句中的表和字段血缘关系</p>
        </div>
      </router-link>
      <router-link to="/sql-splitter" class="nav-card">
        <div class="card-content">
          <h2>SQL切分测试工具</h2>
          <p>将多条SQL语句分割成独立的语句</p>
        </div>
      </router-link>
      <router-link to="/table-parser" class="nav-card">
        <div class="card-content">
          <h2>SQL表解析工具</h2>
          <p>解析SQL语句中的所有表名</p>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  created() {
    // 在 Vue 组件或任何 JS 文件中
    console.log("import.meta.env=>", import.meta.env)
    console.log("import.meta.env.VITE_API_URL=>", import.meta.env.VITE_API_URL)
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px; /* 保持原有内边距 */
  text-align: center;
}

h1 {
  color: #333;
  margin-bottom: 40px;
}

.nav-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.nav-card {
  width: 350px;
  padding: 25px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  color: inherit;
  transition: transform 0.3s, box-shadow 0.3s;
}

.nav-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.card-content h2 {
  color: #409EFF;
  margin-bottom: 15px;
}

.card-content p {
  color: #666;
  line-height: 1.6;
}
</style>
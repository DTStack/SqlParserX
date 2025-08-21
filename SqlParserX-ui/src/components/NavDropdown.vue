<template>
  <div class="nav-dropdown">
    <el-dropdown trigger="click" @command="handleCommand">
      <span class="dropdown-trigger">
        <i class="el-icon-arrow-down"></i> SQL工具集
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="/syntax-validator">在线语法解析验证工具</el-dropdown-item>
        <el-dropdown-item command="/lineage">血缘解析可视化工具</el-dropdown-item>
        <el-dropdown-item command="/sql-splitter">SQL切分测试工具</el-dropdown-item>
        <el-dropdown-item command="/table-parser">SQL表解析工具</el-dropdown-item>
        <el-dropdown-item command="/create-table-parser">建表SQL解析工具</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  name: 'NavDropdown',
  methods: {
    handleCommand(command) {
      if (this.$route.path !== command) {
        this.$router.push(command);
      }
    }
  }
};
</script>

<style scoped>
.nav-dropdown {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  padding: 8px 15px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.dropdown-trigger:hover {
  background-color: #f5f7fa;
}

.dropdown-trigger i {
  margin-right: 5px;
  font-size: 14px;
}
</style>
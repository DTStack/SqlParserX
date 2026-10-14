<template>
  <div class="nav-dropdown">
    <el-dropdown trigger="click" @command="handleCommand">
      <span class="dropdown-trigger">
        <i class="el-icon-menu"></i> SQL工具集
        <i class="el-icon-arrow-down caret"></i>
      </span>
      <el-dropdown-menu slot="dropdown" class="px-dropdown-menu">
        <el-dropdown-item command="/lineage">血缘解析可视化工具</el-dropdown-item>
        <el-dropdown-item command="/parameter-workbench">SQL 参数提取与预编译</el-dropdown-item>
        <el-dropdown-item command="/syntax-validator">在线语法解析验证工具</el-dropdown-item>
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
  position: static;
  z-index: 1000;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: #374151;
  background: #fff;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.dropdown-trigger:hover {
  background: #eff6ff;
  border-color: #2563eb;
  color: #2563eb;
}

.dropdown-trigger .caret {
  font-size: 12px;
}
</style>

<style>
/* 下拉菜单浅色主题（element-ui 弹层挂载在 body 下，需全局样式） */
.px-dropdown-menu.el-dropdown-menu {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  padding: 6px;
}
.px-dropdown-menu .el-dropdown-menu__item {
  color: #374151;
  border-radius: 8px;
  font-size: 14px;
  padding: 9px 16px;
}
.px-dropdown-menu .el-dropdown-menu__item:hover,
.px-dropdown-menu .el-dropdown-menu__item:focus {
  background: #eff6ff;
  color: #2563eb;
}
</style>

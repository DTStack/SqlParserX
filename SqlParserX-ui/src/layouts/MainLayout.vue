<template>
  <div class="main-layout">
    <!-- 只在非首页显示导航栏 -->
    <header v-if="showNavDropdown" class="app-header">
      <div class="header-content">
        <router-link to="/" class="home-link">
          <i class="el-icon-s-home"></i>
          <span>返回首页</span>
        </router-link>
        <nav-dropdown></nav-dropdown>
      </div>
    </header>

    <!-- 子页面内容容器 -->
    <div :class="['content-container', { 'has-nav': showNavDropdown }]">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
import NavDropdown from '@/components/NavDropdown.vue';

export default {
  name: 'MainLayout',
  components: {NavDropdown},
  computed: {
    showNavDropdown() {
      return this.$route.path !== '/';
    }
  }
};
</script>

<style scoped>
.main-layout {
  position: relative;
  min-height: 100vh;
}

/* 顶部导航栏样式 */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 88%);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 6%;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
  display: flex;
  align-items: center;
}

/* 返回首页按钮样式 */
.home-link {
  display: flex;
  align-items: center;
  color: white;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.home-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.home-link i {
  margin-right: 8px;
  font-size: 18px;
}

/* 内容容器样式 */
.content-container {
  padding: 20px;
}

/* 当有导航时的内容容器样式 */
.content-container.has-nav {
  padding: 4% 10px 10px 10px;
}

/* 调整下拉菜单样式 */
.main-layout >>> .nav-dropdown {
  margin-left: 30px;
  position: static;
  background: transparent;
  box-shadow: none;
}
</style>
<template>
  <div class="main-layout">
    <!-- 只在非首页显示导航栏 -->
    <header v-if="showNavDropdown" class="app-header">
      <div class="header-content">
        <router-link to="/" class="home-link">
          <i class="el-icon-s-home"></i>
          <span>返回首页</span>
        </router-link>
        <div class="header-brand">SqlParserX</div>
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
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏：深色玻璃拟态 */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
  display: flex;
  align-items: center;
  background: rgba(13, 18, 40, 0.82);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(99, 102, 241, 0.3);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.35);
}

.header-content {
  width: 100%;
  max-width: 1280px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 18px;
}

/* 品牌标识 */
.header-brand {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(120deg, #a5b4fc, #22d3ee);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  padding: 0 6px;
}

/* 返回首页按钮样式 */
.home-link {
  display: flex;
  align-items: center;
  color: #c7d2fe;
  text-decoration: none;
  font-size: 15px;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.home-link:hover {
  background: rgba(99, 102, 241, 0.16);
  border-color: rgba(99, 102, 241, 0.4);
  color: #fff;
}

.home-link i {
  margin-right: 8px;
  font-size: 18px;
}

/* 内容容器样式 */
.content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 60px; /* 与header高度一致 */
  overflow: hidden; /* 防止内部内容溢出 */
}

/* 当有导航时的内容容器样式 */
.content-container.has-nav {
  margin-top: 72px;
  padding: 0; /* 移除padding，由内部组件控制 */
}

/* 调整下拉菜单样式 */
.main-layout >>> .nav-dropdown {
  margin-left: auto;
  position: static;
  background: transparent;
  box-shadow: none;
}
</style>

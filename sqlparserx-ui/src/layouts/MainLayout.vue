<template>
  <div class="main-layout">
    <!-- 只在非首页显示导航栏 -->
    <header v-if="showNavDropdown" class="app-header">
      <div class="header-content">
        <router-link to="/" class="home-link">
          <i class="el-icon-s-home"></i>
          <span>返回首页</span>
        </router-link>
        <div class="header-brand"><span>SqlParser</span><span class="header-brand-x">X</span></div>
        <nav-dropdown></nav-dropdown>
      </div>
    </header>

    <!-- 子页面内容容器 -->
    <div :class="['content-container', { 'has-nav': showNavDropdown }, { 'is-home': !$route.path || $route.path === '/' }]">
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
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏：浅色 */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
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
  display: flex;
  align-items: center;
  font-size: 19px;
  font-weight: 800;
  letter-spacing: -0.55px;
  color: #172b44;
  -webkit-text-stroke: 0.25px currentColor;
  padding: 0 6px;
}

.header-brand-x {
  margin-left: 1px;
  background: linear-gradient(180deg, #11c9e8 0%, #1684ff 55%, #073de1 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  -webkit-text-fill-color: transparent;
  -webkit-text-stroke: 0;
}

/* 返回首页按钮样式 */
.home-link {
  display: flex;
  align-items: center;
  color: #4b5563;
  text-decoration: none;
  font-size: 15px;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.home-link:hover {
  background: #eff6ff;
  border-color: #dbeafe;
  color: #2563eb;
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

/* 首页容器：铺满不推下 */
.content-container.is-home {
  margin-top: 0;
  display: block;
  flex: none;
}

/* 调整下拉菜单样式 */
.main-layout >>> .nav-dropdown {
  margin-left: auto;
  position: static;
  background: transparent;
  box-shadow: none;
}
</style>

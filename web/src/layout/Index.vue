
<template>
  <el-container class="layout-container">
    <!-- 移动端顶部导航栏 -->
    <el-header v-if="isMobile" class="mobile-header">
      <div class="mobile-toolbar">
        <el-button 
          type="text" 
          :icon="Menu" 
          @click="toggleMobileMenu"
          class="menu-toggle"
          size="large"
        />
        <div class="mobile-logo">API Pool</div>
        <div class="mobile-actions">
          <el-button 
            type="text" 
            :icon="Refresh" 
            size="large"
            @click="handleRefresh"
            title="刷新"
          />
        </div>
      </div>
    </el-header>

    <!-- 侧边栏导航 -->
    <el-aside 
      :width="sidebarWidth" 
      class="main-aside"
      :class="{
        'aside-collapsed': isTablet,
        'aside-mobile': isMobile,
        'aside-mobile-open': isMobile && mobileMenuOpen
      }"
    >
      <div class="logo-area" v-if="!isMobile">
        <span v-if="!isTablet">API Pool</span>
        <span v-else class="logo-short">AP</span>
      </div>
      
      <el-scrollbar>
        <el-menu 
          :default-active="$route.path" 
          router
          :collapse="isTablet"
          @select="handleMenuSelect"
        >
          <el-menu-item-group v-if="!isTablet">
            <template #title>管理中心</template>
            <el-menu-item index="/pool">
              <el-icon><DataLine /></el-icon>
              <span>号池管理</span>
            </el-menu-item>
            <el-menu-item index="/proxy">
              <el-icon><Platform /></el-icon>
              <span>代理管理</span>
            </el-menu-item>
            <el-menu-item index="/account">
              <el-icon><User /></el-icon>
              <span>账户管理</span>
            </el-menu-item>
          </el-menu-item-group>
          
          <!-- 平板端折叠菜单 -->
          <template v-else>
            <el-menu-item index="/pool">
              <el-icon><DataLine /></el-icon>
              <span>号池管理</span>
            </el-menu-item>
            <el-menu-item index="/proxy">
              <el-icon><Platform /></el-icon>
              <span>代理管理</span>
            </el-menu-item>
            <el-menu-item index="/account">
              <el-icon><User /></el-icon>
              <span>账户管理</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && mobileMenuOpen" 
      class="mobile-overlay"
      @click="closeMobileMenu"
    ></div>

    <el-container>
      <!-- 桌面端/平板端顶部 -->
      <el-header v-if="!isMobile" class="main-header">
        <div class="toolbar">
          <h1>{{ $route.meta.title }}</h1>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { DataLine, Platform, User, Menu, Refresh } from '@element-plus/icons-vue'

// 响应式状态
const screenWidth = ref(window.innerWidth)
const mobileMenuOpen = ref(false)

// 断点判断
const isMobile = computed(() => screenWidth.value < 768)
const isTablet = computed(() => screenWidth.value >= 768 && screenWidth.value < 1024)
const isDesktop = computed(() => screenWidth.value >= 1024)

// 侧边栏宽度
const sidebarWidth = computed(() => {
  if (isMobile.value) return '280px' // 移动端滑出菜单宽度
  if (isTablet.value) return '64px'  // 平板端折叠宽度
  return '220px'                     // 桌面端完整宽度
})

// 移动端菜单控制
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

// 菜单选择处理
const handleMenuSelect = () => {
  if (isMobile.value) {
    closeMobileMenu()
  }
}

// 移动端操作按钮处理
const handleRefresh = () => {
  // 刷新当前页面数据
  window.location.reload()
}

// 监听屏幕尺寸变化
const handleResize = () => {
  screenWidth.value = window.innerWidth
  // 屏幕变大时自动关闭移动端菜单
  if (screenWidth.value >= 768) {
    mobileMenuOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* ===== 移动端导航 ===== */
.mobile-header {
  height: 56px;
  background: linear-gradient(135deg, #ffffff 0%, #fafbff 100%);
  border-bottom: 1px solid #eaf2f8;
  position: relative;
  z-index: 1001;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(20px);
}

.mobile-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.3), transparent);
}

.mobile-toolbar {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  position: relative;
}

.menu-toggle {
  color: var(--text-primary);
  font-size: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px !important;
  position: relative;
  overflow: hidden;
}

.menu-toggle::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, transparent 70%);
  transition: all 0.3s ease;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.menu-toggle:hover::before {
  width: 40px;
  height: 40px;
}

.menu-toggle:active {
  transform: scale(0.95);
}

.mobile-logo {
  font-size: 1.25rem;
  font-weight: bold;
  background: linear-gradient(135deg, var(--primary-600), var(--primary-400));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(59, 130, 246, 0.1);
  position: relative;
  animation: logoGlow 3s ease-in-out infinite;
}

@keyframes logoGlow {
  0%, 100% { 
    filter: drop-shadow(0 0 5px rgba(59, 130, 246, 0.3)); 
  }
  50% { 
    filter: drop-shadow(0 0 15px rgba(59, 130, 246, 0.5)); 
  }
}

.mobile-actions {
  display: flex;
  gap: 8px;
}

.mobile-actions .el-button {
  color: var(--text-secondary);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px !important;
}

.mobile-actions .el-button:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05)) !important;
  transform: scale(1.1);
  color: var(--primary-600);
}

/* 移动端遮罩层 */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  backdrop-filter: blur(4px);
  animation: overlayFadeIn 0.3s ease;
}

@keyframes overlayFadeIn {
  from {
    opacity: 0;
    backdrop-filter: blur(0px);
  }
  to {
    opacity: 1;
    backdrop-filter: blur(4px);
  }
}

/* ===== 侧边栏样式 ===== */
.main-aside {
  background: #fff;
  border-right: 1px solid #eaf2f8;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1000;
}

/* 平板端折叠状态 */
.main-aside.aside-collapsed {
  overflow: visible;
}

.main-aside.aside-collapsed :deep(.el-menu-item-group__title) {
  display: none;
}

/* 移动端状态 */
.main-aside.aside-mobile {
  position: fixed;
  top: 56px;
  left: 0;
  bottom: 0;
  transform: translateX(-100%);
  z-index: 1000;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.main-aside.aside-mobile-open {
  transform: translateX(0);
}

/* Logo区域 */
.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2ff 50%, #f0f9ff 100%);
  border-bottom: 1px solid #eaf2f8;
  position: relative;
  overflow: hidden;
}

.logo-area::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.6s ease;
}

.logo-area:hover::before {
  left: 100%;
}

.logo-area span {
  background: linear-gradient(135deg, var(--primary-700), var(--primary-500));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
  z-index: 1;
}

.logo-short {
  font-size: 1.2rem;
  background: linear-gradient(135deg, var(--primary-700), var(--primary-400));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: bold;
  position: relative;
}

/* 菜单样式 */
.el-menu {
  border-right: none;
  background-color: transparent;
  --el-menu-item-hover-bg-color: var(--accent-color-light);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, var(--accent-color-light), transparent) !important;
  color: var(--accent-color) !important;
  border-right: 4px solid var(--accent-color);
  position: relative;
  overflow: hidden;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: linear-gradient(180deg, var(--accent-color), var(--primary-400));
  box-shadow: 0 0 12px var(--accent-color);
}

:deep(.el-menu-item.is-active .el-icon) {
  color: var(--accent-color);
  transform: scale(1.1);
  transition: transform 0.2s ease;
}

.el-menu-item {
  color: var(--text-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  margin: 2px 8px;
  border-radius: 8px;
}

.el-menu-item .el-icon {
  color: var(--text-muted);
  transition: all 0.3s ease;
}

.el-menu-item:hover {
  background: linear-gradient(90deg, var(--accent-color-light), rgba(59, 130, 246, 0.03));
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.el-menu-item:hover .el-icon {
  color: var(--accent-color);
  transform: scale(1.05);
}

.el-menu-item-group > :deep(.el-menu-item-group__title) {
  padding-top: 20px;
  font-size: 14px;
  color: var(--text-muted);
}

/* ===== 主要内容区域 ===== */
.main-header {
  background: linear-gradient(135deg, #ffffff 0%, #fafbff 100%);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eaf2f8;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(10px);
  position: relative;
}

.main-header::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--primary-200), transparent);
}

.toolbar h1 {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.main-content {
  padding: 24px;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
    linear-gradient(180deg, var(--app-bg-start) 0%, var(--app-bg-end) 100%);
  overflow-y: auto;
  position: relative;
  min-height: calc(100vh - 60px);
}

.main-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.8) 0%, transparent 100%);
  pointer-events: none;
}

/* ===== 响应式调整 ===== */
@media (max-width: 767px) {
  .main-content {
    padding: 16px 12px;
  }
  
  .el-menu-item {
    padding: 0 20px;
    height: 48px;
    line-height: 48px;
  }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .logo-area {
    font-size: 1rem;
  }
  
  :deep(.el-menu--collapse) {
    width: 64px;
  }
  
  :deep(.el-menu--collapse .el-menu-item) {
    padding: 0 18px !important;
  }
}

@media (min-width: 1024px) {
  .main-content {
    padding: 24px;
  }
}
</style>
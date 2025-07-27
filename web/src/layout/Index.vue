
<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="main-aside">
      <div class="logo-area">
        API Pool
      </div>
      <el-scrollbar>
        <el-menu :default-active="$route.path" router>
          <el-menu-item-group>
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
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="main-header">
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
import { DataLine, Platform, User } from '@element-plus/icons-vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.main-aside {
  background: #fff;
  border-right: 1px solid #eaf2f8;
  display: flex;
  flex-direction: column;
}
.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--text-primary);
  background: linear-gradient(45deg, #f0f9ff, #fff);
}
.el-menu {
  border-right: none;
  background-color: transparent;
  --el-menu-item-hover-bg-color: var(--accent-color-light);
}
:deep(.el-menu-item.is-active) {
    background-color: var(--accent-color-light) !important;
    color: var(--accent-color) !important;
    border-right: 3px solid var(--accent-color);
}
:deep(.el-menu-item.is-active .el-icon) {
    color: var(--accent-color);
}
.el-menu-item {
    color: var(--text-secondary);
}
.el-menu-item .el-icon {
    color: var(--text-muted);
}
.el-menu-item-group > :deep(.el-menu-item-group__title) {
    padding-top: 20px;
    font-size: 14px;
    color: var(--text-muted);
}

.main-header {
  background-color: #ffffff;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eaf2f8;
  height: 60px;
}
.toolbar h1 {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.main-content {
  padding: 24px;
  background: linear-gradient(180deg, var(--app-bg-start) 0%, var(--app-bg-end) 100%);
  overflow-y: auto;
}
</style>
<template>
  <el-container class="layout-container">
    <el-aside width="200px">
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
      <el-header style="text-align: right; font-size: 12px">
        <div class="toolbar">
          <h1>{{ $route.meta.title }}</h1>
        </div>
      </el-header>

      <el-main>
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
.el-aside {
  color: var(--el-text-color-primary);
  background: var(--el-color-primary-light-9);
}
.el-menu {
  border-right: none;
}
.el-header {
  background-color: var(--el-color-primary-light-7);
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.toolbar h1 {
  font-size: 1.2rem;
  margin: 0;
}
</style>

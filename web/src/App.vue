<template>
  <router-view v-slot="{ Component, route }">
    <transition 
      :name="route.meta?.transition || 'fade'" 
      mode="out-in"
      appear
    >
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<style>
/* === 页面过渡动画 === */

/* 淡入淡出过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 滑入滑出过渡 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 缩放过渡 */
.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scale-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.scale-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* 页面加载时的入场动画 */
.fade-appear-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-appear-from {
  opacity: 0;
  transform: translateY(30px) scale(0.98);
}
</style>

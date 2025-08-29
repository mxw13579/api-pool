<template>
  <el-card class="pool-card" shadow="always">
    <template #header>
      <div class="card-header">
        <span class="pool-name">{{ pool.name }}</span>
        <el-dropdown trigger="click">
          <el-button text :icon="MoreFilled" class="more-options-btn"/>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleEdit" :icon="Edit">编辑</el-dropdown-item>
              <el-dropdown-item @click="handleDelete" :icon="Delete" class="delete-item">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </template>

    <div class="card-content">
      <div class="info-item">
        <el-icon>
          <Link/>
        </el-icon>
        <span><strong>Endpoint:</strong> {{ pool.endpoint }}</span>
      </div>
      <div class="info-item">
        <el-icon>
          <Location/>
        </el-icon>
        <span><strong>地址:</strong> {{ pool.address || '未设置' }}</span>
      </div>
      <div class="info-item">
        <el-icon>
          <Odometer/>
        </el-icon>
        <span><strong>延迟:</strong> {{ formatLatency(pool.latency) }}</span>
      </div>
      <el-divider/>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-value">{{ pool.monitoringIntervalTime }}</span>
          <span class="stat-label">监控间隔 (分)</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ pool.minActiveChannels }}</span>
          <span class="stat-label">最小激活数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ pool.maxMonitorRetries }}</span>
          <span class="stat-label">最大重试</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="card-footer">
        <el-button
          class="test-latency-btn"
          @click="handleTestLatency"
          :icon="Odometer"
          :loading="isLatencyTesting"
        >
          延迟测试
        </el-button>
        <el-button
          class="view-channels-btn"
          @click="handleViewChannels"
          :icon="View"
        >
          管理渠道
        </el-button>
        <el-button
          @click="handleShowStatistics"
          :icon="DataLine"
        >
          统计信息
        </el-button>
        <el-button
          @click="handleShowErrorLogs"
          :icon="Warning"
        >
          错误日志
        </el-button>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import {
  MoreFilled,
  View,
  Link,
  Location,
  Edit,
  Delete,
  Odometer,
  DataLine,
  Warning
} from '@element-plus/icons-vue';
import type { PoolEntity } from '@/types';

interface Props {
  pool: PoolEntity;
  isLatencyTesting?: boolean;
}

interface Emits {
  (e: 'edit', pool: PoolEntity): void;
  (e: 'delete', poolId: number): void;
  (e: 'test-latency', pool: PoolEntity): void;
  (e: 'view-channels', pool: PoolEntity): void;
  (e: 'show-statistics', poolId: number): void;
  (e: 'show-error-logs', poolId: number): void;
}

const props = withDefaults(defineProps<Props>(), {
  isLatencyTesting: false
});

const emit = defineEmits<Emits>();

const formatLatency = (latency: number | null | undefined): string => {
  if (latency === undefined || latency === null) return '未测试';
  return latency >= 0 ? `${latency}ms` : '超时';
};

const handleEdit = () => {
  emit('edit', props.pool);
};

const handleDelete = () => {
  emit('delete', props.pool.id);
};

const handleTestLatency = () => {
  emit('test-latency', props.pool);
};

const handleViewChannels = () => {
  emit('view-channels', props.pool);
};

const handleShowStatistics = () => {
  emit('show-statistics', props.pool.id);
};

const handleShowErrorLogs = () => {
  emit('show-error-logs', props.pool.id);
};
</script>

<style scoped>
.pool-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 12px;
  margin-bottom: 24px;
  color: var(--text-secondary);
  transition: transform 0.2s, box-shadow 0.2s;
  --el-card-padding: 0;
}

.pool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px var(--card-shadow);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #eaf6ff;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pool-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.more-options-btn {
  color: #8cb4d2;
}

.more-options-btn:hover {
  color: var(--accent-color);
  background-color: var(--accent-color-light);
}

.delete-item {
  color: var(--el-color-danger);
}

.delete-item:hover {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.card-content {
  padding: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: var(--text-muted);
}

.info-item .el-icon {
  font-size: 16px;
  color: #8cb4d2;
}

.info-item strong {
  color: var(--text-secondary);
  margin-right: 5px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  text-align: center;
  margin-top: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--accent-color);
}

.stat-label {
  font-size: 0.8rem;
  color: #8cb4d2;
}

:deep(.el-card__footer) {
  border-top: 1px solid #eaf6ff;
  padding: 12px 20px;
  background-color: #f8fcff;
}

.card-footer {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.view-channels-btn,
.test-latency-btn {
  flex: 1;
  min-width: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-footer {
    flex-direction: column;
    gap: 8px;
  }
  
  .view-channels-btn,
  .test-latency-btn {
    width: 100%;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

/* 按钮样式 - 黑色背景白色文字 */
.card-footer .el-button {
  background-color: #2d3748 !important;
  color: white !important;
  border: 1px solid #2d3748 !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  padding: 8px 16px !important;
  transition: all 0.2s ease !important;
}

.card-footer .el-button:hover {
  background-color: #1a202c !important;
  border-color: #1a202c !important;
  transform: translateY(-1px) !important;
}

.card-footer .el-button:active {
  transform: translateY(0) !important;
}

.card-footer .el-button .el-icon {
  color: white !important;
}

/* 加载中的按钮保持样式一致 */
.card-footer .el-button.is-loading {
  background-color: #4a5568 !important;
  color: white !important;
}
</style>
<template>
  <div class="pool-management-container page-container">
    <!-- 头部工具栏 -->
    <div class="header-toolbar">
      <div class="page-title">
        <p class="subtitle">统一管理API服务池，实时监控运行状态</p>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" @click="handleAdd" :icon="Plus" size="large">
          新增号池
        </el-button>
        <el-button type="success" @click="handleBatchAddChannel" :icon="Ticket" size="large">
          批量新增渠道
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" size="40">
        <Loading/>
      </el-icon>
      <p>正在加载号池数据...</p>
    </div>

    <!-- 号池网格 -->
    <el-row :gutter="20" v-else class="pool-grid">
      <el-col
        :xs="24"
        :sm="12"
        :md="12"
        :lg="8"
        :xl="6"
        v-for="pool in pools"
        :key="pool.id"
        class="pool-col"
      >
        <PoolCard
          :pool="pool"
          :is-latency-testing="latencyTesting[pool.id]"
          @edit="handleEdit"
          @delete="handleDelete"
          @test-latency="handleTestLatency"
          @view-channels="handleViewChannels"
          @show-statistics="handleShowStatistics"
          @show-error-logs="handleShowErrorLogs"
        />
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && pools.length === 0"
      description="暂无号池数据"
      class="empty-state"
    >
      <el-button type="primary" @click="handleAdd" :icon="Plus">创建第一个号池</el-button>
    </el-empty>

    <!-- 渠道详情对话框 -->
    <ChannelDetailDialog v-model:visible="channelDialogVisible" :poolId="currentPoolId"/>

    <!-- 对话框管理器 -->
    <DialogManager
      v-model:pool-dialog-visible="dialogVisible"
      :pool-dialog-title="dialogTitle"
      :pool-form="form"
      v-model:statistics-dialog-visible="statisticsDialogVisible"
      :current-statistics="currentStatistics"
      v-model:error-log-dialog-visible="errorLogDialogVisible"
      :current-error-logs="currentErrorLogs"
      v-model:batch-add-dialog-visible="batchAddDialogVisible"
      :batch-channel-form="batchChannelForm"
      v-model:batch-result-dialog-visible="batchResultDialogVisible"
      :batch-result-list="batchResultList"
      :submitting="submitting"
      @pool-submit="handleSubmit"
      @batch-channel-submit="handleBatchSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
  getPoolList,
  addPool,
  updatePool,
  deletePool,
  batchAddChannelToAll,
  testPoolLatency,
  getPoolStatistics,
  getErrorLogs
} from '@/api/pool';
import type { PoolEntity, Channel } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Ticket, Loading } from '@element-plus/icons-vue';
import ChannelDetailDialog from '@/components/ChannelDetailDialog.vue';
import PoolCard from '@/components/PoolCard.vue';
import DialogManager from '@/components/DialogManager.vue';

// 接口定义
interface AccountStats {
  today: number;
  yesterday: number;
  thisWeek: number;
  thisMonth: number;
  total: number;
}

interface PoolStatistics {
  accountStats: AccountStats;
}

// 响应式状态
const pools = ref<PoolEntity[]>([]);
const loading = ref(false);
const submitting = ref(false);
const latencyTesting = ref<{ [key: number]: boolean }>({});

// 号池表单相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = ref<Partial<PoolEntity>>({});

// 渠道对话框相关
const channelDialogVisible = ref(false);
const currentPoolId = ref<number | null>(null);

// 统计信息相关
const statisticsDialogVisible = ref(false);
const currentStatistics = ref<PoolStatistics | null>(null);

// 错误日志相关
const errorLogDialogVisible = ref(false);
const currentErrorLogs = ref<any[]>([]);

// 批量新增相关
const batchAddDialogVisible = ref(false);
const batchChannelForm = ref<Partial<Channel>>({});
const batchResultDialogVisible = ref(false);
const batchResultList = ref<string[]>([]);


// 默认数据
const defaultChannel: Partial<Channel> = {
  name: '',
  type: 41,
  status: 2,
  key: '',
  baseUrl: '',
  models: 'gemini-2.5-flash,gemini-2.5-pro',
  group: 'default',
  priority: 0,
  weight: 0,
  autoBan: 1,
  modelMapping: '{}',
  tag: '',
  setting: "",
  proxy: 1,
  paramOverride: '',
  other: '{"default": "global"}',
  otherInfo: '',
};

const defaultPool: Partial<PoolEntity> = {
  name: '',
  endpoint: '',
  username: '',
  password: '',
  address: '',
  monitoringIntervalTime: 5,
  minActiveChannels: 1,
  maxMonitorRetries: 5,
};

// 主要方法
const fetchPools = async () => {
  loading.value = true;
  try {
    pools.value = await getPoolList();
  } catch (error) {
    console.error('获取号池列表失败:', error);
    ElMessage.error('获取号池列表失败');
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  form.value = { ...defaultPool };
  dialogTitle.value = '新增号池';
  dialogVisible.value = true;
};

const handleEdit = (pool: PoolEntity) => {
  form.value = { ...pool };
  dialogTitle.value = '编辑号池';
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  submitting.value = true;
  try {
    const apiCall = form.value.id ? updatePool : addPool;
    await apiCall(form.value as any);
    ElMessage.success(form.value.id ? '更新成功' : '新增成功');
    dialogVisible.value = false;
    await fetchPools();
  } catch (error) {
    console.error('提交失败:', error);
    ElMessage.error('操作失败');
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (poolId: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个号池吗? 这将是一个不可逆转的操作。',
      '严重警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        buttonSize: 'default',
      }
    );

    await deletePool(poolId);
    ElMessage.success('删除成功');
    await fetchPools();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

const handleViewChannels = (pool: PoolEntity) => {
  currentPoolId.value = pool.id;
  channelDialogVisible.value = true;
};

const handleShowStatistics = async (poolId: number) => {
  try {
    const stats = await getPoolStatistics(poolId);
    currentStatistics.value = stats;
    statisticsDialogVisible.value = true;
  } catch (error) {
    console.error('获取统计信息失败:', error);
    ElMessage.error('获取统计信息失败');
  }
};

const handleShowErrorLogs = async (poolId: number) => {
  try {
    const logs = await getErrorLogs(poolId);
    currentErrorLogs.value = logs || [];
    errorLogDialogVisible.value = true;
  } catch (error) {
    console.error('获取错误日志失败:', error);
    ElMessage.error('获取错误日志失败');
  }
};

const handleTestLatency = async (pool: PoolEntity) => {
  latencyTesting.value[pool.id] = true;
  try {
    const latency = await testPoolLatency(pool.id);
    const targetPool = pools.value.find(p => p.id === pool.id);
    if (targetPool) {
      targetPool.latency = latency;
    }
    ElMessage.success(`延迟测试完成: ${latency > 0 ? `${latency}ms` : '超时'}`);
  } catch (error) {
    console.error('延迟测试失败:', error);
    ElMessage.error('延迟测试失败');
  } finally {
    latencyTesting.value[pool.id] = false;
  }
};

const handleBatchAddChannel = () => {
  batchChannelForm.value = { ...defaultChannel };
  batchAddDialogVisible.value = true;
};

const handleBatchSubmit = async () => {
  if (!batchChannelForm.value.name || !batchChannelForm.value.key) {
    ElMessage.error('渠道名称和密钥为必填项');
    return;
  }

  submitting.value = true;
  try {
    const res = await batchAddChannelToAll(batchChannelForm.value);
    if (Array.isArray(res)) {
      batchResultList.value = res;
      batchResultDialogVisible.value = true;
      batchAddDialogVisible.value = false;
    } else {
      ElMessage.error('批量新增渠道失败，返回数据格式异常');
    }
  } catch (error: any) {
    console.error('批量新增失败:', error);
    const errorMessage = error.response?.data?.msg || error.msg || '批量新增渠道失败';
    ElMessage.error(errorMessage);
  } finally {
    submitting.value = false;
  }
};

// 生命周期
onMounted(fetchPools);
</script>

<style scoped>
.pool-management-container {
  min-height: calc(100vh - 50px);
}

.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}

.page-title .subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 500;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* 取消相邻按钮的默认左外边距，避免与 gap/纵向布局叠加 */
.toolbar-actions .el-button + .el-button {
  margin-left: 0;
}

.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: var(--text-muted);
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 12px;
}

/* 优化号池网格布局 */
.pool-grid {
  margin: 0 !important;
}

.pool-col {
  margin-bottom: 20px;
  display: flex;
}

.pool-col :deep(.pool-card) {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .header-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .toolbar-actions {
    justify-content: stretch;
  }

  .toolbar-actions .el-button {
    flex: 1;
  }

  .pool-col {
    margin-bottom: 16px;
  }
}

@media (min-width: 1200px) {
  .pool-col {
    margin-bottom: 24px;
  }
}

@media (min-width: 1600px) {
  .pool-grid .el-col {
    max-width: 20%; /* 5列布局 */
    flex: 0 0 20%;
  }
}

.loading-state .el-icon {
  margin-bottom: 16px;
  color: var(--accent-color);
}

.loading-state p {
  font-size: 14px;
  margin: 0;
}

.pool-grid {
  margin-bottom: 24px;
}

.empty-state {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 12px;
  padding: 48px 24px;
}

.empty-state :deep(.el-empty__description) {
  color: var(--text-muted);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .page-title {
    text-align: center;
  }

  .toolbar-actions {
    flex-direction: column;
    gap: 8px;
  }

  .pool-grid {
    margin: 0 -12px;
  }

  .pool-grid :deep(.el-col) {
    padding: 0 12px !important;
  }
}

@media (max-width: 480px) {
  .page-title .subtitle {
    font-size: 14px;
  }

  .pool-grid {
    margin: 0 -8px;
  }

  .pool-grid :deep(.el-col) {
    padding: 0 8px !important;
  }
}

/* 改进加载动画 */
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}

.loading-state .el-icon.is-loading {
  animation: pulse 1.5s ease-in-out infinite;
}
</style>

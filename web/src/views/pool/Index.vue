<template>
  <div class="pool-management-container">
    <div class="header-toolbar">
      <el-button type="primary" @click="handleAdd" :icon="Plus" size="large">新增号池</el-button>
      <el-button type="success" @click="handleBatchAddChannel" :icon="Ticket" size="large">批量新增渠道</el-button>
    </div>

    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" size="40"><Loading /></el-icon>
      <p>正在加载号池数据...</p>
    </div>

    <el-row :gutter="24" v-else class="pool-grid">
      <el-col :xs="24" :sm="12" :md="8" v-for="pool in pools" :key="pool.id">
        <el-card class="pool-card" shadow="always">
          <template #header>
            <div class="card-header">
              <span class="pool-name">{{ pool.name }}</span>
              <el-dropdown trigger="click">
                <el-button text :icon="MoreFilled" class="more-options-btn" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(pool)" :icon="Edit">编辑</el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(pool.id)" :icon="Delete" class="delete-item">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>

          <div class="card-content">
            <div class="info-item">
              <el-icon><Link /></el-icon>
              <span><strong>Endpoint:</strong> {{ pool.endpoint }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span><strong>地址:</strong> {{ pool.address || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Odometer /></el-icon>
              <span><strong>延迟:</strong> {{ pool.latency === undefined || pool.latency === null ? '未测试' : pool.latency >= 0 ? `${pool.latency}ms` : '超时' }}</span>
            </div>
            <el-divider />
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
                @click="handleTestLatency(pool)"
                :icon="Odometer"
                type="warning"
                plain
                round
                :loading="latencyTesting[pool.id]"
              >
                延迟测试
              </el-button>
              <el-button
                class="view-channels-btn"
                @click="handleViewChannels(pool)"
                :icon="View"
                type="primary"
                plain
                round
              >
                管理渠道
              </el-button>
              <el-button
                @click="handleShowStatistics(pool.id)"
                :icon="DataLine"
                type="info"
                plain
                round
              >
                统计信息
              </el-button>
              <el-button
                @click="handleShowErrorLogs(pool.id)"
                :icon="Warning"
                type="danger"
                plain
                round
              >
                错误日志
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- Dialogs remain unchanged -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" custom-class="form-dialog">
      <el-form :model="form" ref="formRef" label-width="120px">
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Endpoint" prop="endpoint" required>
          <el-input v-model="form.endpoint" />
        </el-form-item>
        <el-form-item label="用户名" prop="username" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!form.id">
          <el-input v-model="form.password" type="password" show-password placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="监控间隔(分)" prop="monitoringIntervalTime">
          <el-input-number v-model="form.monitoringIntervalTime" :min="1" placeholder="例如: 5" />
        </el-form-item>
        <el-form-item label="最小激活数" prop="minActiveChannels">
          <el-input-number v-model="form.minActiveChannels" :min="1" placeholder="例如: 1" />
        </el-form-item>
        <el-form-item label="最大监控重试" prop="maxMonitorRetries">
          <el-input-number v-model="form.maxMonitorRetries" :min="0" placeholder="例如: 5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <ChannelDetailDialog v-model:visible="channelDialogVisible" :poolId="currentPoolId" />

    <el-dialog v-model="statisticsDialogVisible" title="号池统计信息" width="600px">
      <div v-if="currentStatistics">
        <h3>账号添加统计</h3>
        <el-table :data="[currentStatistics.accountStats]">
          <el-table-column prop="today" label="今日" />
          <el-table-column prop="yesterday" label="昨日" />
          <el-table-column prop="thisWeek" label="本周" />
          <el-table-column prop="thisMonth" label="本月" />
          <el-table-column prop="total" label="共计" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="statisticsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="errorLogDialogVisible" title="错误日志" width="1200px">
      <el-table :data="currentErrorLogs" height="800" empty-text="暂无错误日志">
        <el-table-column prop="channelName" label="渠道名称" width="150" />
        <el-table-column prop="errorMessage" label="错误信息" />
        <el-table-column prop="createdAt" label="时间" width="180"   :formatter="formatTimestamp"/>
      </el-table>
      <template #footer>
        <el-button @click="errorLogDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
        v-model="batchAddDialogVisible"
        title="批量全号池新增渠道"
        width="60%"
        :close-on-click-modal="false"
         custom-class="form-dialog"
    >
      <el-form
          v-if="batchChannelForm"
          :model="batchChannelForm"
          label-width="120px"
      >
        <el-form-item label="名称" required>
          <el-input v-model="batchChannelForm.name" placeholder="请输入渠道名称" />
        </el-form-item>
        <el-form-item label="类型">
        <el-select v-model="batchChannelForm.type" placeholder="请选择类型">
          <el-option v-for="(name, code) in channelTypeMap" :key="code" :label="name" :value="Number(code)" />
        </el-select>
      </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="batchChannelForm.group" placeholder="请输入分组名称, e.g. default" />
        </el-form-item>
        <el-form-item label="密钥" required>
          <el-input v-model="batchChannelForm.key" type="textarea" :rows="8" show-password placeholder="请输入渠道密钥" />
        </el-form-item>
        <el-form-item label="基础URL">
          <el-input v-model="batchChannelForm.baseUrl" placeholder="e.g. https://api.openai.com" />
        </el-form-item>
        <el-form-item label="支持的模型">
          <el-input v-model="batchChannelForm.models" placeholder="逗号分隔, e.g. gpt-3.5-turbo,gpt-4" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="batchChannelForm.priority" :min="0" />
        </el-form-item>
        <el-form-item label="权重">
          <el-input-number v-model="batchChannelForm.weight" :min="0" />
        </el-form-item>
        <el-form-item label="自动封禁">
          <el-switch v-model="batchChannelForm.autoBan" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="batchChannelForm.tag" placeholder="请输入渠道标签" />
        </el-form-item>
        <el-form-item label="设置信息">
          <el-input v-model="batchChannelForm.setting" type="textarea" :rows="3" placeholder="JSON格式的设置信息" />
        </el-form-item>
        <el-form-item label="参数覆盖">
          <el-input v-model="batchChannelForm.paramOverride" type="textarea" :rows="3" placeholder="JSON格式的参数覆盖信息" />
        </el-form-item>
        <el-form-item label="部署地区">
          <el-input v-model="batchChannelForm.other" type="textarea" :rows="3" placeholder="部署地区" />
        </el-form-item>
        <el-form-item label="代理设置">
          <el-select v-model="batchChannelForm.proxy" placeholder="请选择类型">
            <el-option v-for="(name, code) in proxyTypeMap" :key="code" :label="name" :value="Number(code)" />
          </el-select>
        </el-form-item>
        <el-form-item label="其他附加信息">
          <el-input v-model="batchChannelForm.otherInfo" type="textarea" :rows="2" placeholder="其他附加信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchAddDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog
        v-model="batchResultDialogVisible"
        title="批量新增渠道结果"
        width="600px"
        :close-on-click-modal="false"
         custom-class="form-dialog"
    >
      <div style="margin-bottom: 16px;">
        共计 {{ totalCount }} 条渠道，成功 {{ successCount }} 条，失败 {{ failCount }} 条
      </div>
      <div style="max-height: 400px; overflow-y: auto;">
        <ul>
          <li v-for="(msg, idx) in batchResultList" :key="idx" style="margin-bottom: 8px;">
            {{ msg }}
          </li>
        </ul>
      </div>
      <template #footer>
        <el-button type="primary" @click="batchResultDialogVisible = false">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
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
import { Plus, Ticket, MoreFilled, View, Loading, Link, Location, Edit, Delete, Odometer, DataLine, Warning } from '@element-plus/icons-vue';
import ChannelDetailDialog from '@/components/ChannelDetailDialog.vue';
import { channelTypeMap, proxyTypeMap } from "@/utils/maps.ts";

const pools = ref<PoolEntity[]>([]);
const loading = ref(false);
const latencyTesting = ref<{ [key: number]: boolean }>({});
const dialogVisible = ref(false);
const dialogTitle = ref('');
const statisticsDialogVisible = ref(false);
const currentStatistics = ref(null);
const errorLogDialogVisible = ref(false);
const currentErrorLogs = ref<any[]>([]);
const form = ref<Partial<PoolEntity>>({});
const formRef = ref();

const channelDialogVisible = ref(false);
const currentPoolId = ref<number | null>(null);
const batchResultDialogVisible = ref(false);
const batchResultList = ref<string[]>([]);

const totalCount = computed(() => batchResultList.value.length);
const successCount = computed(() =>
    batchResultList.value.filter(msg => msg.includes('成功')).length
);
const failCount = computed(() =>
    batchResultList.value.filter(msg => msg.includes('失败') || msg.includes('异常')).length
);

const batchAddDialogVisible = ref(false);
const batchChannelForm = ref<Partial<Channel>>({});


const defaultChannel: Partial<Channel> = {
  name: '',
  type: 41,
  status: 2,
  key: '',
  baseUrl: '',
  models: 'gemini-2.5-flash,gemini-2.5-pro,gemini-2.5-pro-preview-05-06,gemini-2.5-pro-preview-06-05',
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

const handleBatchAddChannel = () => {
  batchChannelForm.value = { ...defaultChannel };
  batchAddDialogVisible.value = true;
};

const handleBatchSubmit = async () => {
  if (!batchChannelForm.value.name || !batchChannelForm.value.key) {
    ElMessage.error('渠道名称和密钥为必填项');
    return;
  }
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
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg);
    } else if (error.msg) {
      ElMessage.error(error.msg);
    } else {
      ElMessage.error('批量新增渠道失败');
    }
    console.error(error);
  }
};



const fetchPools = async () => {
  loading.value = true;
  try {
    pools.value = await getPoolList();
  } catch(e) {
    console.error(e)
  } finally {
    loading.value = false;
  }
};

onMounted(fetchPools);

const handleAdd = () => {
  form.value = { ...defaultPool };
  dialogTitle.value = '新增号池';
  dialogVisible.value = true;
};

const handleEdit = (row: PoolEntity) => {
  form.value = { ...row };
  dialogTitle.value = '编辑号池';
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  try {
    const apiCall = form.value.id ? updatePool : addPool;
    await apiCall(form.value as any);
    ElMessage.success(form.value.id ? '更新成功' : '新增成功');
    dialogVisible.value = false;
    fetchPools();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这个号池吗? 这将是一个不可逆转的操作。', '严重警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
    buttonSize: 'default',
  }).then(async () => {
    await deletePool(id);
    ElMessage.success('删除成功');
    fetchPools();
  }).catch(() => {});
};

const handleViewChannels = (row: PoolEntity) => {
  currentPoolId.value = row.id;
  channelDialogVisible.value = true;
}

const handleShowStatistics = async (poolId) => {
  try {
    const stats = await getPoolStatistics(poolId);
    currentStatistics.value = stats;
    statisticsDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取统计信息失败');
  }
};

const handleShowErrorLogs = async (poolId) => {
  try {
    const stats = await getErrorLogs(poolId);
    console.log(stats)
    currentErrorLogs.value = stats || [];
    errorLogDialogVisible.value = true;
  } catch (error) {
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
    ElMessage.error('延迟测试失败');
  } finally {
    latencyTesting.value[pool.id] = false;
  }
};

/**
 * 将秒级时间戳转为 YYYY-MM-DD HH:mm:ss 格式
 */
function formatTimestamp(row: any) {
  if (!row.createdAt) return '';
  const date = new Date(row.createdAt * 1000);
  const Y = date.getFullYear();
  const M = String(date.getMonth() + 1).padStart(2, '0');
  const D = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const m = String(date.getMinutes()).padStart(2, '0');
  const s = String(date.getSeconds()).padStart(2, '0');
  return `${Y}-${M}-${D} ${h}:${m}:${s}`;
}

</script>

<style>
/* Reverting dialog styles to default light theme */
.el-dialog.form-dialog {
  --el-dialog-bg-color: var(--el-color-white);
  border-radius: 10px;
}
.form-dialog .el-dialog__title {
  color: var(--el-text-color-primary);
}
.form-dialog .el-form-item__label {
  color: var(--el-text-color-regular);
}
.form-dialog .el-input__wrapper, .form-dialog .el-textarea__inner {
  background-color: var(--el-color-white) !important;
  box-shadow: 0 0 0 1px var(--el-input-border-color,var(--el-border-color)) inset !important;
  color: var(--el-text-color-primary);
}
.form-dialog .el-input__inner::placeholder {
  color: var(--el-text-color-placeholder);
}
.form-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}
</style>

<style scoped>
.pool-management-container {
  padding: 24px;
  background: linear-gradient(180deg, #f0f9ff 0%, #e0f2ff 100%);
  min-height: calc(100vh - 50px);
}

.header-toolbar {
  margin-bottom: 24px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #5f99be;
}
.loading-state .el-icon {
    margin-bottom: 10px;
}

.pool-card {
  background: #ffffff;
  border: 1px solid #d3eafc;
  border-radius: 12px;
  margin-bottom: 24px;
  color: #304455;
  transition: transform 0.2s, box-shadow 0.2s;
  --el-card-padding: 0;
}

.pool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 123, 255, 0.15);
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
  color: #005a9e;
}

.more-options-btn {
  color: #8cb4d2;
}
.more-options-btn:hover {
  color: #007bff;
  background-color: rgba(0, 123, 255, 0.05);
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
  color: #5a788a;
}
.info-item .el-icon {
  font-size: 16px;
  color: #8cb4d2;
}
.info-item strong {
  color: #304455;
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
  color: #007bff;
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

.view-channels-btn {
  width: 100%;
}

.test-latency-btn {
    width: 100%;
}

.card-footer {
    display: flex;
    gap: 10px;
}
</style>

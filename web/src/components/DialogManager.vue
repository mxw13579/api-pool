<template>
  <!-- 号池表单对话框 -->
  <el-dialog v-model="poolDialogVisible" :title="poolDialogTitle" width="500px" custom-class="form-dialog">
    <el-form :model="poolForm" ref="poolFormRef" label-width="120px" :rules="poolFormRules">
      <el-form-item label="名称" prop="name">
        <el-input v-model="poolForm.name" placeholder="请输入号池名称"/>
      </el-form-item>
      <el-form-item label="Endpoint" prop="endpoint">
        <el-input v-model="poolForm.endpoint" placeholder="请输入API端点地址"/>
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="poolForm.username" placeholder="请输入用户名"/>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input 
          v-model="poolForm.password" 
          type="password" 
          show-password 
          :placeholder="poolForm.id ? '不修改请留空' : '请输入密码'"
        />
      </el-form-item>
      <el-form-item label="地址" prop="address">
        <el-input v-model="poolForm.address" placeholder="请输入地址（可选）"/>
      </el-form-item>
      <el-form-item label="监控间隔(分)" prop="monitoringIntervalTime">
        <el-input-number v-model="poolForm.monitoringIntervalTime" :min="1" placeholder="例如: 5" class="w-full"/>
      </el-form-item>
      <el-form-item label="最小激活数" prop="minActiveChannels">
        <el-input-number v-model="poolForm.minActiveChannels" :min="1" placeholder="例如: 1" class="w-full"/>
      </el-form-item>
      <el-form-item label="最大监控重试" prop="maxMonitorRetries">
        <el-input-number v-model="poolForm.maxMonitorRetries" :min="0" placeholder="例如: 5" class="w-full"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="poolDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handlePoolSubmit">
        {{ submitting ? '提交中' : '确定' }}
      </el-button>
    </template>
  </el-dialog>

  <!-- 统计信息对话框 -->
  <el-dialog v-model="statisticsDialogVisible" title="号池统计信息" width="600px">
    <div v-if="currentStatistics">
      <h3 class="statistics-title">账号添加统计</h3>
      <el-table :data="[currentStatistics.accountStats]" class="statistics-table">
        <el-table-column prop="today" label="今日" align="center"/>
        <el-table-column prop="yesterday" label="昨日" align="center"/>
        <el-table-column prop="thisWeek" label="本周" align="center"/>
        <el-table-column prop="thisMonth" label="本月" align="center"/>
        <el-table-column prop="total" label="共计" align="center"/>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="statisticsDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 错误日志对话框 -->
  <el-dialog v-model="errorLogDialogVisible" title="错误日志" width="1200px">
    <el-table :data="currentErrorLogs" height="400" empty-text="暂无错误日志">
      <el-table-column prop="channelName" label="渠道名称" width="150"/>
      <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip/>
      <el-table-column prop="createdAt" label="时间" width="180" :formatter="formatTimestamp"/>
    </el-table>
    <template #footer>
      <el-button @click="errorLogDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 批量新增渠道对话框 -->
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
      :rules="batchChannelRules"
      ref="batchChannelFormRef"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="batchChannelForm.name" placeholder="请输入渠道名称"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型">
            <el-select v-model="batchChannelForm.type" placeholder="请选择类型" class="w-full">
              <el-option v-for="(name, code) in channelTypeMap" :key="code" :label="name" :value="Number(code)"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分组">
            <el-input v-model="batchChannelForm.group" placeholder="例如: default"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="基础URL">
            <el-input v-model="batchChannelForm.baseUrl" placeholder="例如: https://api.openai.com"/>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="密钥" prop="key">
        <el-input 
          v-model="batchChannelForm.key" 
          type="textarea" 
          :rows="4" 
          show-password
          placeholder="请输入渠道密钥"
        />
      </el-form-item>
      
      <el-form-item label="支持的模型">
        <el-input 
          v-model="batchChannelForm.models" 
          placeholder="逗号分隔，例如: gpt-3.5-turbo,gpt-4"
        />
      </el-form-item>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="优先级">
            <el-input-number v-model="batchChannelForm.priority" :min="0" class="w-full"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="权重">
            <el-input-number v-model="batchChannelForm.weight" :min="0" class="w-full"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="自动封禁">
            <el-switch v-model="batchChannelForm.autoBan" :active-value="1" :inactive-value="0"/>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="标签">
            <el-input v-model="batchChannelForm.tag" placeholder="请输入渠道标签"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="代理设置">
            <el-select v-model="batchChannelForm.proxy" placeholder="请选择代理类型" class="w-full">
              <el-option v-for="(name, code) in proxyTypeMap" :key="code" :label="name" :value="Number(code)"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="设置信息">
        <el-input 
          v-model="batchChannelForm.setting" 
          type="textarea" 
          :rows="2" 
          placeholder="JSON格式的设置信息"
        />
      </el-form-item>
      
      <el-form-item label="参数覆盖">
        <el-input 
          v-model="batchChannelForm.paramOverride" 
          type="textarea" 
          :rows="2"
          placeholder="JSON格式的参数覆盖信息"
        />
      </el-form-item>
      
      <el-form-item label="部署地区">
        <el-input 
          v-model="batchChannelForm.other" 
          type="textarea" 
          :rows="2" 
          placeholder="部署地区信息"
        />
      </el-form-item>
      
      <el-form-item label="其他附加信息">
        <el-input 
          v-model="batchChannelForm.otherInfo" 
          type="textarea" 
          :rows="2" 
          placeholder="其他附加信息"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="batchAddDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleBatchChannelSubmit">
          {{ submitting ? '提交中' : '确定' }}
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 批量新增结果对话框 -->
  <el-dialog
    v-model="batchResultDialogVisible"
    title="批量新增渠道结果"
    width="600px"
    :close-on-click-modal="false"
    custom-class="form-dialog"
  >
    <div class="batch-result-summary">
      <el-tag type="info" size="large">共计: {{ totalCount }}</el-tag>
      <el-tag type="success" size="large">成功: {{ successCount }}</el-tag>
      <el-tag type="danger" size="large">失败: {{ failCount }}</el-tag>
    </div>
    
    <div class="batch-result-details">
      <el-scrollbar height="300px">
        <ul class="result-list">
          <li 
            v-for="(msg, idx) in batchResultList" 
            :key="idx" 
            :class="getResultItemClass(msg)"
            class="result-item"
          >
            <el-icon v-if="msg.includes('成功')" color="var(--el-color-success)">
              <SuccessFilled />
            </el-icon>
            <el-icon v-else color="var(--el-color-danger)">
              <CircleCloseFilled />
            </el-icon>
            <span>{{ msg }}</span>
          </li>
        </ul>
      </el-scrollbar>
    </div>
    
    <template #footer>
      <el-button type="primary" @click="batchResultDialogVisible = false">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElForm, ElMessage } from 'element-plus';
import { SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue';
import type { PoolEntity, Channel } from '@/types';
import { channelTypeMap, proxyTypeMap } from "@/utils/maps";

interface Props {
  poolDialogVisible: boolean;
  poolDialogTitle: string;
  poolForm: Partial<PoolEntity>;
  statisticsDialogVisible: boolean;
  currentStatistics: any;
  errorLogDialogVisible: boolean;
  currentErrorLogs: any[];
  batchAddDialogVisible: boolean;
  batchChannelForm: Partial<Channel>;
  batchResultDialogVisible: boolean;
  batchResultList: string[];
  submitting?: boolean;
}

interface Emits {
  (e: 'update:poolDialogVisible', value: boolean): void;
  (e: 'update:statisticsDialogVisible', value: boolean): void;
  (e: 'update:errorLogDialogVisible', value: boolean): void;
  (e: 'update:batchAddDialogVisible', value: boolean): void;
  (e: 'update:batchResultDialogVisible', value: boolean): void;
  (e: 'pool-submit'): void;
  (e: 'batch-channel-submit'): void;
}

const props = withDefaults(defineProps<Props>(), {
  submitting: false
});

const emit = defineEmits<Emits>();

// 表单引用
const poolFormRef = ref<InstanceType<typeof ElForm>>();
const batchChannelFormRef = ref<InstanceType<typeof ElForm>>();

// 表单验证规则
const poolFormRules = {
  name: [{ required: true, message: '请输入号池名称', trigger: 'blur' }],
  endpoint: [{ required: true, message: '请输入API端点地址', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { 
      required: !props.poolForm.id, 
      message: '请输入密码', 
      trigger: 'blur' 
    }
  ],
  monitoringIntervalTime: [{ required: true, message: '请输入监控间隔时间', trigger: 'blur' }],
  minActiveChannels: [{ required: true, message: '请输入最小激活数', trigger: 'blur' }],
  maxMonitorRetries: [{ required: true, message: '请输入最大监控重试次数', trigger: 'blur' }]
};

const batchChannelRules = {
  name: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  key: [{ required: true, message: '请输入渠道密钥', trigger: 'blur' }]
};

// 计算属性
const totalCount = computed(() => props.batchResultList.length);
const successCount = computed(() => 
  props.batchResultList.filter(msg => msg.includes('成功')).length
);
const failCount = computed(() => 
  props.batchResultList.filter(msg => msg.includes('失败') || msg.includes('异常')).length
);

// 响应式属性
const poolDialogVisible = computed({
  get: () => props.poolDialogVisible,
  set: (value) => emit('update:poolDialogVisible', value)
});

const statisticsDialogVisible = computed({
  get: () => props.statisticsDialogVisible,
  set: (value) => emit('update:statisticsDialogVisible', value)
});

const errorLogDialogVisible = computed({
  get: () => props.errorLogDialogVisible,
  set: (value) => emit('update:errorLogDialogVisible', value)
});

const batchAddDialogVisible = computed({
  get: () => props.batchAddDialogVisible,
  set: (value) => emit('update:batchAddDialogVisible', value)
});

const batchResultDialogVisible = computed({
  get: () => props.batchResultDialogVisible,
  set: (value) => emit('update:batchResultDialogVisible', value)
});

// 方法
const handlePoolSubmit = async () => {
  if (!poolFormRef.value) return;
  
  try {
    await poolFormRef.value.validate();
    emit('pool-submit');
  } catch {
    ElMessage.error('请检查表单输入');
  }
};

const handleBatchChannelSubmit = async () => {
  if (!batchChannelFormRef.value) return;
  
  try {
    await batchChannelFormRef.value.validate();
    emit('batch-channel-submit');
  } catch {
    ElMessage.error('请检查表单输入');
  }
};

const formatTimestamp = (row: any): string => {
  if (!row.createdAt) return '';
  const date = new Date(row.createdAt * 1000);
  const Y = date.getFullYear();
  const M = String(date.getMonth() + 1).padStart(2, '0');
  const D = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const m = String(date.getMinutes()).padStart(2, '0');
  const s = String(date.getSeconds()).padStart(2, '0');
  return `${Y}-${M}-${D} ${h}:${m}:${s}`;
};

const getResultItemClass = (msg: string): string => {
  if (msg.includes('成功')) return 'success-item';
  if (msg.includes('失败') || msg.includes('异常')) return 'error-item';
  return '';
};
</script>

<style>
/* 全局对话框样式 */
.el-dialog.form-dialog {
  --el-dialog-bg-color: var(--card-bg);
  border-radius: 12px;
  border: 1px solid var(--card-border);
}

.form-dialog .el-dialog__title {
  color: var(--text-primary);
  font-weight: 600;
}

.form-dialog .el-form-item__label {
  color: var(--text-secondary);
  font-weight: 500;
}

.form-dialog .el-input__wrapper, 
.form-dialog .el-textarea__inner {
  background-color: var(--card-bg) !important;
  box-shadow: 0 0 0 1px var(--card-border) inset !important;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-dialog .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--accent-color) inset !important;
}

.form-dialog .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px var(--accent-color) inset !important;
}

.form-dialog .el-input__inner::placeholder {
  color: var(--text-muted);
}

.form-dialog .el-dialog__body {
  padding: 20px 24px;
}

.w-full {
  width: 100%;
}
</style>

<style scoped>
.statistics-title {
  color: var(--text-primary);
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.statistics-table {
  border-radius: 8px;
  overflow: hidden;
}

.batch-result-summary {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  justify-content: center;
}

.batch-result-details {
  border: 1px solid var(--card-border);
  border-radius: 8px;
  background: var(--card-bg);
}

.result-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--card-border);
  font-size: 14px;
  line-height: 1.4;
}

.result-item:last-child {
  border-bottom: none;
}

.success-item {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
}

.error-item {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger-dark-2);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .batch-result-summary {
    flex-direction: column;
    align-items: stretch;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
}
</style>
<template>
  <!-- 渠道列表主对话框 -->
  <el-dialog
      :model-value="visible"
      title="渠道列表"
      width="90%"
      :close-on-click-modal="false"
      class="channel-list-dialog"
      @close="$emit('update:visible', false)"
  >
    <!-- 操作区域：添加渠道按钮 -->
    <div class="channel-header">
      <el-button type="primary" @click="openAddDialog" size="default">
        <el-icon><Plus /></el-icon>
        添加渠道
      </el-button>
    </div>

    <!-- 渠道数据表格：移动端更友好（可横向滚动 + 精简列） -->
    <div class="table-responsive">
      <el-table :data="channels" v-loading="loading" border>
        <el-table-column prop="id" label="ID" :width="isMobile ? 60 : 80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型">
          <template #default="{ row }">
            {{ getChannelTypeName(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag effect="plain">
              {{ getChannelStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="group" label="分组" v-if="!isMobile" />
        <el-table-column label="已用配额" v-if="!isMobile">
          <template #default="{ row }">
            {{ row.usedQuota ? (Math.floor(row.usedQuota / 500000 * 100) / 100) : 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" v-if="!isMobile" />
        <el-table-column prop="responseTime" label="响应时间(ms)" v-if="!isMobile" />
        <el-table-column label="操作" fixed="right" :width="isMobile ? 220 : 300">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" @click="testChannel(row)" plain>测试</el-button>
              <el-button
                  size="small"
                  @click="toggleChannelStatus(row)"
                  plain
  >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" @click="openEditDialog(row)" plain>编辑</el-button>
              <el-button size="small" @click="deleteChannel(row)" plain>删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>

  <!-- 添加/编辑渠道的复用对话框 -->
  <el-dialog
      v-model="formDialogVisible"
      :title="formMode === 'add' ? '添加渠道' : '编辑渠道'"
      width="50%"
      :close-on-click-modal="false"
      class="channel-form-dialog"
  >
    <!-- 表单 v-loading 用于在加载编辑数据时显示加载动画 -->
    <el-form
        v-if="currentChannel"
        :model="currentChannel"
        label-width="120px"
        :label-position="isMobile ? 'top' : 'right'"
        v-loading="formLoading"
  >
      <el-form-item label="名称">
        <el-input v-model="currentChannel.name" placeholder="请输入渠道名称" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="currentChannel.type" placeholder="请选择类型">
          <el-option v-for="(name, code) in channelTypeMap" :key="code" :label="name" :value="Number(code)" />
        </el-select>
      </el-form-item>
      <el-form-item label="分组">
        <el-input v-model="currentChannel.group" placeholder="请输入分组名，例如 default" />
      </el-form-item>
      <el-form-item label="密钥">
        <el-input v-model="currentChannel.key" type="textarea" :rows="10" show-password placeholder="请输入渠道密钥" />
      </el-form-item>
      <el-form-item label="基础URL">
        <el-input v-model="currentChannel.baseUrl" placeholder="e.g. https://api.openai.com" />
      </el-form-item>
      <el-form-item label="支持的模型">
        <el-input v-model="currentChannel.models" placeholder="逗号分隔, e.g. gpt-3.5-turbo,gpt-4" />
      </el-form-item>
      <el-form-item label="模型映射">
        <el-input v-model="currentChannel.modelMapping" type="textarea" :rows="3" placeholder='JSON格式, e.g. {"gpt-3.5-turbo-16k": "gpt-3.5-turbo"}' />
      </el-form-item>
      <el-form-item label="优先级">
        <el-input-number v-model="currentChannel.priority" :min="0" />
      </el-form-item>
      <el-form-item label="权重">
        <el-input-number v-model="currentChannel.weight" :min="0" />
      </el-form-item>
      <el-form-item label="自动封禁">
        <el-switch v-model="currentChannel.autoBan" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="currentChannel.tag" placeholder="请输入渠道标签" />
      </el-form-item>
      <el-form-item label="设置信息">
        <el-input v-model="currentChannel.setting" type="textarea" :rows="3" placeholder="JSON格式的设置信息" />
      </el-form-item>
      <el-form-item label="参数覆盖">
        <el-input v-model="currentChannel.paramOverride" type="textarea" :rows="3" placeholder="JSON格式的参数覆盖信息" />
      </el-form-item>
      <el-form-item label="部署地区">
        <el-input v-model="currentChannel.other" type="textarea" :rows="3" placeholder="部署地区" />
      </el-form-item>
      <el-form-item label="代理设置">
        <el-select v-model="currentChannel.proxy" placeholder="请选择类型">
          <el-option v-for="(name, code) in proxyTypeMap" :key="code" :label="name" :value="Number(code)" />
        </el-select>
      </el-form-item>
      <el-form-item label="其他附加信息">
        <el-input v-model="currentChannel.otherInfo" type="textarea" :rows="2" placeholder="其他附加信息" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';
import {
  getChannelsByPoolId,
  updateChannelByPoolId,
  testChannelByPoolId,
  deleteChannelByPoolId,
  getChannelDetail,
  addChannelByPoolId // 确保已在API文件中定义并导出此函数
} from '@/api/pool'; // 引入所有需要的API函数
import type { Channel } from '@/types'; // 引入Channel类型定义
import { ElMessage, ElMessageBox } from 'element-plus'; // 引入Element Plus组件
import { Plus } from '@element-plus/icons-vue'; // 引入图标
import {channelStatusMap, channelTypeMap, proxyTypeMap} from '@/utils/maps'; // 引入辅助映射

// 定义组件的props，接收父组件传来的可见性和号池ID
const props = defineProps<{
  visible: boolean;
  poolId: number | null;
}>();

// 定义组件的emits，用于通知父组件更新状态?
const emit = defineEmits(['update:visible']);

// --- 响应式状态定义?---

// 主表格的加载状态?
const loading = ref(false);
// 存储渠道列表数据
const channels = ref<Channel[]>([]);

// 响应式：检测是否为移动端，用于精简表格列与宽度
const screenWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024);
const isMobile = computed(() => screenWidth.value < 768);
const onResize = () => { screenWidth.value = window.innerWidth; };
onMounted(() => window.addEventListener('resize', onResize));
onUnmounted(() => window.removeEventListener('resize', onResize));
// 添加/编辑表单对话框的可见性
const formDialogVisible = ref(false);
// 表单模式：'add' 为添加模式，'edit' 为编辑模式
const formMode = ref<'add' | 'edit'>('add');
// 当前正在表单中操作的渠道数据
const currentChannel = ref<Partial<Channel> | null>(null);
// 表单的加载状态（主要用于编辑时获取数据）
const formLoading = ref(false);

// 添加渠道时使用的默认数据结构
const defaultChannel: Partial<Channel> = {
  name: '',
  type: 41, // 默认类型，例如 OpenAI
  status: 2, // 默认禁用
  key: '',
  baseUrl: '',
  models: 'gemini-2.5-flash,gemini-2.5-pro',
  group: 'default',
  priority: 0,
  weight: 0,
  autoBan: 1, // 默认开启自动封禁?
  modelMapping: '{}',
  tag: '',
  setting: "",
  proxy: 1,
  paramOverride: '',
  other: '{\n"default\": \"global\"\n}',
  otherInfo: '',
};

// --- 辅助函数 ---

const getChannelTypeName = (code: number) => channelTypeMap[code] || '未知';
const getChannelStatusName = (code: number) => channelStatusMap[code]?.text || '未知';
const getStatusTag = (code: number) => channelStatusMap[code]?.type || 'info';

// --- API调用与逻辑处理 ---

/**
 * 根据号池ID获取渠道列表
 */
const fetchChannels = async (id: number) => {
  if (!id) return;
  loading.value = true;
  try {
    const data = await getChannelsByPoolId(id);
    channels.value = Array.isArray(data) ? data : [];
  } catch (error) {
    ElMessage.error('获取渠道列表失败');
  } finally {
    loading.value = false;
  }
};

// 监听主对话框的可见性，当变为可见时，获取渠道列表
watch(
    () => props.visible,
    (newVal) => {
      if (newVal && props.poolId !== null) {
        fetchChannels(props.poolId);
      } else {
        channels.value = []; // 关闭时清空数据?
      }
    }
);

/**
 * 打开添加渠道对话框?
 */
const openAddDialog = () => {
  formMode.value = 'add';
  currentChannel.value = { ...defaultChannel }; // 使用默认数据初始化表单
  formDialogVisible.value = true;
};

/**
 * 打开编辑渠道对话框?
 */
const openEditDialog = async (row: Channel) => {
  formMode.value = 'edit';
  currentChannel.value = null; // 先清空，避免显示旧数据?
  formDialogVisible.value = true;
  formLoading.value = true;

  try {
    // 调用API获取该渠道的最新、最完整数据
    const channelData = await getChannelDetail(props.poolId!, row.id);
    currentChannel.value = channelData;
  } catch (e) {
    ElMessage.error('获取渠道详情失败');
    formDialogVisible.value = false; // 获取失败则关闭对话框
  } finally {
    formLoading.value = false;
  }
};

/**
 * 处理保存操作（添加或更新）
 */
const handleSave = async () => {
  if (!currentChannel.value || props.poolId === null) return;

  formLoading.value = true;
  try {
    // 根据表单模式，调用不同的API
    if (formMode.value === 'add') {
      await addChannelByPoolId(props.poolId, currentChannel.value as Channel);
      ElMessage.success('渠道添加成功');
    } else {
      await updateChannelByPoolId(props.poolId, currentChannel.value as Channel);
      ElMessage.success('渠道更新成功');
    }
    formDialogVisible.value = false; // 关闭对话框?
    fetchChannels(props.poolId); // 刷新列表
  } catch (e) {
    ElMessage.error(formMode.value === 'add' ? '添加失败' : '更新失败');
  } finally {
    formLoading.value = false;
  }
};

/**
 * 测试渠道连通性
 */
const testChannel = async (row: Channel) => {
  try {
    await testChannelByPoolId(props.poolId!, row.id);
    ElMessage.success('测试请求已发送，请稍后刷新查看响应时间');
  }  catch (e: unknown) {
    let msg = '测试失败';
    if (e && typeof e === 'object' && 'message' in e) {
      msg += `：${(e as any).message}`;
    } else if (typeof e === 'string') {
      msg += `：${e}`;
    }
    ElMessage.error(msg);
  }
};

/**
 * 切换渠道状态（启用/禁用）
 */
const toggleChannelStatus = async (row: Channel) => {
  const newStatus = row.status === 1 ? 2 : 1;
  try {
    await updateChannelByPoolId(props.poolId!, { ...row, status: newStatus });
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用');
    fetchChannels(props.poolId!); // 操作成功后刷新列表
  } catch (e) {
    ElMessage.error('操作失败');
  }
};

/**
 * 删除渠道
 */
const deleteChannel = async (row: Channel) => {
  try {
    await ElMessageBox.confirm(`确定要删除渠道“${row.name}”吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteChannelByPoolId(props.poolId!, row.id);
    ElMessage.success('删除成功');
    fetchChannels(props.poolId!); // 删除成功后刷新列表
  } catch (error) {
    // 如果是取消操作?catch 'cancel')，则不显示消息?
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};
</script>

<style scoped>
/* 渠道列表对话框样式?*/
:deep(.channel-list-dialog) {
  max-width: 1400px;
}

:deep(.channel-list-dialog .el-dialog__body) {
  padding: 20px 24px;
}

/* 页头样式 */
.channel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--card-border);
}

.channel-header .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 表格操作按钮样式 */
.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.table-actions .el-button {
  min-width: 60px;
  font-size: 12px;
  padding: 6px 12px;
  color: var(--text-secondary);
  border-color: var(--card-border);
  background-color: transparent;
}

.table-actions .el-button:hover {
  color: var(--text-primary);
  border-color: var(--text-secondary);
  background-color: var(--card-bg-hover);
}

/* 取消相邻按钮的默认左外边距?*/
.table-actions .el-button + .el-button {
  margin-left: 0;
}

/* 表单对话框样式?*/
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button + .el-button {
  margin-left: 0;
}

/* 响应式设置?*/
@media (max-width: 1200px) {
  :deep(.channel-list-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .table-actions {
    flex-direction: column;
    gap: 4px;
  }
  
  .table-actions .el-button {
    width: 100%;
    margin: 0;
  }
  
  :deep(.el-table .el-table__cell) {
    padding: 8px 4px;
  }
  /* 表单对话框在小屏宽度与内边距优化 */
  :deep(.channel-form-dialog) {
    width: 95% !important;
    max-width: 95% !important;
    margin: 0 auto !important;
  }
  :deep(.channel-form-dialog .el-dialog__body) {
    padding: 16px 16px !important;
  }
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
  border-color: var(--card-border);
}

:deep(.el-table th) {
  background-color: var(--card-bg);
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--card-border);
}

:deep(.el-table td) {
  color: var(--text-secondary);
  border-color: var(--card-border);
}

:deep(.el-table--border) {
  border-color: var(--card-border);
}

:deep(.el-table--border::after) {
  background-color: var(--card-border);
}

/* 状态标签样式?*/
:deep(.el-tag) {
  font-weight: 500;
  color: var(--text-secondary);
  background-color: var(--card-bg-hover);
  border-color: var(--card-border);
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
}

:deep(.el-input__inner) {
  font-size: 14px;
}

:deep(.el-textarea__inner) {
  font-size: 14px;
}

/* 让表格在小屏下可横向滚动，避免挤压?*/
.table-responsive {
  width: 100%;
  overflow-x: auto;
}
</style>

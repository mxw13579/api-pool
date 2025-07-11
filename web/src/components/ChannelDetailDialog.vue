<template>
  <!-- 渠道列表主对话框 -->
  <el-dialog
      :model-value="visible"
      title="渠道列表"
      width="80%"
      @close="$emit('update:visible', false)"
  >
    <!-- 操作区域：添加渠道按钮 -->
    <div style="margin-bottom: 10px; text-align: right;">
      <el-button type="primary" @click="openAddDialog">添加渠道</el-button>
    </div>

    <!-- 渠道数据表格 -->
    <el-table :data="channels" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="type" label="类型">
        <template #default="{ row }">
          {{ getChannelTypeName(row.type) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)">
            {{ getChannelStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="group" label="分组" />
      <el-table-column label="已用配额">
        <template #default="{ row }">
          {{ row.used_quota ? (Math.floor(row.used_quota / 500000 * 100) / 100) : 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" />
      <el-table-column prop="responseTime" label="响应时间(ms)" />
      <el-table-column label="操作" fixed="right" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="testChannel(row)" type="primary" plain>测试</el-button>
          <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleChannelStatus(row)"
              plain
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" @click="openEditDialog(row)" type="info" plain>编辑</el-button>
          <el-button size="small" @click="deleteChannel(row)" type="danger" plain>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 添加/编辑渠道的复用对话框 -->
  <el-dialog
      v-model="formDialogVisible"
      :title="formMode === 'add' ? '添加渠道' : '编辑渠道'"
      width="50%"
      :close-on-click-modal="false"
  >
    <!-- 表单 v-loading 用于在加载编辑数据时显示加载动画 -->
    <el-form
        v-if="currentChannel"
        :model="currentChannel"
        label-width="120px"
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
        <el-input v-model="currentChannel.group" placeholder="请输入分组名称, e.g. default" />
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
      <span class="dialog-footer">
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
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
import {channelStatusMap, channelTypeMap, proxyTypeMap} from '@/utils/maps'; // 引入辅助映射

// 定义组件的props，接收父组件传来的可见性和号池ID
const props = defineProps<{
  visible: boolean;
  poolId: number | null;
}>();

// 定义组件的emits，用于通知父组件更新状态
const emit = defineEmits(['update:visible']);

// --- 响应式状态定义 ---

// 主表格的加载状态
const loading = ref(false);
// 存储渠道列表数据
const channels = ref<Channel[]>([]);
// 添加/编辑表单对话框的可见性
const formDialogVisible = ref(false);
// 表单模式，'add' 为添加模式, 'edit' 为编辑模式
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
  models: 'gemini-2.5-flash,gemini-2.5-pro,gemini-2.5-pro-preview-05-06,gemini-2.5-pro-preview-06-05',
  group: 'default',
  priority: 0,
  weight: 0,
  autoBan: 1, // 默认开启自动封禁
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
        channels.value = []; // 关闭时清空数据
      }
    }
);

/**
 * 打开添加渠道对话框
 */
const openAddDialog = () => {
  formMode.value = 'add';
  currentChannel.value = { ...defaultChannel }; // 使用默认数据初始化表单
  formDialogVisible.value = true;
};

/**
 * 打开编辑渠道对话框
 */
const openEditDialog = async (row: Channel) => {
  formMode.value = 'edit';
  currentChannel.value = null; // 先清空，避免显示旧数据
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
    formDialogVisible.value = false; // 关闭对话框
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
  } catch (e) {
    ElMessage.error('测试失败');
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
    await ElMessageBox.confirm(`确定要删除渠道 “${row.name}” 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteChannelByPoolId(props.poolId!, row.id);
    ElMessage.success('删除成功');
    fetchChannels(props.poolId!); // 删除成功后刷新列表
  } catch (error) {
    // 如果是取消操作(catch 'cancel')，则不显示消息
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};
</script>

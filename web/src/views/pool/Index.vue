<template>
  <div>
    <el-button type="primary" @click="handleAdd" :icon="Plus">新增号池</el-button>
    <!-- [新增] 添加“批量全号池新增渠道”按钮 -->
    <el-button type="success" @click="handleBatchAddChannel" :icon="Ticket">批量全号池新增渠道</el-button>

    <el-table :data="pools" v-loading="loading" border stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="endpoint" label="Endpoint" />
      <el-table-column prop="address" label="地址" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="handleViewChannels(row)">查看渠道</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 原有的新增/编辑号池弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
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
        <el-form-item label="监控间隔" prop="monitoringIntervalTime">
          <el-input v-model="form.monitoringIntervalTime" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 原有的渠道详情弹窗 -->
    <ChannelDetailDialog v-model:visible="channelDialogVisible" :poolId="currentPoolId" />

    <!-- [新增] 批量新增渠道的弹窗 -->
    <el-dialog
        v-model="batchAddDialogVisible"
        title="批量全号池新增渠道"
        width="60%"
        :close-on-click-modal="false"
    >
      <el-form
          v-if="batchChannelForm"
          :model="batchChannelForm"
          label-width="120px"
      >
        <el-form-item label="名称" required>
          <el-input v-model="batchChannelForm.name" placeholder="请输入渠道名称" />
        </el-form-item>
        <<el-form-item label="类型">
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
import { ref, onMounted } from 'vue';
// [修改] 引入新的API函数和图标
import { getPoolList, addPool, updatePool, deletePool, batchAddChannelToAll } from '@/api/pool';
import type { PoolEntity, Channel } from '@/types'; // 假设 Channel 类型在 types.ts 中
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Ticket } from '@element-plus/icons-vue'; // 引入 Ticket 图标
import ChannelDetailDialog from '@/components/ChannelDetailDialog.vue';
import {channelTypeMap, proxyTypeMap} from "@/utils/maps.ts";

const pools = ref<PoolEntity[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = ref<Partial<PoolEntity>>({});
const formRef = ref();

const channelDialogVisible = ref(false);
const currentPoolId = ref<number | null>(null);
const batchResultDialogVisible = ref(false);
const batchResultList = ref<string[]>([]);

import { computed } from 'vue';

const totalCount = computed(() => batchResultList.value.length);
const successCount = computed(() =>
    batchResultList.value.filter(msg => msg.includes('成功')).length
);
const failCount = computed(() =>
    batchResultList.value.filter(msg => msg.includes('失败') || msg.includes('异常')).length
);

// [新增] 批量新增渠道功能相关的状态和逻辑
const batchAddDialogVisible = ref(false);
const batchChannelForm = ref<Partial<Channel>>({});

// 参考 ChannelDetailDialog 的默认渠道对象
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
      batchResultDialogVisible.value = true; // 打开结果弹窗
      batchAddDialogVisible.value = false;   // 关闭表单弹窗
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
  form.value = {};
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
  ElMessageBox.confirm('确定要删除这个号池吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
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
</script>

<template>
  <div>
    <el-button type="primary" @click="handleAdd" :icon="Plus">新增号池</el-button>
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

    <!-- Form Dialog -->
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

    <!-- Channel Detail Dialog -->
    <ChannelDetailDialog v-model:visible="channelDialogVisible" :poolId="currentPoolId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getPoolList, addPool, updatePool, deletePool } from '@/api/pool';
import type { PoolEntity } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue'
import ChannelDetailDialog from '@/components/ChannelDetailDialog.vue';

const pools = ref<PoolEntity[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = ref<Partial<PoolEntity>>({});
const formRef = ref();

const channelDialogVisible = ref(false);
const currentPoolId = ref<number | null>(null);

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

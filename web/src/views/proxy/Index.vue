<template>
  <div>
    <el-button type="primary" @click="handleAdd" :icon="Plus">新增代理</el-button>
    <el-button type="success" @click="handleBatchAdd" :icon="Plus" style="margin-left: 10px;">批量添加</el-button>
    <el-table :data="proxies" v-loading="loading" border stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="proxyUrl" label="代理URL" />
      <el-table-column prop="source" label="来源" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="bindCount" label="绑定号池数" width="120" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
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
        <el-form-item label="代理URL" prop="proxyUrl" required>
          <el-input v-model="form.proxyUrl" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-input v-model="form.source" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="状态" prop="status" required>
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
  <el-dialog v-model="batchDialogVisible" title="批量添加代理" width="600px">
    <el-form :model="batchForm" ref="batchFormRef" label-width="120px">
      <el-form-item label="名称前缀" prop="prefixName" required>
        <el-input v-model="batchForm.prefixName" placeholder="如 proxy" />
      </el-form-item>
      <el-form-item label="代理URL批量" prop="proxyUrlBatches" required>
        <el-input
            v-model="batchForm.proxyUrlBatches"
            type="textarea"
            :rows="8"
            placeholder="每行一个代理URL"
        />
      </el-form-item>
      <el-form-item label="来源" prop="source">
        <el-input v-model="batchForm.source" />
      </el-form-item>
      <el-form-item label="地址" prop="address">
        <el-input v-model="batchForm.address" />
      </el-form-item>
      <el-form-item label="状态" prop="status" required>
        <el-switch v-model="batchForm.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="batchDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleBatchSubmit">确定</el-button>
    </template>
  </el-dialog>

</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getProxyList, addProxy, updateProxy, deleteProxy } from '@/api/proxy';
import type { ProxyEntity } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue'
import { addProxyBatches } from '@/api/proxy';


const proxies = ref<ProxyEntity[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = ref<Partial<ProxyEntity>>({ status: 1 });

const fetchProxies = async () => {
  loading.value = true;
  proxies.value = await getProxyList();
  loading.value = false;
};

onMounted(fetchProxies);

const handleAdd = () => {
  form.value = { status: 1 };
  dialogTitle.value = '新增代理';
  dialogVisible.value = true;
};

const handleEdit = (row: ProxyEntity) => {
  form.value = { ...row };
  dialogTitle.value = '编辑代理';
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const apiCall = form.value.id ? updateProxy : addProxy;
  await apiCall(form.value as any);
  ElMessage.success(form.value.id ? '更新成功' : '新增成功');
  dialogVisible.value = false;
  fetchProxies();
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这个代理吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteProxy(id);
    ElMessage.success('删除成功');
    fetchProxies();
  });
};

const batchDialogVisible = ref(false);
const batchForm = ref({
  prefixName: '',
  proxyUrlBatches: '',
  source: '',
  address: '',
  status: 1,
});

const handleBatchAdd = () => {
  batchForm.value = {
    prefixName: '',
    proxyUrlBatches: '',
    source: '',
    address: '',
    status: 1,
  };
  batchDialogVisible.value = true;
};

const handleBatchSubmit = async () => {
  if (!batchForm.value.prefixName || !batchForm.value.proxyUrlBatches) {
    ElMessage.warning('请填写名称前缀和代理URL批量');
    return;
  }
  const count = await addProxyBatches(batchForm.value);
  ElMessage.success(`批量添加成功，成功数量：${count}`);
  batchDialogVisible.value = false;
  fetchProxies();
};
</script>

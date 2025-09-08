
<template>
  <div class="page-container">
    <div class="header-toolbar">
      <el-button type="primary" @click="handleAdd" :icon="Plus" size="large">新增代理</el-button>
      <el-button type="success" @click="handleBatchAdd" :icon="Plus" size="large" class="ml-3">批量添加</el-button>
<el-button type="danger" @click="handleBatchDelete" :icon="Delete" size="large" :disabled="selectedProxies.length === 0" class="ml-3">批量删除</el-button>
      <el-checkbox
          v-model="isAllSelected"
          :indeterminate="isIndeterminate"
          @change="toggleSelectAll"
          size="large"
          class="select-all-checkbox ml-3 mr-4"
      >
        全选
      </el-checkbox>
    </div>

    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" size="40"><Loading /></el-icon>
      <p>正在加载代理数据...</p>
    </div>

    <el-row :gutter="24" v-else>
      <el-col :xs="24" :sm="12" :md="8" v-for="proxy in proxies" :key="proxy.id">
        <el-card class="themed-card" shadow="always">
          <template #header>
            <div class="card-header">
              <el-checkbox v-model="selectedProxies" :label="proxy.id" size="large" class="mr-3" style="height: 20px" @change="handleSelectionChange" />
              <span class="card-title">{{ proxy.name }}</span>
              <el-dropdown trigger="click">
                <el-button text :icon="MoreFilled" class="more-options-btn" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(proxy)" :icon="Edit">编辑</el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(proxy.id)" :icon="Delete" class="delete-item">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>

          <div class="card-content">
            <div class="info-item">
              <el-icon><Link /></el-icon>
              <span>{{ proxy.proxyUrl }}</span>
            </div>
            <div class="info-item">
              <el-icon><Compass /></el-icon>
              <span><strong>来源:</strong> {{ proxy.source || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span><strong>地址:</strong> {{ proxy.address || 'N/A' }}</span>
            </div>
          </div>

          <template #footer>
            <div class="card-footer">
              <el-tag :type="proxy.status === 1 ? 'success' : 'danger'" round>
                {{ proxy.status === 1 ? '启用' : '禁用' }}
              </el-tag>
              <el-tag type="info" round effect="light">
                绑定号池: {{ proxy.bindCount }}
              </el-tag>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- Form Dialogs are unchanged -->
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

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getProxyList, addProxy, updateProxy, deleteProxy, addProxyBatches, deleteProxyBatches } from '@/api/proxy';
import type { ProxyEntity } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Loading, MoreFilled, Edit, Delete, Link, Location, Compass } from '@element-plus/icons-vue';
import { computed, watch } from 'vue';



const proxies = ref<ProxyEntity[]>([]);
const selectedProxies = ref<number[]>([]);
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

const isAllSelected = computed({
  get: () => proxies.value.length > 0 && selectedProxies.value.length === proxies.value.length,
  set: (val: boolean) => {
    if (val) {
      selectedProxies.value = proxies.value.map(p => p.id);
    } else {
      selectedProxies.value = [];
    }
  }
});

// 计算属性：是否半选
const isIndeterminate = computed(() =>
    selectedProxies.value.length > 0 &&
    selectedProxies.value.length < proxies.value.length
);

// 监听代理列表变化，自动同步全选状态
watch(proxies, () => {
  if (selectedProxies.value.length > 0) {
    // 移除已删除的代理id
    selectedProxies.value = selectedProxies.value.filter(id =>
        proxies.value.some(p => p.id === id)
    );
  }
});

// 全选切换事件（可选，主要用于兼容 el-checkbox 的 @change 事件）
const toggleSelectAll = (val: boolean) => {
  isAllSelected.value = val;
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

const handleSelectionChange = () => {
  // 可以在这里处理选中变化的逻辑，如果需要的话
};

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedProxies.value.length} 个代理吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteProxyBatches(selectedProxies.value);
    ElMessage.success('批量删除成功');
    selectedProxies.value = [];
    fetchProxies();
  });
};
</script>

<style scoped>
/* Using shared styles from theme.css */
.page-container {
  /* This class can be empty if layout provides the background */
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
  color: var(--text-muted);
}
.loading-state .el-icon {
  margin-bottom: 10px;
}

.themed-card {
    margin-bottom: 24px;
    --el-card-padding: 0;
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--accent-color-light);
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
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
  color: var(--text-secondary);
  word-break: break-all;
}
.info-item .el-icon {
  font-size: 16px;
  color: var(--text-muted);
  flex-shrink: 0;
}
.info-item strong {
  color: var(--text-primary);
  margin-right: 5px;
}

:deep(.el-card__footer) {
  border-top: 1px solid var(--accent-color-light);
  padding: 12px 20px;
  background-color: var(--card-footer-bg);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.select-all-checkbox {
  height: 48px;  /* 与 large 按钮高度对齐 */
  display: flex;
  align-items: center;
  padding: 0 18px;
  border: 1.5px solid var(--el-color-primary);
  border-radius: 6px;  /* 与按钮圆角一致 */
  background: #fff;
  font-weight: 500;  /* 减轻字重 */
  color: var(--el-color-primary);
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.06);
  transition: background 0.2s, border 0.2s, color 0.2s;
  cursor: pointer;
}
.select-all-checkbox:hover {
  background: #f4faff;
  border-color: var(--el-color-primary-dark-2);
  color: var(--el-color-primary-dark-2);
}
.select-all-checkbox .el-checkbox__label {
  font-size: 16px;
  font-weight: 500;  /* 与外层一致 */
  padding-left: 4px;
}

.header-toolbar {
  margin-bottom: 24px;
  display: flex;
  align-items: center; /* 垂直居中所有按钮和全选 */
}


</style>


<template>
  <div class="page-container">
    <div class="header-toolbar">
      <el-button type="primary" @click="handleAdd" :icon="Plus" size="large">新增代理</el-button>
      <el-button type="success" @click="handleBatchAdd" :icon="Plus" size="large" class="ml-3">批量添加</el-button>
      <el-button type="danger" @click="handleBatchDelete" :icon="Delete" size="large" :disabled="selectedProxies.length === 0" class="ml-3">批量删除</el-button>
    </div>

    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" size="40"><Loading /></el-icon>
      <p>正在加载代理数据...</p>
    </div>

    <el-table
      v-else
      :data="proxies"
      style="width: 100%"
      @selection-change="handleTableSelectionChange"
      class="proxy-table"
    >
      <el-table-column type="selection" width="55" />

      <el-table-column prop="name" label="名称" min-width="120">
        <template #default="{ row }">
          <span class="proxy-name">{{ row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="proxyUrl" label="代理URL" min-width="200">
        <template #default="{ row }">
          <div class="url-cell">
            <el-icon class="url-icon"><Link /></el-icon>
            <span class="url-text">{{ row.proxyUrl }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="source" label="来源" min-width="100">
        <template #default="{ row }">
          <span>{{ row.source || 'N/A' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="address" label="地址" min-width="120">
        <template #default="{ row }">
          <span>{{ row.address || 'N/A' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="bindCount" label="绑定号池" width="100">
        <template #default="{ row }">
          <el-tag type="info" size="small" effect="light">
            {{ row.bindCount || 0 }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            :icon="Edit"
            @click="handleEdit(row)"
            link
          >
            编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            :icon="Delete"
            @click="handleDelete(row.id)"
            link
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </div>

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
import { getProxyList, addProxy, updateProxy, deleteProxy, addProxyBatches, deleteProxyBatches, type PageRequest, type PageResult } from '@/api/proxy';
import type { ProxyEntity } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Loading, Edit, Delete, Link } from '@element-plus/icons-vue';
import { computed, watch } from 'vue';



const proxies = ref<ProxyEntity[]>([]);
const selectedProxies = ref<number[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = ref<Partial<ProxyEntity>>({ status: 1 });

// 分页相关状态
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0
});

const fetchProxies = async () => {
  loading.value = true;
  try {
    const params: PageRequest = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      orderBy: 'created_at',
      orderDirection: 'desc'
    };

    const result: PageResult<ProxyEntity> = await getProxyList(params);
    proxies.value = result.items;
    pagination.value.total = result.total;
    pagination.value.totalPages = result.totalPages;

    // 清理失效的选择项
    if (selectedProxies.value.length > 0) {
      selectedProxies.value = selectedProxies.value.filter(id =>
        proxies.value.some(p => p.id === id)
      );
    }
  } catch (error) {
    console.error('获取代理列表失败:', error);
    ElMessage.error('获取代理列表失败');
  } finally {
    loading.value = false;
  }
};

// 分页变化处理
const handlePageChange = (page: number) => {
  pagination.value.pageNum = page;
  fetchProxies();
};

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size;
  pagination.value.pageNum = 1; // 重置到第一页
  fetchProxies();
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

// 表格选择变化事件
const handleTableSelectionChange = (selection: ProxyEntity[]) => {
  selectedProxies.value = selection.map(item => item.id);
};

// 全选状态计算 - 基于表格选择
const isAllSelected = computed({
  get: () => proxies.value.length > 0 && selectedProxies.value.length === proxies.value.length,
  set: (val: boolean) => {
    // 这个功能将由表格的全选checkbox自动处理
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
  // 保留兼容性，实际由handleTableSelectionChange处理
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
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
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

/* 表格样式 */
.proxy-table {
  margin-top: 8px;
}

.proxy-name {
  font-weight: 600;
  color: var(--text-primary, #303133);
}

.url-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.url-icon {
  color: var(--text-muted, #909399);
  font-size: 14px;
}

.url-text {
  word-break: break-all;
  color: var(--text-secondary, #606266);
}

/* 响应式表格 */
@media (max-width: 768px) {
  .proxy-table .el-table__body-wrapper {
    overflow-x: auto;
  }

  .proxy-table .el-table__row {
    font-size: 14px;
  }
}

/* 分页样式 */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.pagination {
  background: #fff;
}

/* 响应式分页 */
@media (max-width: 768px) {
  .pagination-container {
    margin-top: 16px;
  }

  .pagination .el-pagination__editor {
    width: 60px;
  }
}


</style>

<style scoped>
/* 避免与 Element Plus 默认相邻按钮左外边距叠加 */
.header-toolbar .el-button + .el-button { margin-left: 0; }

/* 移动端：纵向排列并清理工具类左右边距 */
@media (max-width: 768px) {
  .header-toolbar { flex-direction: column; align-items: stretch; gap: 8px; }
  .header-toolbar .ml-3, .header-toolbar .mr-3, .header-toolbar .mr-4 { margin-left: 0 !important; margin-right: 0 !important; }
  /* 对当前页面内的对话框统一做移动端宽度适配 */
  :deep(.el-dialog) {
    width: 95% !important;
    max-width: 95% !important;
    margin: 0 auto !important;
  }
}
</style>


<template>
  <div class="page-container">
    <div class="header-toolbar">
      <el-button type="primary" @click="handleAdd" :icon="Plus" size="large">新增代理</el-button>
      <el-button type="success" @click="handleBatchAdd" :icon="Plus" size="large" class="ml-3">批量添加</el-button>
      <el-button type="danger" @click="handleBatchDelete" :icon="Delete" size="large" class="ml-3">批量删除</el-button>
    </div>

    <!-- 表格卡片容器 -->
    <div class="table-card">
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

      <el-table-column prop="name" label="名称" min-width="80">
        <template #default="{ row }">
          <span class="proxy-name">{{ row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="proxyUrl" label="代理URL" min-width="240">
        <template #default="{ row }">
          <div class="url-cell">
            <el-icon class="url-icon"><Link /></el-icon>
            <span class="url-text">{{ row.proxyUrl }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="source" label="来源" min-width="40">
        <template #default="{ row }">
          <span>{{ row.source || 'N/A' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="address" label="地址" min-width="40">
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

      <el-table-column prop="bindCount" label="绑定号池" width="80">
        <template #default="{ row }">
          <el-tag type="info" size="small" effect="light">
            {{ row.bindCount || 0 }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="360" fixed="right" class-name="action-column">
        <template #default="{ row }">
          <div class="action-links">
            <el-link type="primary" :underline="false" @click="handleEdit(row)">
              <el-icon class="link-icon"><Edit /></el-icon>
              <span>编辑</span>
            </el-link>
            <span class="action-divider" />
            <el-link type="danger" :underline="false" @click="handleDelete(row.id)">
              <el-icon class="link-icon"><Delete /></el-icon>
              <span>删除</span>
            </el-link>
          </div>
        </template>
      </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <div class="pagination-card">
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
      </div>
    </div>
    <!-- 表格卡片容器结束 -->

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
  if (selectedProxies.value.length === 0) {
    ElMessage.warning('请先选择要删除的代理');
    return;
  }

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
/* ==================== 页面容器样式 ==================== */
.page-container {
  padding: var(--space-lg);
  background-color: var(--bg-base);
  min-height: calc(100vh - 60px); /* 减去头部高度 */
}

/* 表格卡片容器 */
.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: var(--space-lg);
  margin-top: var(--space-md);
  border: 1px solid var(--border-color);
}

/* ==================== 工具栏样式 ==================== */
.header-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md);
  margin-bottom: 0; /* 移除底部边距，由table-card的margin-top处理 */
}

/* 按钮组优化 */
.header-toolbar :deep(.el-button) {
  height: var(--button-height-md);
  padding: 0 var(--space-lg);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-sm);
  transition: var(--transition-all);
  border: none;
}

/* 按钮悬停效果 */
.header-toolbar :deep(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-hover);
}

/* 按钮激活效果 */
.header-toolbar :deep(.el-button:active) {
  transform: translateY(0);
}

/* 禁用按钮样式 */
.header-toolbar :deep(.el-button.is-disabled) {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 主要按钮 */
.header-toolbar :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: var(--text-inverse);
}

.header-toolbar :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, var(--primary-hover) 0%, var(--primary-active) 100%);
}

/* 成功按钮 */
.header-toolbar :deep(.el-button--success) {
  background: var(--success-color);
  color: var(--text-inverse);
}

.header-toolbar :deep(.el-button--success:hover) {
  background: var(--success-hover);
}

/* 危险按钮 */
.header-toolbar :deep(.el-button--danger) {
  background: var(--danger-color);
  color: var(--text-inverse);
}

.header-toolbar :deep(.el-button--danger:hover) {
  background: var(--danger-hover);
}

/* 强制批量删除按钮始终保持启用状态的样式 */
.header-toolbar :deep(.el-button--danger.is-disabled) {
  opacity: 1 !important;
  cursor: pointer !important;
  background: var(--danger-color) !important;
}

.header-toolbar :deep(.el-button--danger.is-disabled:hover) {
  background: var(--danger-hover) !important;
  transform: translateY(-1px) !important;
  box-shadow: var(--shadow-hover) !important;
}

/* 按钮图标间距 */
.header-toolbar :deep(.el-button .el-icon) {
  margin-right: var(--space-xs);
}

/* ==================== 加载状态 ==================== */
.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
  color: var(--text-tertiary);
}

.loading-state .el-icon {
  margin-bottom: var(--space-md);
  color: var(--primary-color);
}

.loading-state p {
  font-size: var(--font-size-base);
  margin-top: var(--space-sm);
}

/* ==================== 表格样式优化 ==================== */
.proxy-table {
  margin-top: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

/* 表头样式 */
.proxy-table :deep(.el-table__header-wrapper) {
  background-color: #f8fafc;
}

.proxy-table :deep(.el-table__header th) {
  background-color: #f8fafc !important;
  color: var(--text-secondary);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  padding: var(--space-md) var(--space-sm);
  border-bottom: 2px solid var(--border-color);
}

/* 表格边框 */
.proxy-table :deep(.el-table) {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
}

.proxy-table :deep(.el-table td),
.proxy-table :deep(.el-table th.is-leaf) {
  border-bottom: 1px solid var(--border-light);
}

/* 单元格样式 */
.proxy-table :deep(.el-table__body td) {
  padding: var(--space-md) var(--space-sm);
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

/* 表格行悬停效果 */
.proxy-table :deep(.el-table__body tr:hover > td) {
  background-color: var(--bg-hover) !important;
  transition: background-color var(--transition-base);
  position: relative;
}

/* 表格行悬停左侧色条 */
.proxy-table :deep(.el-table__body tr:hover > td:first-child::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--primary-color);
  transition: width var(--transition-base);
}

/* 隔行换色（斑马纹） */
.proxy-table :deep(.el-table__body tr:nth-child(even) > td) {
  background-color: var(--bg-stripe);
}

/* 选中行样式 */
.proxy-table :deep(.el-table__body tr.el-table__row--striped.hover-row > td) {
  background-color: var(--primary-lighter) !important;
}

/* ==================== 表格内容样式 ==================== */
.proxy-name {
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

.url-cell {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.url-icon {
  color: var(--text-tertiary);
  font-size: 14px;
  flex-shrink: 0;
}

.url-text {
  word-break: break-all;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
}

/* ==================== 状态标签和徽章样式 ==================== */
.proxy-table :deep(.el-tag) {
  border-radius: var(--radius-xs);
  padding: 4px 12px;
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-xs);
  border: none;
  height: 24px;
  line-height: 16px;
}

/* 成功状态标签 */
.proxy-table :deep(.el-tag--success) {
  background-color: var(--success-lighter);
  color: var(--success-dark);
}

/* 危险状态标签 */
.proxy-table :deep(.el-tag--danger) {
  background-color: var(--danger-lighter);
  color: var(--danger-dark);
}

/* 信息状态标签（绑定数量） */
.proxy-table :deep(.el-tag--info) {
  background-color: var(--info-lighter);
  color: var(--info-dark);
  font-weight: var(--font-weight-semibold);
}

/* ==================== 操作按钮样式 ==================== */
.proxy-table :deep(.action-column .cell) {
  display: flex;
  align-items: center;
}

.action-links {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-sm);
}

.action-links :deep(.el-link) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: var(--radius-xs);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-all);
}

/* 编辑按钮 */
.action-links :deep(.el-link--primary) {
  color: var(--primary-color);
}

.action-links :deep(.el-link--primary:hover) {
  background: var(--primary-lighter);
  color: var(--primary-hover);
}

/* 删除按钮 */
.action-links :deep(.el-link--danger) {
  color: var(--danger-color);
}

.action-links :deep(.el-link--danger:hover) {
  background: var(--danger-lighter);
  color: var(--danger-hover);
}

/* 操作按钮图标动画 */
.action-links :deep(.el-link:hover .link-icon) {
  transform: scale(1.1);
  transition: transform var(--transition-fast);
}

.action-divider {
  width: 1px;
  height: 14px;
  background: var(--border-color);
  flex-shrink: 0;
}

.link-icon {
  font-size: 15px;
  transition: var(--transition-transform);
}

/* ==================== 分页组件样式 ==================== */
.pagination-container {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: center;
}

.pagination-card {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
  min-width: 0;
}

.pagination-card :deep(.el-pagination) {
  margin: 0;
}

/* 分页文字颜色 */
.pagination-card :deep(.el-pagination__total),
.pagination-card :deep(.el-pagination__jump) {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

/* 分页选择器 */
.pagination-card :deep(.el-select .el-input__wrapper) {
  box-shadow: none;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xs);
  transition: var(--transition-colors);
}

.pagination-card :deep(.el-select .el-input__wrapper:hover) {
  border-color: var(--primary-color);
}

/* 分页输入框 */
.pagination-card :deep(.el-pagination__editor .el-input__inner) {
  border-color: var(--border-color);
  border-radius: var(--radius-xs);
  transition: var(--transition-colors);
}

.pagination-card :deep(.el-pagination__editor .el-input__inner:focus) {
  border-color: var(--primary-color);
}

/* 分页按钮通用样式 */
.pagination-card :deep(.el-pagination button),
.pagination-card :deep(.el-pager li) {
  border-radius: var(--radius-xs);
  transition: var(--transition-all);
  background: transparent;
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
}

/* 分页页码 */
.pagination-card :deep(.el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  margin: 0 2px;
}

/* 分页按钮和页码悬停 */
.pagination-card :deep(.el-pager li:not(.is-active):hover),
.pagination-card :deep(.el-pagination button:not(.is-disabled):hover) {
  background: var(--primary-lighter);
  color: var(--primary-hover);
}

/* 当前页码高亮 */
.pagination-card :deep(.el-pager li.is-active) {
  background: var(--primary-color);
  color: var(--text-inverse);
  font-weight: var(--font-weight-semibold);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

/* 分页按钮禁用状态 */
.pagination-card :deep(.el-pagination button.is-disabled) {
  color: var(--text-quaternary);
  cursor: not-allowed;
}

/* ==================== 对话框样式 ==================== */
/* 对话框容器 */
:deep(.el-dialog) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-2xl);
  padding: 0;
}

/* 对话框头部 */
:deep(.el-dialog__header) {
  padding: var(--space-lg) var(--space-xl);
  border-bottom: 1px solid var(--border-light);
  margin: 0;
}

:deep(.el-dialog__title) {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

:deep(.el-dialog__headerbtn) {
  top: var(--space-lg);
  right: var(--space-lg);
  width: 32px;
  height: 32px;
  font-size: 20px;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--danger-color);
}

/* 对话框内容 */
:deep(.el-dialog__body) {
  padding: var(--space-xl);
}

/* 表单样式 */
:deep(.el-form-item) {
  margin-bottom: var(--space-lg);
}

:deep(.el-form-item__label) {
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

/* 输入框 */
:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: none;
  border: 1px solid var(--border-color);
  transition: var(--transition-colors);
  padding: 8px 12px;
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--primary-color);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-lighter);
}

/* 文本域 */
:deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  border-color: var(--border-color);
  transition: var(--transition-colors);
  padding: var(--space-sm) var(--space-md);
  font-size: var(--font-size-base);
}

:deep(.el-textarea__inner:hover) {
  border-color: var(--primary-color);
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-lighter);
}

/* 开关组件 */
:deep(.el-switch) {
  --el-switch-on-color: var(--success-color);
  --el-switch-off-color: var(--info-color);
}

/* 对话框底部 */
:deep(.el-dialog__footer) {
  padding: var(--space-lg) var(--space-xl);
  border-top: 1px solid var(--border-light);
}

/* 对话框按钮组 */
:deep(.el-dialog__footer .el-button) {
  min-width: 80px;
  height: var(--button-height-md);
  padding: 0 var(--space-lg);
  border-radius: var(--radius-sm);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-all);
}

:deep(.el-dialog__footer .el-button:hover) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

:deep(.el-dialog__footer .el-button--primary) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.el-dialog__footer .el-button--primary:hover) {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
}

/* ==================== 响应式适配 ==================== */

/* 移动端和小屏幕适配（768px以下） */
@media (max-width: 768px) {
  /* 页面容器 */
  .page-container {
    padding: var(--space-md);
  }

  /* 表格卡片 */
  .table-card {
    padding: var(--space-md);
    margin-top: var(--space-sm);
  }

  /* 工具栏按钮组 */
  .header-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-sm);
  }

  .header-toolbar .el-button {
    width: 100%;
    margin: 0 !important;
  }

  /* 表格滚动 */
  .proxy-table :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }

  .proxy-table :deep(.el-table__row) {
    font-size: var(--font-size-sm);
  }

  /* 分页组件 */
  .pagination-container {
    margin-top: var(--space-md);
    justify-content: center;
  }

  .pagination-card {
    width: 100%;
    padding: var(--space-sm) var(--space-md);
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--space-sm);
  }

  /* 分页布局调整 */
  .pagination-card :deep(.el-pagination__sizes),
  .pagination-card :deep(.el-pagination__total) {
    flex: 0 0 100%;
    justify-content: center;
  }

  .pagination-card :deep(.el-pagination__jump) {
    margin-left: 0 !important;
  }

  /* 对话框 */
  :deep(.el-dialog) {
    width: 95% !important;
    max-width: 95% !important;
    margin: var(--space-md) auto !important;
  }

  :deep(.el-dialog__header),
  :deep(.el-dialog__body),
  :deep(.el-dialog__footer) {
    padding: var(--space-md);
  }
}

/* 平板适配（768px-1024px） */
@media (min-width: 768px) and (max-width: 1024px) {
  .page-container {
    padding: var(--space-lg);
  }

  .header-toolbar {
    gap: var(--space-sm);
  }

  .pagination-card {
    gap: var(--space-sm);
  }
}

/* 去除Element Plus按钮默认边距 */
.header-toolbar .el-button + .el-button {
  margin-left: 0;
}

</style>

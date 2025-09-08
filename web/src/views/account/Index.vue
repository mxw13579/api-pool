<template>
  <div>
    <el-button type="primary" @click="handleAdd" :icon="Plus" size="large">新增账户</el-button>
    <el-table :data="accounts" v-loading="loading" border stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="昵称" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色" />
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
        <el-form-item label="昵称" prop="name" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="用户名" prop="username" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!form.id">
          <el-input v-model="form.password" type="password" show-password placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-input v-model="form.role" />
        </el-form-item>
        <el-form-item label="状态" prop="status" required>
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getAccountList, addAccount, updateAccount, deleteAccount } from '@/api/account';
import type { AccountEntity } from '@/types';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue'

const accounts = ref<AccountEntity[]>([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = ref<Partial<AccountEntity>>({ status: 1 });

const fetchAccounts = async () => {
  loading.value = true;
  accounts.value = await getAccountList();
  loading.value = false;
};

onMounted(fetchAccounts);

const handleAdd = () => {
  form.value = { status: 1 };
  dialogTitle.value = '新增账户';
  dialogVisible.value = true;
};

const handleEdit = (row: AccountEntity) => {
  form.value = { ...row };
  dialogTitle.value = '编辑账户';
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const apiCall = form.value.id ? updateAccount : addAccount;
  await apiCall(form.value as any);
  ElMessage.success(form.value.id ? '更新成功' : '新增成功');
  dialogVisible.value = false;
  fetchAccounts();
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这个账户吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteAccount(id);
    ElMessage.success('删除成功');
    fetchAccounts();
  });
};
</script>

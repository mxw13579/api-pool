<template>
  <div class="login-container">
    <div class="login-form">
      <!-- 品牌标识区域 -->
      <div class="brand-section">
        <div class="brand-icon">
          <el-icon :size="48" color="var(--el-color-primary)">
            <Connection />
          </el-icon>
        </div>
        <h2 class="brand-title">FuFu 号池管理系统</h2>
        <p class="brand-subtitle">统一API资源池管理平台</p>
      </div>

      <!-- 登录表单区域 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form-content"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username" class="form-item">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password" class="form-item">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item class="form-item login-btn-item">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            <template #loading>
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
            </template>
            {{ loading ? '登录中' : '立即登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 错误信息提示 -->
      <transition name="el-fade-in">
        <el-alert
          v-if="error"
          :title="error"
          type="error"
          show-icon
          :closable="false"
          class="error-alert"
        />
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElForm, ElMessage } from 'element-plus';
import { User, Lock, Loading, Connection } from '@element-plus/icons-vue';
// 假设你有一个封装好的axios实例
import request, { setToken } from '@/api/index';

// 表单引用
const loginFormRef = ref<InstanceType<typeof ElForm>>();

// 表单数据
const loginForm = reactive({
  username: 'admin',
  password: 'admin'
});

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 20, message: '密码长度在 4 到 20 个字符', trigger: 'blur' }
  ]
};

const loading = ref(false);
const error = ref('');
const router = useRouter();

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  try {
    // 先进行表单验证
    const isValid = await loginFormRef.value.validate();
    if (!isValid) return;

    loading.value = true;
    error.value = '';
    
    const res = await request.post('/account/login', {
      username: loginForm.username,
      password: loginForm.password,
    });
    
    const token = res.data.token;
    if (token) {
      setToken(token); // 使用安全的token设置函数
      ElMessage.success('登录成功！');
      router.push('/');
    } else {
      error.value = '登录成功，但未能获取到Token。';
    }
  } catch (err: any) {
    error.value = err.message || '登录失败，请检查用户名和密码';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--app-bg-start) 0%, var(--app-bg-end) 100%);
  padding: 20px;
  box-sizing: border-box;
}

.login-form {
  width: 100%;
  max-width: 400px;
  padding: 40px 32px;
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 123, 255, 0.15);
  border: 1px solid var(--card-border);
}

/* 品牌标识区域 */
.brand-section {
  text-align: center;
  margin-bottom: 32px;
}

.brand-icon {
  margin-bottom: 16px;
}

.brand-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
  font-weight: 400;
}

/* 表单区域 */
.login-form-content {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
}

.login-btn-item {
  margin-bottom: 16px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

/* 错误提示 */
.error-alert {
  margin-top: 16px;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }
  
  .login-form {
    padding: 32px 24px;
    max-width: 100%;
  }
  
  .brand-title {
    font-size: 20px;
  }
  
  .brand-subtitle {
    font-size: 13px;
  }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(0, 123, 255, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 12px rgba(0, 123, 255, 0.25);
}

:deep(.el-form-item__error) {
  font-size: 12px;
  margin-top: 4px;
}

/* 加载状态动画 */
.el-fade-in-enter-active,
.el-fade-in-leave-active {
  transition: opacity 0.3s ease;
}

.el-fade-in-enter-from,
.el-fade-in-leave-to {
  opacity: 0;
}
</style>

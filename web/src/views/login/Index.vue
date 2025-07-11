<template>
  <div class="login-container">
    <div class="login-form">
      <h2>系统登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="username" required />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="password" required />
        </div>
        <div class="form-group">
          <button type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </div>
        <p v-if="error" class="error-message">{{ error }}</p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// 假设你有一个封装好的axios实例
import request from '@/utils/request';

const username = ref('admin');
const password = ref('admin');
const loading = ref(false);
const error = ref('');
const router = useRouter();

const handleLogin = async () => {
  loading.value = true;
  error.value = '';
  try {
    const res = await request.post('/api/account/login', {
      username: username.value,
      password: password.value,
    });

    const token = res.data.token;

    if (token) {
      // 将token存储到localStorage
      localStorage.setItem('authToken', token);
      // 跳转到首页
      router.push('/');
    } else {
      // 作为一种保护，如果成功响应中没有token，也视为错误
      throw new Error('登录成功，但未获取到有效的Token。');
    }

  } catch (err: any) {
    // 3. 处理错误逻辑。
    // 所有非200的业务错误和网络错误都会被这里的 catch 捕获。
    // err.message 就是拦截器中 new Error(res.msg) 传递过来的错误信息。
    console.error('登录失败，错误详情:', err.message);
    error.value = err.message || '登录失败，发生未知错误。';

  } finally {
    // 4. 无论成功或失败，都结束加载状态。
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-form {
  padding: 2rem 3rem;
  width: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
  text-align: left;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background-color: #1890ff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #40a9ff;
}

button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.error-message {
  color: red;
  margin-top: 1rem;
}
</style>

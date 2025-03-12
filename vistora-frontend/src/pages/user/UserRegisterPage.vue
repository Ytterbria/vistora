<template>
  <div id="user-register-page">
    <div class="background-overlay"></div>
    <div class="register-container">
      <div class="register-card">
        <h2 class="title">用户注册</h2>
        <p class="description">vistora - 企业级智能协同云图库</p>

        <a-form
          :model="formState"
          name="registerForm"
          autocomplete="off"
          @finish="handleSubmit"
          @finishFailed="onFinishFailed"
          class="form-wrapper"
        >
          <!-- 用户账号 -->
          <a-form-item
            name="userAccount"
            :rules="[
              { required: true, message: '请输入用户账号' },
              { min: 8, message: '账号至少4个字符' },
              { max: 20, message: '账号最多20个字符' },
            ]"
          >
            <a-input
              v-model:value="formState.userAccount"
              placeholder="请输入用户账号"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <UserOutlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <!-- 密码 -->
          <a-form-item
            name="password"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码至少8个字符' },
            ]"
          >
            <a-input-password
              v-model:value="formState.password"
              placeholder="请输入密码"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 确认密码 -->
          <a-form-item
            name="confirmPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword },
            ]"
          >
            <a-input-password
              v-model:value="formState.confirmPassword"
              placeholder="请确认密码"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 按钮组 -->
          <a-form-item class="button-group">
            <a-button type="primary" html-type="submit" size="large" class="register-button">
              注册
            </a-button>
            <div class="login-link">
              已有账号?
              <RouterLink to="/user/login">立即登录</RouterLink>
            </div>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { UserOutlined, LockOutlined, PhoneOutlined } from '@ant-design/icons-vue'
import { userRegisterUsingPost } from '@/api/userController' // 替换为实际API路径
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

interface FormState {
  userAccount: string
  password: string
  confirmPassword: string
}

const formState = reactive<FormState>({
  userAccount: '',
  password: '',
  confirmPassword: '',
})

const router = useRouter()

// 验证两次密码是否一致
const validateConfirmPassword = async (_: any, value: string) => {
  if (!value) return Promise.reject('请确认密码')
  if (value !== formState.password) return Promise.reject('两次密码不一致')
  return Promise.resolve()
}

// 提交表单
const handleSubmit = async (values: any) => {
  const res = await userRegisterUsingPost(values)
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    }) // 注册成功后跳转到登录页
  } else {
    message.error(res.data.message || '注册失败')
  }
}

// 表单验证失败回调
const onFinishFailed = (errorInfo: any) => {
  console.log('验证失败:', errorInfo)
}
</script>

<style lang="less" scoped>
@primary-color: #1890ff;
@secondary-color: #85d4c0;
@background-start: #f5f7fa;
@background-end: #c3cfe2;
@card-bg: rgba(255, 255, 255, 0.9);

#user-register-page {
  min-height: 10vh;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.register-container {
  max-width: 520px;
  width: 100%;
  padding: 24px;
  z-index: 1;
}

.register-card {
  background: @card-bg;
  border-radius: 25px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.1);
  padding: 48px 40px;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transform-style: preserve-3d;
  transition: transform 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    top: -20px;
    left: -20px;
    width: 100px;
    height: 100px;
    background: rgba(255, 255, 255, 0.1);
    transform: rotate(45deg);
    animation: float 6s linear infinite;
  }
}

@keyframes float {
  0% {
    transform: rotate(45deg) translate(0, 0);
  }
  50% {
    transform: rotate(45deg) translate(20px, 20px);
  }
  100% {
    transform: rotate(45deg) translate(0, 0);
  }
}

.title {
  color: @primary-color;
  font-size: 40px;
  text-align: center;
  letter-spacing: 2px;
  margin-bottom: 24px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 50%;
    width: 64px;
    height: 4px;
    background: linear-gradient(90deg, @primary-color, @secondary-color);
    transform: translateX(-50%);
    border-radius: 2px;
  }
}

.description {
  color: #666;
  font-size: 18px;
  text-align: center;
  margin-bottom: 48px;
  line-height: 1.6;
}

.custom-input {
  :deep(.ant-input) {
    border-radius: 15px;
    border: 2px solid #e8e8e8;
    padding: 16px 20px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    background: rgba(255, 255, 255, 0.85);

    &:focus {
      border-color: @primary-color;
      box-shadow: 0 0 0 4px fade(@primary-color, 30%);
      background: white;
    }
  }

  .input-icon {
    color: rgba(0, 0, 0, 0.4);
    margin-right: 12px;
    transition:
      color 0.3s,
      transform 0.3s;
  }

  &:hover {
    .input-icon {
      color: rgba(0, 0, 0, 0.6);
      transform: translateX(3px);
    }
  }
}

.button-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.register-button {
  width: 100%;
  border-radius: 15px;
  background: linear-gradient(135deg, @secondary-color 0%, @primary-color 100%);
  border: none;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 6px 12px rgba(24, 144, 255, 0.25);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.3);
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 16px rgba(24, 144, 255, 0.3);

    &::after {
      transform: translateX(100%);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

.login-link {
  color: #666;
  font-size: 16px;
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;

  a {
    color: @primary-color;
    margin-left: 8px;
    text-decoration: none;
    position: relative;
    transition: all 0.3s;

    &::after {
      content: '';
      position: absolute;
      width: 0;
      height: 3px;
      bottom: -3px;
      left: 0;
      background: linear-gradient(90deg, @primary-color, @secondary-color);
      transition:
        width 0.3s,
        background 0.3s;
    }

    &:hover {
      color: darken(@primary-color, 10%);

      &::after {
        width: 100%;
        background: linear-gradient(270deg, @primary-color, @secondary-color);
      }
    }
  }
}
</style>

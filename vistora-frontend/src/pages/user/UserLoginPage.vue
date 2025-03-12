<template>
  <div id="user-login-page">
    <div class="background-overlay"></div>
    <div class="login-container">
      <div class="login-card">
        <h2 class="title">用户登录</h2>
        <p class="description">vistora - 企业级智能协同云图库</p>

        <a-form
          :model="formState"
          name="basic"
          autocomplete="off"
          @finish="handleSubmit"
          @finishFailed="onFinishFailed"
          class="form-wrapper"
        >
          <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入用户账号' }]">
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

          <a-form-item name="userPassword" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password
              v-model:value="formState.userPassword"
              placeholder="请输入密码"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item class="button-group">
            <a-button type="primary" html-type="submit" size="large" class="login-button">
              登录
            </a-button>
            <div class="register-link">
              没有账号?
              <RouterLink to="/user/register">立即注册</RouterLink>
            </div>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/userLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

interface FormState {
  username: string
  password: string
  remember: boolean
}

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()

const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  if (res.data.code === 0 && res.data.data) {
    // 如果登录成功,则将登录状态和信息保存到全局中
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error(res.data.message)
  }
}

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
@text-color: #333;
@type-speed: 0.8s;
@cursor-color: #1890ff;
@title-gradient: linear-gradient(135deg, #1890ff, #85d4c0);
@title-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);

#user-login-page {
  min-height: 20vh;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.background-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    135deg,
    lighten(@background-start, 5%) 0%,
    @background-start 25%,
    @background-end 75%,
    darken(@background-end, 5%) 100%
  );
  animation: gradientAnimation 15s ease infinite;
  transform-origin: top left;
  z-index: -1;
}

@keyframes gradientAnimation {
  0% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.login-container {
  max-width: 520px;
  width: 100%;
  padding: 24px;
  z-index: 1;
}

.login-card {
  background: @card-bg;
  border-radius: 25px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.1);
  padding: 48px 40px;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transform-style: preserve-3d;
  transition: transform 0.3s ease;
}

.title {
  color: @primary-color;
  margin-bottom: 16px;
  text-align: center;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  letter-spacing: 1.5px;

  font-size: 40px;
  letter-spacing: 2px;
  background: @title-gradient;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: @title-shadow;
  position: relative;
  padding-bottom: 12px;
  margin-bottom: 24px;
}

.description {
  color: #666;
  font-size: 18px;
  margin-bottom: 48px;
  text-align: center;
  line-height: 1.6;

  overflow: hidden;
  white-space: nowrap;
  border-right: 2px solid transparent;
  animation:
    typing 5s steps(20, end) infinite,
    cursor-blink 1.5s infinite;
  width: fit-content;
  margin: 0 auto 48px;

  @keyframes typing {
    0% {
      width: 0;
    }
    70% {
      width: 100%;
    }
    100% {
      width: 100%;
    }
  }

  @keyframes cursor-blink {
    0%,
    100% {
      border-right-color: transparent;
    }
    50% {
      border-right-color: @cursor-color;
    }
  }
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

.login-button {
  width: 100%;
  border-radius: 15px;
  background: linear-gradient(135deg, @primary-color 0%, @secondary-color 100%);
  border: none;
  text-align: center;
  font-size: 15px;
  font-weight: 489;
  //如何让按钮中的字体位于按钮正中间？

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

.register-link {
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

.form-wrapper {
  :deep(.ant-form-item) {
    margin-bottom: 36px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(.ant-form-item-label) {
    label {
      font-weight: 600;
      color: #333;
      font-size: 16px;
    }
  }
}
</style>

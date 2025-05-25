<template>
  <div class="avatar-menu-wrapper" @mouseenter="isOpen = true" @mouseleave="isOpen = false">
    <a-avatar :src="userAvatar" class="avatar" />
    <transition name="fan">
      <div v-if="isOpen" class="fan-menu">
        <div class="fan-item" @click="goTo('/')">
          <icon-home style="font-size: 22px" />
          <span>公共图库</span>
        </div>
        <div class="fan-item" @click="goTo('/my_space')">
          <icon-user style="font-size: 22px" />
          <span>我的空间</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { HomeOutlined as IconHome, UserOutlined as IconUser } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/userLoginUserStore.ts'

const isOpen = ref(false)
const router = useRouter()
const loginUserStore = useLoginUserStore()
const userAvatar = loginUserStore.loginUser.userAvatar || require('@/assets/logo.svg')

const goTo = (path: string) => {
  router.push(path)
  isOpen.value = false
}
</script>

<style scoped>
.avatar-menu-wrapper {
  position: relative;
  display: inline-block;
  margin-left: 16px;
}
.avatar {
  border: 2px solid #6190e8;
  box-shadow: 0 2px 8px #b6c6e6;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.avatar:hover {
  box-shadow: 0 4px 16px #a7bfff;
}
.fan-menu {
  position: absolute;
  top: 50%;
  right: 60px;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 18px;
  z-index: 999;
  pointer-events: auto;
}
.fan-item {
  display: flex;
  align-items: center;
  background: linear-gradient(90deg, #a7bfff 0%, #6190e8 100%);
  color: #fff;
  border-radius: 24px;
  padding: 10px 22px 10px 16px;
  font-size: 17px;
  font-weight: 500;
  box-shadow: 0 2px 8px #b6c6e6;
  cursor: pointer;
  opacity: 0.95;
  transform: rotate(-15deg) scale(0.95);
  transition:
    background 0.2s,
    transform 0.2s;
}
.fan-item:hover {
  background: #e6f0ff;
  color: #6190e8;
  transform: rotate(0deg) scale(1.05);
}
.fan-enter-active,
.fan-leave-active {
  transition:
    opacity 0.3s,
    transform 0.3s;
}
.fan-enter-from,
.fan-leave-to {
  opacity: 0;
  transform: scale(0.7) rotate(-30deg);
}
.fan-enter-to,
.fan-leave-from {
  opacity: 1;
  transform: scale(1) rotate(0deg);
}
</style>

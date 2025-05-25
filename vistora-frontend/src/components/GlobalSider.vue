<template>
  <div id="globalSider" v-if="loginUserStore.loginUser.id">
    <a-layout-sider class="sider" width="200" breakpoint="lg">
      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/userLoginUserStore.ts'

const loginUserStore = useLoginUserStore()

const menuItems = [
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
]

const router = useRouter()
const current = ref<string[]>([])

router.afterEach((to) => {
  current.value = [to.path]
})

const doMenuClick = ({ key }: { key: string }) => {
  router.push({ path: key })
}
</script>

<style scoped>
/* 可自定义样式 */
</style>

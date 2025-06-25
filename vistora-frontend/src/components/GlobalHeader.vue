<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.svg" alt="logo" />
            <div class="title">vistora云图库</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-avatar :src="loginUserStore.loginUser.userAvatar"></a-avatar>
              {{ loginUserStore.loginUser.userName ?? '匿名' }}
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout"> <LogoutOutlined /> 退出登录 </a-menu-item>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/userLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'
import { UserOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const current = ref<string[]>(['home'])
const router = useRouter()
const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/user/admin',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: '/space_analyze',
    label: '空间分析',
    title: '空间分析',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/Ytterbria', target: '_blank' }, '其他项目'),
    title: 'vistora',
  },
])

const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

//用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.data === true) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录')
    router.push('/user/login')
  } else {
    message.error('退出登录失败' + res.data.message)
  }
}

router.afterEach((to, from, next) => {
  current.value = [to.path]
})
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>

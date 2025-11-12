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
import { ref, h, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { PictureOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/userLoginUserStore.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController'
import { listSpaceUsingPost } from '@/api/spaceController'
import { Modal } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const current = ref<string[]>([])

const publicMenu = {
  key: '/',
  label: '公共图库',
  icon: () => h(PictureOutlined),
}

const teamSpaces = ref<any[]>([])

const fetchTeamSpaces = async () => {
  // 使用新的团队空间接口
  const res = await listMyTeamSpaceUsingPost()
  if (res?.data?.code === 0 && Array.isArray(res.data.data)) {
    teamSpaces.value = res.data.data
  }
}

const myPrivateSpace = ref<any>(null)

const fetchPrivateSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) return
  const res = await listSpaceUsingPost({
    userId: loginUser.id,
    spaceType: 0,
    current: 1,
    pageSize: 1,
  })
  myPrivateSpace.value =
    res?.data?.code === 0 && res.data.data?.records && res.data.data.records.length > 0
      ? res.data.data.records[0]
      : null
}

const mySpaceMenu = computed(() => {
  if (myPrivateSpace.value) {
    return {
      key: `/space/my/${myPrivateSpace.value.id}`,
      label: '我的空间',
      icon: () => h(UserOutlined),
    }
  } else {
    return {
      key: 'create-private-space',
      label: h(
        'span',
        {
          style: { color: '#1677ff', cursor: 'pointer' },
          title: '创建私人空间',
          onMouseenter: (e: MouseEvent) => {
            const target = e.target as HTMLElement
            target.style.textDecoration = 'underline'
          },
          onMouseleave: (e: MouseEvent) => {
            const target = e.target as HTMLElement
            target.style.textDecoration = 'none'
          },
        },
        [h(UserOutlined), ' 创建私人空间'],
      ),
    }
  }
})

const menuItems = ref<any[]>([])

const buildMenuItems = () => {
  menuItems.value = [
    publicMenu,
    mySpaceMenu.value,
    teamSpaces.value.length > 0
      ? {
          key: 'team-spaces',
          label: '团队空间',
          type: 'group',
          children: teamSpaces.value.map((ts) => ({
            key: `/space/team/${ts.spaceId}`,
            label: ts.space?.spaceName || '团队空间',
            icon: () => h(TeamOutlined),
          })),
        }
      : {
          key: 'create-team-space',
          label: h(
            'span',
            {
              style: { color: '#1677ff', cursor: 'pointer' },
              title: '创建团队空间',
              onMouseenter: (e: MouseEvent) => {
                const target = e.target as HTMLElement
                target.style.textDecoration = 'underline'
              },
              onMouseleave: (e: MouseEvent) => {
                const target = e.target as HTMLElement
                target.style.textDecoration = 'none'
              },
            },
            [h(TeamOutlined), ' 创建团队空间'],
          ),
        },
  ]
}

onMounted(() => {
  Promise.all([fetchPrivateSpace(), fetchTeamSpaces()]).then(buildMenuItems)
})

watch([teamSpaces, myPrivateSpace], buildMenuItems)

router.afterEach((to) => {
  current.value = [to.path]
})

const doMenuClick = ({ key }: { key: string }) => {
  if (key === 'create-team-space' || key === 'create-private-space') {
    router.push('/space/create')
  } else {
    router.push({ path: key })
  }
}
</script>

<style scoped>
/* 可自定义样式 */
</style>

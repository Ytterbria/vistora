<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import WaterfallItem from '@/components/WaterFallItem.vue'
import { listPictureVoByPageUsingPost } from '@/api/pictureController'
import { useLoginUserStore } from '@/stores/userLoginUserStore'
import { listSpaceUsingPost } from '@/api/spaceController'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const spaceId = computed(() => route.params.id as string)
const dataList = ref<API.PictureVO[]>([])
const loading = ref(false)
const noMoreData = ref(false)
const searchParams = ref({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'desc',
  spaceId: spaceId.value,
  userId: loginUserStore.loginUser.id,
})
const spaceInfo = ref<API.SpaceVO | null>(null)
const title = ref('空间')

// const fetchSpaceInfo = async () => {
//   // 用 listSpaceUsingPost 查询空间详情，带上 spaceType
//   const res = await listSpaceUsingPost({
//     id: Number(spaceId.value),
//     current: 1,
//     pageSize: 1,
//   })
//   const space =
//     res?.data?.code === 0 && res.data.data?.records && res.data.data.records.length > 0
//       ? res.data.data.records[0]
//       : null
//   if (space) {
//     spaceInfo.value = space
//   }
// }

const fetchData = async () => {
  if (loading.value || noMoreData.value) return
  loading.value = true
  try {
    // 查询图片时也带上 spaceType
    const res = await listPictureVoByPageUsingPost(searchParams)
    if (res.data.code === 0 && res.data.data?.records) {
      const newData = res.data.data.records
      dataList.value = [...dataList.value, ...newData]
      if (newData.length < searchParams.value.pageSize) {
        noMoreData.value = true
      } else {
        searchParams.value.current++
      }
    }
  } catch (e) {
    message.error('加载图片失败')
  } finally {
    loading.value = false
  }
}

const handlePictureDetail = (id: number) => {
  router.push(`/picture/${id}`)
}

const handleCreatePicture = () => {
  router.push({
    path: '/add_picture',
    query: { spaceId: (spaceId.value as string) || '' },
  })
}

const handleAnalyzeSpace = () => {
  router.push({
    path: '/space_analyze',
    query: { spaceId: spaceId.value },
  })
}

const handleScroll = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const bodyHeight = document.body.scrollHeight
  if (scrollTop + windowHeight >= bodyHeight - 100) {
    fetchData()
  }
}

onMounted(async () => {
  fetchData()
  window.addEventListener('scroll', handleScroll)
})
</script>

<template>
  <div id="space-detail-page">
    <div class="header-bg">
      <div class="header-bar">
        <h2>{{ title }}</h2>
        <a-button type="primary" @click="handleCreatePicture" shape="round" size="large">
          <template #icon>
            <i class="iconfont icon-plus" style="margin-right: 4px"></i>
          </template>
          创建图片
        </a-button>
      </div>
    </div>
    <transition-group name="waterfall" tag="div" class="waterfall-grid">
      <WaterfallItem
        v-for="picture in dataList"
        :key="picture.id"
        :picture="picture"
        @click="handlePictureDetail(picture.id)"
      />
    </transition-group>
    <div v-if="loading && !noMoreData" class="loading-wrapper">
      <a-spin />
      <span>加载中...</span>
    </div>
    <div v-if="noMoreData && dataList.length > 0" class="no-more-data">没有更多数据了</div>
    <div v-if="!loading && dataList.length === 0" class="empty-state">
      <img
        src="https://img.alicdn.com/imgextra/i4/O1CN01Qw0QwC1w6Qw6Qw6Qw_!!6000000000000-2-tps-200-200.png"
        alt="empty"
      />
      <div>暂无图片，快去创建吧！</div>
    </div>
  </div>
</template>

<style scoped>
#space-detail-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}
.header-bar {
  max-width: 1200px;
  margin: 0 auto 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-bar h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0;
  letter-spacing: 2px;
}
.waterfall-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
  align-items: start;
  min-height: 300px;
}
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 32px 0;
  color: #888;
  font-size: 16px;
}
.no-more-data {
  text-align: center;
  color: #aaa;
  margin: 32px 0 0 0;
  font-size: 15px;
  letter-spacing: 1px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 60px 0 0 0;
  color: #bbb;
  font-size: 16px;
}
.empty-state img {
  width: 120px;
  margin-bottom: 16px;
  opacity: 0.7;
}
</style>

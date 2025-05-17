<template>
  <div id="home-page">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="从海量图片中搜索"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        @search="handleSearch"
      />
    </div>

    <!-- 分类 + 标签 -->
    <a-tabs v-model:activeKey="selectedCategory" @change="handleSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="handleSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!-- 瀑布流容器 -->
    <div ref="waterfallContainer" class="waterfall-container">
      <WaterfallItem
        v-for="picture in dataList"
        :key="picture.id"
        :picture="picture"
        @click="handlePictureDetail(picture.id)"
      />
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && !noMoreData" class="loading-wrapper">
      <a-spin />
      <span>加载中...</span>
    </div>

    <div v-if="noMoreData && dataList.length > 0" class="no-more-data">没有更多数据了</div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import WaterfallItem from '@/components/WaterFallItem.vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageWithCacheUsingPost,
} from '@/api/pictureController'

// 响应式数据
const router = useRouter()
const dataList = ref<API.PictureVO[]>([])
const loading = ref<boolean>(false)
const noMoreData = ref<boolean>(false)
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 分类标签数据
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 获取分类标签选项
const getTagCategoryOptions = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      const { categoryList: categories, tagList: tags } = res.data.data
      categoryList.value = categories ?? []
      tagList.value = tags ?? []
      selectedTagList.value = Array(tags?.length ?? 0).fill(false)
    }
  } catch (error) {
    message.error('加载分类标签失败')
  }
}

// 构建请求参数
const buildSearchParams = () => {
  const params = { ...searchParams }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }

  params.tags = selectedTagList.value
    .map((checked, index) => (checked ? tagList.value[index] : null))
    .filter((tag) => tag !== null) as string[]

  return params
}

// 数据加载
const fetchData = async () => {
  if (loading.value || noMoreData.value) return

  loading.value = true
  const params = buildSearchParams()

  try {
    const res = await listPictureVoByPageWithCacheUsingPost(params)
    if (res.data.code === 0 && res.data.data?.records) {
      const newData = res.data.data.records
      dataList.value = [...dataList.value, ...newData]

      // 判断是否还有更多数据
      if (newData.length < params.pageSize!) {
        noMoreData.value = true
      } else {
        searchParams.current = (searchParams.current || 0) + 1
      }
    }
  } catch (error) {
    message.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  searchParams.current = 1
  noMoreData.value = false
  dataList.value = []
  fetchData()
}

const handlePictureDetail = (id) => {
  router.push({
    path: `/picture/${id}`,
  })
}

// 滚动监听
const isBottom = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const bodyHeight = document.body.scrollHeight
  return scrollTop + windowHeight >= bodyHeight - 100 // 提前100px加载
}

const handleScroll = () => {
  if (isBottom()) {
    fetchData()
  }
}

// 生命周期
onMounted(async () => {
  await getTagCategoryOptions()
  fetchData()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.tag-bar {
  margin-bottom: 20px;
}
.search-bar {
  width: 500px;
  margin: 0 auto 24px;
}

.waterfall-container {
  column-count: 4;
  column-gap: 16px;
  padding: 0 24px;
}

.loading-wrapper,
.no-more-data {
  text-align: center;
  margin: 20px 0;
  color: #666;
}

@media (max-width: 768px) {
  .waterfall-container {
    column-count: 2;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .waterfall-container {
    column-count: 3;
  }
}
</style>

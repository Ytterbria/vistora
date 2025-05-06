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
    <div class="waterfall-container">
      <div
        v-for="picture in dataList"
        :key="picture.id"
        class="waterfall-item"
        @click="handlePictureDetail(picture.id)"
      >
        <img
          class="waterfall-image"
          :src="picture.url"
          :alt="picture.name"
          @load="handleImageLoaded"
        />
        <div class="waterfall-overlay">
          <h3>{{ picture.name }}</h3>
          <div class="tags">
            <a-tag color="green">{{ picture.category ?? '默认' }}</a-tag>
            <a-tag v-for="tag in picture.tags" :key="tag">{{ tag }}</a-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" style="text-align: center; margin-top: 70px">
      <a-pagination
        v-model:current="searchParams.current"
        :total="total"
        :page-size="searchParams.pageSize"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
  listPictureVoByPageWithCacheUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

// 导入接口类型
import type { PictureVO } from '@/api/pictureController.ts'
import { useRouter } from 'vue-router'

// 初始化数据
const dataList = ref<PictureVO[]>([])
const total = ref<number>(0)
const loading = ref<boolean>(false)
const router = useRouter()
// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
    selectedTagList.value = Array(res.data.data.tagList?.length ?? 0).fill(false)
  } else {
    message.error('加载分类标签失败，' + res.data.message)
  }
}

const handleSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 分页逻辑
const handlePageChange = (page: number) => {
  searchParams.current = page
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  const params = {
    ...searchParams,
    tags: [],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((checked, index) => {
    if (checked) {
      params.tags.push(tagList.value[index])
    }
  })
  const res = await listPictureVoByPageWithCacheUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records as PictureVO[]
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

const handlePictureDetail = (id) => {
  router.push({
    path: `/picture/${id}`,
  })
}

// 图片加载完成后重新布局
const handleImageLoaded = () => {
  const container = document.querySelector('.waterfall-container')
  if (container) {
    container.dispatchEvent(new Event('resize'))
  }
}

onMounted(() => {
  fetchData()
  getTagCategoryOptions()
})
</script>

<style scoped>
.search-bar {
  width: 500px;
  margin: 0 auto;
}
/* 四周留白 */
#home-page {
  padding: 24px; /* 页面整体留白 */
}

.waterfall-container {
  column-count: 4;
  column-gap: 16px;
  margin: 0 auto; /* 居中显示 */
  padding: 0 24px; /* 左右留白 */
}

/* 悬浮信息框优化 */
.waterfall-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px; /* 增加内边距 */
  background: rgba(255, 255, 255, 0.9); /* 浅色半透明背景 */
  color: #2c2c2c; /* 深灰色文字 */
  opacity: 0;
  transition: all 0.3s ease; /* 平滑过渡 */
  border-radius: 8px 8px 0 0; /* 顶部圆角 */
  backdrop-filter: blur(8px); /* 毛玻璃效果 */
  transform: translateY(100%); /* 初始隐藏 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
}

.waterfall-item:hover .waterfall-overlay {
  opacity: 1;
  transform: translateY(0); /* 悬浮时显示 */
}

/* 其他样式优化 */
.waterfall-item {
  break-inside: avoid;
  margin: 0 8px;
  padding: 8px 0;
  position: relative;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}

.waterfall-item:hover {
  transform: translateY(-8px); /* 上浮效果更明显 */
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08); /* 更柔和的阴影 */
}

.waterfall-image {
  width: 100%;
  height: auto;
  object-fit: cover;
  border-radius: 8px; /* 图片圆角 */
}

.tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tags :deep(.ant-tag) {
  margin: 0;
  background: rgba(255, 255, 255, 0.6); /* 更浅的标签背景 */
  color: #555;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

/* 响应式列数 */
@media (max-width: 768px) {
  .waterfall-container {
    column-count: 2;
  }
}

@media (max-width: 1024px) {
  .waterfall-container {
    column-count: 3;
  }
}

@media (min-width: 1024px) {
  .waterfall-container {
    column-count: 4;
  }
}
</style>

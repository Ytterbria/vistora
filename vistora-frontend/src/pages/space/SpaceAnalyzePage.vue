<template>
  <div class="space-analyze-page">
    <h1>空间分析</h1>
    <div style="margin-bottom: 24px">
      <a-radio-group v-model:value="analyzeMode" :options="analyzeOptions" />
      <a-button type="primary" @click="fetchAnalyzeData" style="margin-left: 16px">分析</a-button>
    </div>
    <div class="analyze-charts">
      <div class="analyze-chart-item" v-if="showCategory">
        <SpaceCategoryAnalyzeChart :data="categoryData" />
      </div>
      <div class="analyze-chart-item" v-if="showTag">
        <SpaceTagAnalyzeChart :data="tagData" />
      </div>
      <div class="analyze-chart-item" v-if="showUser">
        <SpaceUserAnalyzeChart :data="userData" />
      </div>
      <div class="analyze-chart-item" v-if="showUsage">
        <SpaceUsageAnalyzeChart :data="usageData" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import SpaceCategoryAnalyzeChart from '@/components/analyze/SpaceCategoryAnalyzeChart.vue'
import SpaceTagAnalyzeChart from '@/components/analyze/SpaceTagAnalyzeChart.vue'
import SpaceUserAnalyzeChart from '@/components/analyze/SpaceUserAnalyzeChart.vue'
import SpaceUsageAnalyzeChart from '@/components/analyze/SpaceUsageAnalyzeChart.vue'
import {
  getSpaceCategoryAnalyzeUsingPost,
  getSpaceTagAnalyzeUsingPost,
  getSpaceUserAnalyzeUsingPost,
  getSpaceUsageAnalyzeUsingPost,
} from '@/api/spaceAnalyzeController'
import { listSpaceUsingPost } from '@/api/spaceController'
import { useLoginUserStore } from '@/stores/userLoginUserStore'
import { message } from 'ant-design-vue'

const categoryData = ref<API.SpaceCategoryAnalyzeResponse[]>([])
const tagData = ref<API.SpaceTagAnalyzeResponse[]>([])
const userData = ref<API.SpaceUserAnalyzeResponse[]>([])
const usageData = ref<API.SpaceUsageAnalyzeResponse>({})

const userStore = useLoginUserStore()
const loginUser = userStore.loginUser

const analyzeOptions = computed(() => {
  if (loginUser?.userRole === 'admin') {
    return [
      { label: '分析所有空间', value: 'queryAll' },
      { label: '分析公共图库', value: 'queryPub' },
      { label: '分析我的空间', value: 'mySpace' },
    ]
  } else {
    return [{ label: '分析我的空间', value: 'mySpace' }]
  }
})

const analyzeMode = ref<string>('mySpace')

const timeDimensionOptions = [
  { label: '按天', value: 'day' },
  { label: '按周', value: 'week' },
  { label: '按月', value: 'month' },
]
const timeDimension = ref('day')

const showCategory = computed(() => true)
const showTag = computed(() => true)
const showUser = computed(() => true)
const showUsage = ref(false)

const fetchAnalyzeData = async () => {
  categoryData.value = []
  tagData.value = []
  userData.value = []
  usageData.value = {}
  showUsage.value = false
  if (analyzeMode.value === 'mySpace') {
    // 获取当前用户空间id
    const userStore = useLoginUserStore()
    const loginUser = userStore.loginUser
    const userId = userStore.loginUser?.id
    if (!userId) return
    // 用 listSpaceUsingPost 查询私人空间
    const res = await listSpaceUsingPost({
      userId: loginUser.id,
      spaceType: 0,
      current: 1,
      pageSize: 1,
    })
    const spaceId = res.data.data?.records?.[0]?.id
    if (!spaceId) return
    // 查询该空间的标签、分类、用户活跃度、用量
    const [catRes, tagRes, userRes, usageRes] = await Promise.all([
      getSpaceCategoryAnalyzeUsingPost({ spaceId: spaceId, queryAll: false, queryPub: false }),
      getSpaceTagAnalyzeUsingPost({ spaceId: spaceId, queryAll: false, queryPub: false }),
      getSpaceUserAnalyzeUsingPost({
        spaceId: spaceId,
        queryAll: false,
        queryPub: false,
        timeDimension: timeDimension.value,
      }),
      getSpaceUsageAnalyzeUsingPost({ spaceId, queryAll: false, queryPub: false }),
    ])
    if (
      catRes.data.code !== 0 ||
      tagRes.data.code !== 0 ||
      userRes.data.code !== 0 ||
      usageRes.data.code !== 0
    ) {
      message.error('获取空间分析数据失败')
      return
    }
    categoryData.value = catRes.data.data || []
    tagData.value = tagRes.data.data || []
    userData.value = userRes.data.data || []
    usageData.value = usageRes.data.data || {}
    showUsage.value = true
  } else {
    // 所有空间/公共图库
    const queryAll = analyzeMode.value === 'queryAll'
    const queryPub = analyzeMode.value === 'queryPub'
    const [catRes, tagRes, userRes] = await Promise.all([
      getSpaceCategoryAnalyzeUsingPost({ queryAll, queryPub }),
      getSpaceTagAnalyzeUsingPost({ queryAll, queryPub }),
      getSpaceUserAnalyzeUsingPost({ queryAll, queryPub, timeDimension: timeDimension.value }),
    ])

    categoryData.value = catRes.data.data || []
    tagData.value = tagRes.data.data || []
    userData.value = userRes.data.data || []
    showUsage.value = false
  }
}

onMounted(async () => {
  if (!loginUser?.id) {
    await userStore.fetchLoginUser()
  }
  // 设置默认分析模式
  if (loginUser?.userRole === 'admin') {
    analyzeMode.value = 'queryAll'
  } else {
    analyzeMode.value = 'mySpace'
  }
})
</script>

<style scoped>
.space-analyze-page {
  padding: 24px;
}
.analyze-charts {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
}
.analyze-chart-item {
  flex: 1 1 400px;
  min-width: 400px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 16px;
}
</style>

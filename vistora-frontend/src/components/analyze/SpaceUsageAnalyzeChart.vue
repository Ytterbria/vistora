<template>
  <div class="usage-charts">
    <div class="usage-chart-item">
      <div class="usage-title">图片数量</div>
      <div ref="countChartRef" style="width: 100%; height: 260px"></div>
      <div class="usage-desc">
        <span>已用 {{ usedCount }}/{{ maxCount }} 张</span>
        <span class="usage-percent">({{ countPercent }}%)</span>
      </div>
    </div>
    <div class="usage-chart-item">
      <div class="usage-title">空间容量</div>
      <div ref="sizeChartRef" style="width: 100%; height: 260px"></div>
      <div class="usage-desc">
        <span>已用 {{ usedSizeStr }}/{{ maxSizeStr }}</span>
        <span class="usage-percent">({{ sizePercent }}%)</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import * as echarts from 'echarts/core'
import { GaugeChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent } from 'echarts/components'
echarts.use([GaugeChart, TitleComponent, TooltipComponent])

const props = defineProps<{ data: API.SpaceUsageAnalyzeResponse }>()
const countChartRef = ref<HTMLDivElement | null>(null)
const sizeChartRef = ref<HTMLDivElement | null>(null)
let countChart: echarts.ECharts | null = null
let sizeChart: echarts.ECharts | null = null

const usedCount = computed(() => Number(props.data?.usedCount) || 0)
const maxCount = computed(() => Number(props.data?.maxCount) || 0)
const countPercent = computed(() =>
  maxCount.value ? Math.round((usedCount.value / maxCount.value) * 100) : 0,
)

const usedSize = computed(() => Number(props.data?.usedSize) || 0)
const maxSize = computed(() => Number(props.data?.maxSize) || 0)
const sizePercent = computed(() =>
  maxSize.value ? Math.round((usedSize.value / maxSize.value) * 100) : 0,
)

function formatSize(size: number) {
  if (size >= 1024 * 1024 * 1024) return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  if (size >= 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  if (size >= 1024) return (size / 1024).toFixed(2) + ' KB'
  return size + ' B'
}
const usedSizeStr = computed(() => formatSize(usedSize.value))
const maxSizeStr = computed(() => formatSize(maxSize.value))

const renderCountChart = () => {
  if (!countChartRef.value) return
  if (!countChart) {
    countChart = echarts.init(countChartRef.value)
  }
  const option = {
    tooltip: { formatter: '{a} <br/>{b} : {c}%' },
    series: [
      {
        name: '图片数量',
        type: 'gauge',
        detail: { formatter: '{value}%' },
        data: [{ value: countPercent.value, name: '数量使用' }],
        axisLine: { lineStyle: { width: 15 } },
        pointer: { width: 5 },
        min: 0,
        max: 100,
      },
    ],
  }
  countChart.setOption(option)
}

const renderSizeChart = () => {
  if (!sizeChartRef.value) return
  if (!sizeChart) {
    sizeChart = echarts.init(sizeChartRef.value)
  }
  const option = {
    tooltip: { formatter: '{a} <br/>{b} : {c}%' },
    series: [
      {
        name: '空间容量',
        type: 'gauge',
        detail: { formatter: '{value}%' },
        data: [{ value: sizePercent.value, name: '容量使用' }],
        axisLine: { lineStyle: { width: 15 } },
        pointer: { width: 5 },
        min: 0,
        max: 100,
      },
    ],
  }
  sizeChart.setOption(option)
}

onMounted(() => {
  renderCountChart()
  renderSizeChart()
})
watch(
  () => props.data,
  () => {
    renderCountChart()
    renderSizeChart()
  },
  { deep: true },
)
</script>

<style scoped>
.usage-charts {
  display: flex;
  gap: 32px;
  justify-content: center;
}
.usage-chart-item {
  flex: 1 1 300px;
  min-width: 300px;
  background: #fafbfc;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 16px 12px 8px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.usage-title {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 8px;
}
.usage-desc {
  margin-top: 8px;
  color: #666;
  font-size: 15px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.usage-percent {
  color: #409eff;
  font-weight: bold;
}
</style>

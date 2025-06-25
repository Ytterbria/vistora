<template>
  <div ref="chartRef" style="width: 100%; height: 400px"></div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components'
echarts.use([LineChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const props = defineProps<{ data: API.SpaceUserAnalyzeResponse[] }>()
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  // 对数据按 period 升序排序
  const sortedData = [...(props.data || [])].sort((a, b) =>
    String(a.period).localeCompare(String(b.period)),
  )
  const option = {
    title: { text: '空间用户活跃分析', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: sortedData.map((item) => item.period),
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '活跃数',
        type: 'line',
        data: sortedData.map((item) => Number(item.count) || 0),
        smooth: true,
        itemStyle: { color: '#91cc75' },
      },
    ],
  }
  chart.setOption(option)
}

onMounted(renderChart)
watch(() => props.data, renderChart, { deep: true })
</script>

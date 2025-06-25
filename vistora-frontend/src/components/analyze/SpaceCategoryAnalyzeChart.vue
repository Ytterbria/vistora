<template>
  <div ref="chartRef" style="width: 100%; height: 400px"></div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
echarts.use([PieChart, TitleComponent, TooltipComponent, LegendComponent])

const props = defineProps<{ data: API.SpaceCategoryAnalyzeResponse[] }>()
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const option = {
    title: { text: '空间类别分布', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '类别',
        type: 'pie',
        radius: '60%',
        data: (props.data || []).map((item) => ({
          name: item.category,
          value: Number(item.count) || 0,
        })),
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        },
      },
    ],
  }
  chart.setOption(option)
}

onMounted(renderChart)
watch(() => props.data, renderChart, { deep: true })
</script>

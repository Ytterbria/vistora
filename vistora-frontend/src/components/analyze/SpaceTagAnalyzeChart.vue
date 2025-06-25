<template>
  <div ref="chartRef" style="width: 100%; height: 400px"></div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { TitleComponent, TooltipComponent } from 'echarts/components'
// @ts-ignore
import 'echarts-wordcloud'
echarts.use([TitleComponent, TooltipComponent])

const props = defineProps<{ data: API.SpaceTagAnalyzeResponse[] }>()
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const option = {
    title: { text: '空间标签词云', left: 'center' },
    tooltip: {},
    series: [
      {
        type: 'wordCloud',
        shape: 'circle',
        left: 'center',
        top: 'center',
        width: '100%',
        height: '100%',
        sizeRange: [16, 60],
        rotationRange: [-90, 90],
        gridSize: 8,
        drawOutOfBound: false,
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: 'bold',
          color: () => {
            // 随机色
            return `rgb(${Math.round(Math.random() * 160)},${Math.round(Math.random() * 160)},${Math.round(Math.random() * 160)})`
          },
        },
        data: (props.data || []).map((item) => ({
          name: item.tag,
          value: Number(item.count) || 0,
        })),
      },
    ],
  }
  chart.setOption(option)
}

onMounted(renderChart)
watch(() => props.data, renderChart, { deep: true })
</script>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addSpaceUsingPost, listSpaceLevelUsingGet } from '@/api/spaceController'
import { SPACE_TYPE_OPTIONS } from '@/constants/space'

const router = useRouter()
const spaceName = ref('')
const spaceType = ref(SPACE_TYPE_OPTIONS[0].value)
const spaceLevel = ref<number | undefined>(undefined)
const spaceLevels = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  const res = await listSpaceLevelUsingGet()
  if (res?.data?.code === 0 && res.data.data) {
    spaceLevels.value = res.data.data
    if (spaceLevels.value.length > 0) {
      spaceLevel.value = spaceLevels.value[0].value
    }
  }
})

const handleSubmit = async () => {
  if (!spaceName.value) {
    message.warning('请输入空间名称')
    return
  }
  if (spaceLevel.value == undefined) {
    message.warning('请选择空间级别')
    return
  }
  loading.value = true
  const res = await addSpaceUsingPost({
    spaceName: spaceName.value,
    spaceType: spaceType.value,
    spaceLevel: spaceLevel.value,
  })
  loading.value = false
  if (res?.data?.code === 0 && res.data.data) {
    message.success('创建成功')
    router.replace(`/space/${res.data.data}`)
  } else {
    message.error(res?.data?.message || '创建失败')
  }
}
</script>

<template>
  <div style="min-height: 100vh; background: #fff; padding: 32px">
    <h2 style="text-align: center">创建空间</h2>
    <a-form layout="vertical" @submit.prevent="handleSubmit">
      <a-form-item label="空间名称">
        <a-input v-model:value="spaceName" placeholder="请输入空间名称" />
      </a-form-item>
      <a-form-item label="空间类型">
        <a-radio-group v-model:value="spaceType">
          <a-radio v-for="item in SPACE_TYPE_OPTIONS" :key="item.value" :value="item.value">{{
            item.label
          }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="空间级别">
        <a-radio-group v-model:value="spaceLevel">
          <a-radio v-for="item in spaceLevels" :key="item.value" :value="item.value">
            {{ item.text }}
            <span style="color: #888; font-size: 12px; margin-left: 8px">
              (容量: {{ item.maxSize }} MB, 数量: {{ item.maxCount }} 张)
            </span>
          </a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" block>创建空间</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped></style>

<template>
  <div class="batch-upload">
    <a-form layout="vertical">
      <!-- 修改搜索关键词输入 -->
      <a-form-item label="搜索关键词">
        <a-input
          :value="searchText"
          @change="(e) => $emit('update:searchText', e.target.value)"
          placeholder="输入搜索关键词"
        />
      </a-form-item>

      <!-- 修改数量输入 -->
      <a-form-item label="上传数量">
        <a-input-number
          :value="count"
          @change="(value) => $emit('update:count', value)"
          :min="1"
          :max="20"
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" @click="handleBatchUpload" :loading="uploading">
          批量上传
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByBatchUsingPost } from '@/api/pictureController.ts'

const props = defineProps<{
  searchText?: string
  count?: number
  onSuccess?: (data: any) => void
}>()

const emit = defineEmits(['update:searchText', 'update:count'])

const uploading = ref(false)

const handleBatchUpload = async () => {
  if (!props.searchText || !props.count) {
    message.error('请填写完整信息')
    return
  }

  try {
    uploading.value = true
    const res = await uploadPictureByBatchUsingPost({
      searchText: props.searchText,
      count: props.count,
    })

    if (res.data.code === 0) {
      message.success(`成功上传${res.data.data}张图片`)
      props.onSuccess?.(res.data.data)
    }
  } finally {
    uploading.value = false
  }
}
</script>

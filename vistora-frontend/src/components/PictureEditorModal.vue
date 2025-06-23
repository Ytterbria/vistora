<template>
  <a-modal
    :visible="visible"
    title="图片裁剪"
    :footer="null"
    width="800px"
    @cancel="handleCancel"
    @update:visible="(val) => emit('update:visible', val)"
    destroyOnClose
  >
    <div v-if="imageUrl" class="editor-container">
      <a-spin :spinning="uploading">
        <div class="cropper-wrapper">
          <vue-cropper
            ref="cropperRef"
            :img="imageUrl"
            :output-size="1"
            :output-type="'png'"
            :info="true"
            :can-move="true"
            :can-move-box="true"
            :auto-crop="true"
            :fixed-box="false"
            :center-box="true"
            :fixed="false"
            :full="false"
            :enlarge="1"
            :mode="'contain'"
            style="height: 400px; width: 500px"
          />
        </div>
        <div class="editor-actions">
          <a-button @click="rotateLeft">左旋转</a-button>
          <a-button @click="rotateRight">右旋转</a-button>
          <a-button @click="reset">重置</a-button>
          <a-button type="primary" @click="handleCropAndUpload" :loading="uploading"
            >保存并上传</a-button
          >
        </div>
      </a-spin>
    </div>
    <div v-else style="text-align: center; padding: 40px 0">暂无图片</div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, defineProps, defineEmits } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController'

const props = defineProps({
  visible: Boolean,
  imageUrl: String,
  pictureId: Number,
  spaceId: Number,
})
const emit = defineEmits(['update:visible', 'success'])

const cropperRef = ref<any>(null)
const uploading = ref(false)

watch(
  () => props.imageUrl,
  (val) => {
    if (val && cropperRef.value) {
      cropperRef.value.refresh()
    }
  },
)

watch(
  () => props.visible,
  (val) => {
    if (val && props.imageUrl && cropperRef.value) {
      nextTick(() => {
        cropperRef.value.refresh()
      })
    }
  },
)

function rotateLeft() {
  cropperRef.value && cropperRef.value.rotateLeft()
}
function rotateRight() {
  cropperRef.value && cropperRef.value.rotateRight()
}
function reset() {
  cropperRef.value && cropperRef.value.refresh()
}

async function handleCropAndUpload() {
  if (!cropperRef.value) return
  uploading.value = true
  cropperRef.value.getCropBlob(async (blob: Blob) => {
    try {
      if (!blob) throw new Error('裁剪失败')
      const file = new File([blob], 'cropped.png', { type: 'image/png' })
      const res = await uploadPictureUsingPost(
        { id: props.pictureId, spaceId: props.spaceId },
        {},
        file,
      )
      if (res.data.code === 0 && res.data.data) {
        message.success('图片上传成功')
        emit('success', res.data.data)
        emit('update:visible', false)
      } else {
        throw new Error(res.data.message || '上传失败')
      }
    } catch (e: any) {
      message.error(e.message || '上传失败')
    } finally {
      uploading.value = false
    }
  })
}

function handleCancel() {
  emit('update:visible', false)
}
</script>

<script lang="ts">
import { defineComponent } from 'vue'
import { VueCropper } from 'vue-cropper'
export default defineComponent({
  components: { VueCropper },
})
</script>

<style scoped>
.editor-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cropper-wrapper {
  width: 500px;
  height: 400px;
  margin: 0 auto;
  background: #f6f8fa;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.editor-actions {
  margin-top: 24px;
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>

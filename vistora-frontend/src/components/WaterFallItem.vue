<!-- WaterfallItem.vue -->
<template>
  <div class="waterfall-item">
    <div class="card" @click="handleClick">
      <img
        class="card-image"
        :src="picture.thumbnailUrl ?? picture.url"
        :alt="picture.name"
        @load="handleLoad"
      />
      <div class="card-content">
        <h3 class="card-title">{{ picture.name }}</h3>
        <div class="card-tags">
          <a-tag color="green">{{ picture.category ?? '默认' }}</a-tag>
          <a-tag v-for="tag in picture.tags" :key="tag" class="tag">{{ tag }}</a-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

const props = defineProps<{
  picture: API.PictureVO
}>()

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}

const handleLoad = () => {
  window.dispatchEvent(new Event('resize'))
}
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  margin-bottom: 16px;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card-image {
  width: 100%;
  height: auto;
  display: block;
  aspect-ratio: 16 / 9; /* 统一图片比例 */
  object-fit: cover; /* 保持图片比例并裁剪 */
}

.card-content {
  padding: 12px;
}

.card-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.card-tags .tag {
  margin: 0;
  padding: 2px 8px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #555;
  border: none;
  border-radius: 4px;
}
</style>

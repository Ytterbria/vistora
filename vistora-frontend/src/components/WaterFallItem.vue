<template>
  <div class="waterfall-item" @click="handleClick">
    <div class="card">
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
const props = defineProps<{ picture: API.PictureVO }>()
const emit = defineEmits(['click'])
const handleClick = () => emit('click')
const handleLoad = () => window.dispatchEvent(new Event('resize'))
</script>

<style scoped>
.waterfall-item {
  cursor: pointer;
  transition: transform 0.2s;
}
.waterfall-item:hover {
  transform: translateY(-6px) scale(1.03);
}
.card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  height: 100%;
}
.card-image {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  background: #f5f5f5;
}
.card-content {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
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
  font-size: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #555;
  border-radius: 4px;
}
</style>

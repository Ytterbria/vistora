<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useLoginUserStore } from '@/stores/userLoginUserStore.ts'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()
</script>

<style>
#app {
}
</style>

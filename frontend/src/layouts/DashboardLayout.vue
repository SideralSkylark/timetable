<template>
  <div class="flex h-screen bg-gray-50">
    <Sidebar />

    <main class="flex-1 p-8 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/component/layout/Sidebar.vue'
import { RouterView } from 'vue-router'
</script>

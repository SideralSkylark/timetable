<template>
  <div class="mb-6">
    <div class="bg-white rounded-[10px] border border-slate-200 p-5 border-l-[3px] border-l-blue-800">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="bg-blue-50 p-2 rounded-md">
            <component :is="icon" class="w-5 h-5 text-blue-800" />
          </div>
          <div>
            <h1 class="text-xl font-semibold text-gray-900">{{ title }}</h1>
            <p class="text-gray-400 text-sm">{{ subtitle }}</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <slot name="actions" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  icon: Component
  title: string
  subtitle: string
}>()
</script>

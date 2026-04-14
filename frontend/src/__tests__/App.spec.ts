import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../App.vue'
import ToastContainer from '../components/ui/ToastContainer.vue'

describe('App', () => {
  it('mounts and renders ToastContainer', () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          'router-view': true,
          'ToastContainer': true
        }
      }
    })
    expect(wrapper.findComponent(ToastContainer).exists()).toBe(true)
  })
})

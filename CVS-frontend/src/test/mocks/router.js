import { vi } from 'vitest'

// Mock Vue Router
export const mockRouter = {
  push: vi.fn(),
  replace: vi.fn(),
  go: vi.fn(),
  back: vi.fn(),
  forward: vi.fn(),
  currentRoute: {
    value: {
      path: '/',
      name: 'home',
      params: {},
      query: {},
      hash: '',
      meta: {}
    }
  }
}

export const mockRoute = {
  path: '/',
  name: 'home',
  params: {},
  query: {},
  hash: '',
  meta: {}
}
import router from '@/router'
import { routes } from '@/router'

/**
 * Use meta.role to determine if the current user has permission
 * @param roles
 * @param route
 */
function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    // 确保roles是数组
    if (!Array.isArray(roles)) {
      return false
    }
    return roles.some(role => route.meta.roles.includes(role))
  } else {
    return true
  }
}

/**
 * Filter asynchronous routing tables by recursion
 * @param routes asyncRoutes
 * @param roles
 */
export function filterAsyncRoutes(routes, roles) {
  // 暂时不进行任何过滤，直接返回所有路由
  return routes
}

/**
 * Fix routes path for forum
 * @param routes
 */
function fixRoutesPath(routes) {
  routes.forEach(route => {
    if (route.path === '/forum/list') {
      route.path = '/forum/list'
      route.component = () => import('@/views/forum/ForumList.vue')
    }
    if (route.children) {
      fixRoutesPath(route.children)
    }
  })
}

const state = {
  routes: [],
  addRoutes: []
}

const mutations = {
  SET_ROUTES: (state, routes) => {
    state.addRoutes = routes
    state.routes = routes
  },
  RESET_ROUTES: (state) => {
    state.routes = []
    state.addRoutes = []
  },
  FIX_ROUTES: (state, routes) => {
    state.routes = routes
  }
}

const actions = {
  generateRoutes({ commit }, roles) {
    return new Promise(resolve => {
      // 确保roles是数组，如果不是则设置为空数组
      if (!Array.isArray(roles)) {
        roles = []
      }
      
      // 获取所有路由，并提取/home的子路由作为权限路由
      const homeRoute = routes.find(route => route.path === '/home')
      const allRoutes = homeRoute ? homeRoute.children || [] : []
      
      // 根据角色过滤路由
      let accessedRoutes
      if (roles.includes('ADMIN')) {
        // 对ADMIN用户，确保系统管理菜单下包含所有预约管理
        accessedRoutes = [...allRoutes]
        // 验证系统管理菜单是否存在并包含预约管理
        const systemRoute = accessedRoutes.find(route => route.name === 'System')
        if (systemRoute && !systemRoute.children) {
          systemRoute.children = []
        }
      } else {
        accessedRoutes = filterAsyncRoutes(allRoutes, roles)
      }
      
      // Fix routes path - disabled to prevent route structure issues
      // fixRoutesPath(accessedRoutes)
      
      commit('SET_ROUTES', accessedRoutes)
      resolve(accessedRoutes)
    })
  },
  resetRoutes({ commit }) {
    commit('RESET_ROUTES')
  },
  fixRoutes({ commit }, routes) {
    commit('FIX_ROUTES', routes)
  }
}

const getters = {
  fixedRoutes: state => {
    return state.routes.filter(route => {
      return route.path !== '/forum/list' || route.component
    })
  },
  permissionRoutes: state => state.routes
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
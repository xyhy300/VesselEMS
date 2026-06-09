<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="main-aside">
      <!-- Logo 区域 -->
      <div class="aside-logo">
        <el-icon :size="28"><Ship /></el-icon>
        <span v-show="!isCollapse" class="logo-text">VesselEMS</span>
      </div>

      <!-- 菜单（可滚动） -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#1e293b"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        class="aside-menu"
      >
        <el-menu-item index="/workspace/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-sub-menu index="system" v-if="hasMenu(2)">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="hasMenu(21)" index="/workspace/users">用户管理</el-menu-item>
          <el-menu-item v-if="hasMenu(22)" index="/workspace/roles">角色管理</el-menu-item>
          <el-menu-item v-if="hasMenu(23)" index="/workspace/menus">菜单管理</el-menu-item>
          <el-menu-item v-if="hasMenu(24)" index="/workspace/permissions">权限管理</el-menu-item>
          <el-menu-item v-if="hasMenu(25)" index="/workspace/depts">部门管理</el-menu-item>
          <el-menu-item v-if="hasMenu(26)" index="/workspace/config">系统配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="nl2sql" v-if="hasMenu(4)">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>NL2SQL分析</span>
          </template>
          <el-menu-item v-if="hasMenu(41)" index="/workspace/datasources">数据源管理</el-menu-item>
          <el-menu-item v-if="hasMenu(42)" index="/workspace/nlquery">自然语言查询</el-menu-item>
          <el-menu-item v-if="hasMenu(43)" index="/workspace/dialogs">查询历史</el-menu-item>
          <el-menu-item v-if="hasMenu(44)" index="/workspace/models">模型配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="rag" v-if="hasMenu(3)">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>RAG管理</span>
          </template>
          <el-menu-item v-if="hasMenu(31)" index="/workspace/documents">文档管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户中心</span>
          </template>
          <el-menu-item index="/workspace/profile">用户个人中心</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="main-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="22"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/workspace/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="hover">
            <div class="user-avatar-area">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="user-name">{{ displayName }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Ship, DataBoard, Setting, Cpu, User, Fold, Expand,
  UserFilled, ArrowDown, SwitchButton, ChatDotRound
} from '@element-plus/icons-vue'
import { userStore, logout } from '../stores/user.js'
import { hasPermission, hasMenu, loadPermissions, permissionStore } from '../stores/permissions.js'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbTitle = computed(() => {
  return route.meta?.title || ''
})

const displayName = computed(() => {
  return userStore.user?.username || ''
})

function goProfile() {
  router.push('/workspace/profile')
}

function handleLogout() {
  logout()
  router.push('/login')
}

onMounted(() => {
  if (!permissionStore.loaded) {
    loadPermissions()
  }
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.main-aside {
  background: #1e293b;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.aside-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #3b82f6;
  border-bottom: 1px solid #334155;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
  white-space: nowrap;
}

.aside-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.aside-menu .el-sub-menu .el-menu {
  background-color: #0f172a;
}

.aside-menu .el-menu-item:hover {
  background-color: #334155;
}

.aside-menu .el-menu-item.is-active {
  background-color: #2563eb;
}

.main-header {
  height: 60px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #6b7280;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #2563eb;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-avatar-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-avatar-area:hover {
  background: #f1f5f9;
}

.user-name {
  font-size: 14px;
  color: #374151;
}

.arrow-icon {
  color: #9ca3af;
  font-size: 12px;
}

.main-content {
  background: #f1f5f9;
  padding: 20px;
  overflow-y: auto;
}
</style>
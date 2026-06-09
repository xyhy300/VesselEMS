<template>
  <div class="dialog-history">
    <el-card class="table-card" shadow="never">
      <el-table :data="sessions" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="首条问题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.firstQuestion || '-' }}</template>
        </el-table-column>
        <el-table-column prop="count" label="消息数" width="80" align="center" />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ fmtTime(row.firstTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="View" link @click="handleView(row.sessionId)">详情</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDel(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="会话详情" width="800px">
      <div v-for="(d, idx) in detailDialogs" :key="idx" class="detail-block">
        <div class="detail-q">{{ d.question || '-' }}</div>
        <div class="detail-sql" v-if="d.sql"><code>{{ d.sql }}</code></div>
        <div class="detail-error" v-if="d.error">错误：{{ d.error }}</div>
        <div v-if="d.result && d.result.length" style="margin-top:4px">
          <el-table :data="d.result" stripe border size="small">
            <el-table-column v-for="col in getCols(d.result)" :key="col" :prop="col" :label="col" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <el-empty v-if="detailDialogs.length===0" description="暂无记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View, Delete } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const sessions = ref([]), loading = ref(false)

async function fetch() {
  loading.value = true
  try {
    sessions.value = (await request.get('/api/dialog/sessions')) || []
  } catch { sessions.value = [] }
  finally { loading.value = false }
}

function fmtTime(t) {
  if (!t) return ''
  try { return new Date(t).toLocaleString('zh-CN', { hour12: false }) } catch { return t }
}

const detailVisible = ref(false), detailDialogs = ref([])
async function handleView(sessionId) {
  detailVisible.value = true; detailDialogs.value = []
  try {
    const dialogs = (await request.get(`/api/dialog/session/${sessionId}`)) || []
    detailDialogs.value = dialogs.map(d => {
      let c = {}
      try { c = JSON.parse(d.content || '{}') } catch {}
      return c
    })
  } catch { detailDialogs.value = [] }
}

async function handleDel(row) {
  try {
    const dialogs = (await request.get(`/api/dialog/session/${row.sessionId}`)) || []
    for (const d of dialogs) {
      await request.delete(`/api/dialog/${d.id}`)
    }
    ElMessage.success('已删除'); await fetch()
  } catch { ElMessage.error('删除失败') }
}

function getCols(rows) {
  return rows && rows.length ? Object.keys(rows[0]) : []
}

onMounted(fetch)
</script>

<style scoped>
.dialog-history { height: 100%; display: flex; flex-direction: column; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.detail-block { margin-bottom: 12px; padding: 8px; border: 1px solid #e5e7eb; border-radius: 6px; }
.detail-q { font-size: 14px; color: #2563eb; font-weight: 500; }
.detail-sql { margin-top: 4px; background: #1e293b; color: #e2e8f0; padding: 6px 10px; border-radius: 4px; font-size: 12px; overflow-x: auto; }
.detail-sql code { white-space: pre-wrap; word-break: break-all; }
.detail-error { margin-top: 4px; color: #ef4444; font-size: 13px; }
</style>
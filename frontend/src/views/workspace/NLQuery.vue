<template>
  <div class="nl-chat">
    <!-- 左侧会话列表 -->
    <div class="session-panel">
      <div class="session-header">
        <el-button type="primary" size="small" :icon="Plus" @click="newSession">新会话</el-button>
      </div>
      <div class="session-list" v-loading="sessionLoading">
        <div v-for="s in sessions" :key="s.sessionId" class="session-item"
             :class="{ active: s.sessionId === currentSessionId }"
             @click="selectSession(s.sessionId)">
          <div class="session-title">{{ s.firstQuestion || '新会话' }}</div>
          <div class="session-meta">{{ s.count }} 条 · {{ fmtTime(s.firstTime) }}</div>
        </div>
        <el-empty v-if="sessions.length===0" description="暂无会话" :image-size="60" />
      </div>
    </div>

    <!-- 右侧对话区 -->
    <div class="chat-panel">
      <!-- 顶部选择器 -->
      <div class="chat-top">
        <el-select v-model="dsId" placeholder="数据源" size="small" style="width:160px">
          <el-option v-for="d in dsList" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-select v-model="modelId" placeholder="模型" size="small" style="width:160px">
          <el-option v-for="m in modelList" :key="m.id" :label="m.name" :value="m.id" />
        </el-select>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="msgBox">
        <div v-for="(m, idx) in messages" :key="idx" class="msg-block">
          <div class="msg-user">{{ m.question }}</div>
          <div class="msg-sql" v-if="m.sql">
            <strong>SQL：</strong><code>{{ m.sql }}</code>
            <el-tag v-if="m.status==='pending'" size="small" type="warning" style="margin-left:8px">待确认</el-tag>
            <el-tag v-if="m.status==='error'" size="small" type="danger" style="margin-left:8px">失败</el-tag>
          </div>
          <div class="msg-error" v-if="m.error">{{ m.error }}</div>
          <div class="msg-result" v-if="m.result && m.result.length">
            <strong>结果（{{ m.result.length }} 行）：</strong>
            <el-button size="small" :icon="Download" link @click="downloadResult(m)">下载JSON</el-button>
            <el-table :data="m.result" stripe border size="small" style="margin-top:4px">
              <el-table-column v-for="col in getCols(m.result)" :key="col" :prop="col" :label="col" min-width="100" show-overflow-tooltip />
            </el-table>
          </div>
          <div class="msg-actions" v-if="m.status==='pending' && m.dialogId">
            <el-button type="primary" size="small" :icon="Check" @click="confirmExecute(m)">确认执行</el-button>
          </div>
        </div>
        <div v-if="querying" class="msg-loading"><el-icon class="is-loading"><Loading /></el-icon> 思考中...</div>
      </div>

      <!-- 底部输入框 -->
      <div class="chat-input">
        <el-input v-model="question" type="textarea" :rows="2" placeholder="输入问题..." @keydown.enter.exact.prevent="handleQuery" />
        <el-button type="primary" :icon="Promotion" :loading="querying" @click="handleQuery" style="margin-left:8px;height:auto">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Check, Promotion, Loading } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const dsList = ref([]), modelList = ref([])
const dsId = ref(null), modelId = ref(null)
const sessions = ref([]), sessionLoading = ref(false), currentSessionId = ref(null)
const messages = ref([]), question = ref(''), querying = ref(false)
const msgBox = ref(null)

async function fetchDs() {
  try { const d = await request.get('/api/ds'); dsList.value = (d || []).filter(x => x.status === 1) } catch { dsList.value = [] }
}
async function fetchModel() {
  try { const d = await request.get('/api/model'); modelList.value = (d || []).filter(x => x.status === 1) } catch { modelList.value = [] }
}
async function fetchSessions() {
  sessionLoading.value = true
  try { sessions.value = (await request.get('/api/dialog/sessions')) || [] } catch { sessions.value = [] }
  finally { sessionLoading.value = false }
}

function fmtTime(t) {
  if (!t) return ''
  try { return new Date(t).toLocaleString('zh-CN', { hour12: false }) } catch { return t }
}

function newSession() {
  currentSessionId.value = null
  messages.value = []
  scrollBottom()
}

async function selectSession(id) {
  currentSessionId.value = id
  messages.value = []
  try {
    const dialogs = (await request.get(`/api/dialog/session/${id}`)) || []
    messages.value = dialogs.map(d => {
      let c = {}
      try { c = JSON.parse(d.content || '{}') } catch {}
      return { dialogId: d.id, ...c }
    })
  } catch { messages.value = [] }
  scrollBottom()
}

async function handleQuery() {
  if (!dsId.value || !modelId.value || !question.value.trim()) {
    ElMessage.warning('请选择数据源、模型并输入问题'); return
  }
  querying.value = true
  try {
    const body = { dsId: dsId.value, modelId: modelId.value, question: question.value }
    if (currentSessionId.value) body.sessionId = currentSessionId.value
    const d = await request.post('/api/dialog', body)
    currentSessionId.value = d.sessionId
    const msg = { ...d, dialogId: d.dialogId }
    messages.value.push(msg)
    question.value = ''
    if (d.needConfirm) {
      ElMessage.warning('该SQL包含写操作，请确认后执行')
    } else if (d.error) {
      ElMessage.error(d.error)
    }
    await fetchSessions()
    scrollBottom()
  } catch {} finally { querying.value = false }
}

async function confirmExecute(m) {
  try {
    await ElMessageBox.confirm(`确认执行此SQL？${m.sql}`, '写操作确认', { type: 'warning' })
    const d = await request.post(`/api/dialog/${m.dialogId}/execute`)
    m.result = d.result || []
    m.status = d.error ? 'error' : 'success'
    m.error = d.error || null
    scrollBottom()
  } catch {}
}

function getCols(rows) {
  return rows && rows.length ? Object.keys(rows[0]) : []
}

function downloadResult(m) {
  const blob = new Blob([JSON.stringify(m.result, null, 2)], { type: 'application/json' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob); a.download = 'result.json'; a.click()
}

function scrollBottom() {
  nextTick(() => {
    const el = msgBox.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

onMounted(() => { fetchDs(); fetchModel(); fetchSessions() })
</script>

<style scoped>
.nl-chat { height: 100%; display: flex; gap: 0; overflow: hidden; }
.session-panel { width: 220px; background: #fff; border-right: 1px solid #e5e7eb; display: flex; flex-direction: column; flex-shrink: 0; }
.session-header { padding: 10px; border-bottom: 1px solid #e5e7eb; }
.session-list { flex: 1; overflow-y: auto; }
.session-item { padding: 10px 12px; cursor: pointer; border-bottom: 1px solid #f1f5f9; }
.session-item:hover, .session-item.active { background: #eff6ff; }
.session-title { font-size: 13px; color: #1e293b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-meta { font-size: 11px; color: #9ca3af; margin-top: 2px; }

.chat-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #f8fafc; }
.chat-top { padding: 8px 12px; background: #fff; border-bottom: 1px solid #e5e7eb; display: flex; gap: 8px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 12px; }
.msg-block { margin-bottom: 16px; }
.msg-user { background: #2563eb; color: #fff; padding: 8px 12px; border-radius: 8px; display: inline-block; max-width: 80%; font-size: 14px; }
.msg-sql { margin-top: 6px; background: #1e293b; color: #e2e8f0; padding: 8px 12px; border-radius: 6px; font-size: 13px; overflow-x: auto; }
.msg-sql code { font-family: monospace; white-space: pre-wrap; word-break: break-all; }
.msg-error { margin-top: 4px; color: #ef4444; font-size: 13px; padding: 4px 8px; background: #fef2f2; border-radius: 4px; }
.msg-result { margin-top: 6px; font-size: 13px; }
.msg-actions { margin-top: 6px; }
.msg-loading { padding: 8px 16px; color: #6b7280; font-size: 14px; }

.chat-input { padding: 10px 12px; background: #fff; border-top: 1px solid #e5e7eb; display: flex; align-items: flex-end; }
</style>
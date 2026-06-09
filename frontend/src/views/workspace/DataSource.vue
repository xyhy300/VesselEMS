<template>
  <div class="datasource-manage">
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增数据源</el-button>
      </div>
      <el-table :data="pagedData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="数据库名" width="140" />
        <el-table-column prop="url" label="连接地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" :icon="Connection" link @click="handleTest(row)">测试</el-button>
            <el-button type="warning" size="small" :icon="DataBoard" link @click="handleSchema(row)">结构</el-button>
            <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
              <template #reference><el-button type="danger" size="small" :icon="Delete" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]" :total="list.length" layout="total,sizes,prev,pager,next" background />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" width="520px" @close="resetForm">
      <el-form ref="fRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="连接地址" prop="url"><el-input v-model="form.url" placeholder="jdbc:mysql://host:port/db" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="schemaVisible" title="表结构" width="700px">
      <div v-for="t in schemaTables" :key="t.table" style="margin-bottom:16px">
        <h4 style="margin:0 0 8px;color:#2563eb">{{ t.table }}</h4>
        <el-table :data="t.columns" size="small" border>
          <el-table-column prop="name" label="字段名" />
          <el-table-column prop="type" label="类型" />
          <el-table-column prop="size" label="长度" />
        </el-table>
      </div>
      <el-empty v-if="schemaTables.length===0" description="暂无数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Connection, DataBoard } from '@element-plus/icons-vue'
import request from '../../api/request.js'

const list = ref([]), loading = ref(false)
const page = ref(1), size = ref(10)
const pagedData = computed(() => list.value.slice((page.value - 1) * size.value, page.value * size.value))

async function fetch() {
  loading.value = true
  try {
    const d = await request.get('/api/ds')
    list.value = (d || []).map(fmt)
  } catch { list.value = [] }
  finally { loading.value = false }
}

function fmt(r) {
  let t = ''
  if (r.createTime) {
    if (Array.isArray(r.createTime)) {
      const [y, m, d, h = 0, mm = 0, s = 0] = r.createTime
      t = `${y}/${String(m).padStart(2, '0')}/${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    } else {
      t = new Date(r.createTime).toLocaleString('zh-CN', { hour12: false })
    }
  }
  return { ...r, createTime: t }
}

async function handleStatusToggle(row) {
  try {
    await request.put(`/api/ds/${row.id}`, { status: row.status })
    ElMessage.success('状态已更新')
  } catch { row.status = row.status === 1 ? 0 : 1 }
}

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), submitting = ref(false), fRef = ref(null)
const form = reactive({ name: '', url: '', username: '', password: '', status: 1 })
const rules = { name: [{ required: true, message: '请输入名称' }], url: [{ required: true, message: '请输入连接地址' }] }

function handleAdd() { isEdit.value = false; editId.value = null; resetRaw(); dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; form.name = row.name; form.url = row.url; form.username = row.username; form.password = row.password; form.status = row.status; dialogVisible.value = true }
function resetRaw() { form.name = ''; form.url = ''; form.username = ''; form.password = ''; form.status = 1 }
function resetForm() { resetRaw(); fRef.value?.resetFields() }

async function handleSubmit() {
  if (!fRef.value) return
  try { await fRef.value.validate() } catch { return }
  submitting.value = true
  try {
    if (isEdit.value) { await request.put(`/api/ds/${editId.value}`, { ...form }); ElMessage.success('已修改') }
    else { await request.post('/api/ds', { ...form }); ElMessage.success('已新增') }
    dialogVisible.value = false; resetRaw(); await fetch()
  } catch {} finally { submitting.value = false }
}

async function handleDelete(row) {
  try { await request.delete(`/api/ds/${row.id}`); ElMessage.success('已删除'); await fetch() } catch {}
}

async function handleTest(row) {
  try {
    const ok = await request.post(`/api/ds/${row.id}/test`)
    ElMessage(ok ? { message: '连接成功', type: 'success' } : { message: '连接失败', type: 'error' })
  } catch { ElMessage.error('测试失败') }
}

const schemaVisible = ref(false), schemaTables = ref([])
async function handleSchema(row) {
  schemaVisible.value = true; schemaTables.value = []
  try {
    const d = await request.get(`/api/ds/${row.id}/schema`)
    schemaTables.value = d || []
  } catch { ElMessage.error('获取失败') }
}

onMounted(fetch)
</script>

<style scoped>
.datasource-manage { height: 100%; display: flex; flex-direction: column; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.toolbar { margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; flex-shrink: 0; }
</style>
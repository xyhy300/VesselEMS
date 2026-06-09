const Dashboard = () => import('../views/workspace/Dashboard.vue')
const UserManage = () => import('../views/workspace/UserManage.vue')
const RoleManage = () => import('../views/workspace/RoleManage.vue')
const MenuManage = () => import('../views/workspace/MenuManage.vue')
const PermissionManage = () => import('../views/workspace/PermissionManage.vue')
const DeptManage = () => import('../views/workspace/DeptManage.vue')
const DocumentManage = () => import('../views/workspace/DocumentManage.vue')
const UserProfile = () => import('../views/workspace/UserProfile.vue')
const ConfigManage = () => import('../views/workspace/ConfigManage.vue')
const DataSource = () => import('../views/workspace/DataSource.vue')
const NLQuery = () => import('../views/workspace/NLQuery.vue')
const Dialog = () => import('../views/workspace/Dialog.vue')
const Model = () => import('../views/workspace/Model.vue')

export default [
  { path: 'dashboard', name: 'Dashboard', component: Dashboard, meta: { title: '仪表盘' } },
  { path: 'users', name: 'UserManage', component: UserManage, meta: { title: '用户管理' } },
  { path: 'roles', name: 'RoleManage', component: RoleManage, meta: { title: '角色管理' } },
  { path: 'menus', name: 'MenuManage', component: MenuManage, meta: { title: '菜单管理' } },
  { path: 'permissions', name: 'PermissionManage', component: PermissionManage, meta: { title: '权限管理' } },
  { path: 'depts', name: 'DeptManage', component: DeptManage, meta: { title: '部门管理' } },
  { path: 'documents', name: 'DocumentManage', component: DocumentManage, meta: { title: '文档管理' } },
  { path: 'profile', name: 'UserProfile', component: UserProfile, meta: { title: '用户个人中心' } },
  { path: 'config', name: 'ConfigManage', component: ConfigManage, meta: { title: '系统配置' } },
  { path: 'datasources', name: 'DataSource', component: DataSource, meta: { title: '数据源管理' } },
  { path: 'nlquery', name: 'NLQuery', component: NLQuery, meta: { title: '自然语言查询' } },
  { path: 'dialogs', name: 'Dialog', component: Dialog, meta: { title: '查询历史' } },
  { path: 'models', name: 'Model', component: Model, meta: { title: '模型配置' } }
]

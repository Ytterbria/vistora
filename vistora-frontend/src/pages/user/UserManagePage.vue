<template>
  <div id="userManagePage">
    <a-row :gutter="16" style="margin-bottom: 24px">
      <a-form layout="inline" :model="searchParams" @finish="handleSearch" class="search-form">
        <a-form-item label="账号">
          <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </a-row>

    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'id'">
          {{ record.id }}
        </template>

        <template v-if="column.key === 'userAccount'">
          {{ record.userAccount }}
        </template>

        <template v-if="column.key === 'userName'">
          {{ record.userName }}
        </template>

        <template v-else-if="column.key === 'userAvatar'">
          <img :src="record.userAvatar" alt="avatar" class="avatar-image" />
        </template>
        <template v-else-if="column.key === 'userProfile'">
          {{ record.userProfile }}
        </template>
        <template v-else-if="column.key === 'userRole'">
          <span>
            <a-tag :color="getRoleColor(record.userRole)">
              {{ record.userRole }}
            </a-tag>
          </span>
        </template>
        <template v-else-if="column.key === 'createTime'">
          <span>
            {{ formatDateTime(record.createTime) }}
          </span>
        </template>
        <template v-else-if="column.key === 'action'">
          <span>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" @click="handleDelete(record)">删除</a-button>
          </span>
        </template>
      </template>
    </a-table>
    <a-col :span="12">
      <a-button type="primary" @click="showAddUserModal = true">
        <plus-outlined /> 添加用户
      </a-button>
    </a-col>
    <!-- 添加用户模态框 -->
    <a-modal
      v-model:visible="showAddUserModal"
      title="添加用户"
      @ok="handleAddUser"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="用户名">
          <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="账号">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="头像URL">
          <a-input v-model:value="formState.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介">
          <a-input v-model:value="formState.userProfile" placeholder="请输入简介" />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-select v-model:value="formState.userRole" placeholder="请选择用户角色">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑用户模态框 -->
    <a-modal
      v-model:visible="showEditUserModal"
      title="编辑用户"
      @ok="handleUpdateUser"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="editFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="ID">
          <a-input v-model:value="editFormState.id" placeholder="ID" disabled />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="editFormState.userName" placeholder="请输入用户名" />
        </a-form-item>

        <a-form-item label="头像URL">
          <a-input v-model:value="editFormState.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介">
          <a-input v-model:value="editFormState.userProfile" placeholder="请输入简介" />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-select v-model:value="editFormState.userRole" placeholder="请选择用户角色">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { PlusOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  listUserVoUsingPost,
  addUserUsingPost,
  deleteUserUsingPost,
  updateUserUsingPost,
} from '@/api/userController.ts'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    key: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const dataList = ref<API.UserManageVO[]>([])
const total = ref(0)

// 查询参数,搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 5,
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 5,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

const fetchData = async () => {
  const res = await listUserVoUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
    pagination.value.total = total.value
  } else {
    message.error('获取数据失败' + res.data.message)
  }
}

const handleTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const handleSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 添加用户
const showAddUserModal = ref(false)
const formState = reactive<API.UserAddRequest>({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const handleAddUser = async () => {
  try {
    const res = await addUserUsingPost(formState)
    if (res.data.code === 0) {
      message.success('用户添加成功')
      showAddUserModal.value = false
      fetchData()
    } else {
      message.error('用户添加失败' + res.data.message)
    }
  } catch (error) {
    message.error('用户添加失败' + error)
  }
}

// 编辑用户
const showEditUserModal = ref(false)
const editFormState = reactive<API.UserUpdateRequest>({
  id: 0,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const handleEdit = (record: API.UserManageVO) => {
  Object.assign(editFormState, record)
  showEditUserModal.value = true
}

const handleUpdateUser = async () => {
  try {
    const res = await updateUserUsingPost(editFormState) // 确认调用 updateUserUsingPost 接口
    if (res.data.code === 0) {
      message.success('用户更新成功')
      showEditUserModal.value = false
      fetchData()
    } else {
      message.error('用户更新失败' + res.data.message)
    }
  } catch (error) {
    message.error('用户更新失败' + error)
  }
}

const handleDelete = async (record: API.UserManageVO) => {
  try {
    const res = await deleteUserUsingPost({ id: record.id }) // 确认调用 deleteUserUsingPost 接口
    if (res.data.code === 0) {
      message.success('用户删除成功')
      fetchData()
    } else {
      message.error('用户删除失败' + res.data.message)
    }
  } catch (error) {
    message.error('用户删除失败' + error)
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 获取用户角色颜色
const getRoleColor = (role: string) => {
  switch (role) {
    case 'admin':
      return 'red'
    case 'user':
      return 'blue'
    default:
      return 'green'
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.ant-table {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.table-row-even {
  background-color: #fafafa;
}

.table-row-odd {
  background-color: #ffffff;
}

.table-row-even:hover,
.table-row-odd:hover {
  background-color: #e6f7ff;
}

.avatar-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.role-icon {
  margin-right: 8px;
  font-size: 16px;
}

.ant-table-thead th {
  background-color: #f0f2f5;
}

.ant-table-tbody td {
  padding: 16px;
}

.ant-table-pagination.ant-pagination {
  margin-top: 16px;
}

.ant-form-item {
  margin-bottom: 16px;
}
</style>

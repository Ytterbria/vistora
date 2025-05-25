<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>空间管理</h2>
      <a-button type="primary" @click="showAddSpaceModal = true">+ 创建空间</a-button>
    </a-flex>

    <a-form layout="inline" :model="searchParams" @finish="handleSearch" style="margin: 16px 0">
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="searchParams.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-input v-model:value="searchParams.spaceLevel" placeholder="请输入空间级别" allow-clear />
      </a-form-item>
      <a-form-item label="用户ID" name="userId">
        <a-input v-model:value="searchParams.userId" placeholder="请输入用户ID" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>

    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'id'">
          {{ record.id }}
        </template>
        <template v-if="column.dataIndex === 'spaceName'">
          {{ record.spaceName }}
        </template>
        <template v-if="column.dataIndex === 'spaceLevel'">
          {{ SPACE_LEVEL_MAP[record.spaceLevel] ?? record.spaceLevel }}
        </template>
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>已用数量: {{ record.spaceCount ?? 0 }} / {{ record.maxCount ?? '-' }}</div>
          <div>
            已用空间: {{ (record.spaceSize / 1024 / 1024).toFixed(2) }} MB /
            {{ (record.maxSize / 1024 / 1024).toFixed(2) }} MB
          </div>
        </template>
        <template v-if="column.dataIndex === 'userId'">
          {{ record.userId }}
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ formatDateTime(record.editTime) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增空间模态框 -->
    <a-modal
      v-model:visible="showAddSpaceModal"
      title="创建空间"
      @ok="handleAddSpace"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="addFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="空间名称">
          <a-input v-model:value="addFormState.spaceName" placeholder="请输入空间名称" />
        </a-form-item>
        <a-form-item label="空间级别">
          <a-input v-model:value="addFormState.spaceLevel" placeholder="请输入空间级别(数字)" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑空间模态框 -->
    <a-modal
      v-model:visible="showEditSpaceModal"
      title="编辑空间"
      @ok="handleUpdateSpace"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="editFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="ID">
          <a-input v-model:value="editFormState.id" disabled />
        </a-form-item>
        <a-form-item label="空间名称">
          <a-input v-model:value="editFormState.spaceName" placeholder="请输入空间名称" />
        </a-form-item>
        <a-form-item label="空间级别">
          <a-input v-model:value="editFormState.spaceLevel" placeholder="请输入空间级别(数字)" />
        </a-form-item>
        <a-form-item label="最大数量">
          <a-input v-model:value="editFormState.maxCount" placeholder="最大数量" />
        </a-form-item>
        <a-form-item label="最大空间(MB)">
          <a-input v-model:value="editFormState.maxSize" placeholder="最大空间(MB)" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  listSpaceUsingPost,
  addSpaceUsingPost,
  updateSpaceUsingPost,
  deleteSpaceUsingPost,
} from '@/api/spaceController.ts'
import dayjs from 'dayjs'
import { SPACE_LEVEL_MAP } from '../../constants/space.ts'

const columns = [
  { title: 'id', dataIndex: 'id', width: 80 },
  { title: '空间名称', dataIndex: 'spaceName' },
  { title: '空间级别', dataIndex: 'spaceLevel' },
  { title: '使用情况', dataIndex: 'spaceUseInfo' },
  { title: '用户 id', dataIndex: 'userId', width: 80 },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '编辑时间', dataIndex: 'editTime' },
  { title: '操作', key: 'action' },
]

const dataList = ref<API.SpaceVO[]>([])
const total = ref(0)

const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const pagination = computed(() => ({
  current: searchParams.current ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

const fetchData = async () => {
  const res = await listSpaceUsingPost({ ...searchParams })
  if (res.data && res.data.data) {
    console.log(res.data.data)
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取空间数据失败' + (res.data?.message || ''))
  }
}

const handleSearch = () => {
  searchParams.current = 1
  fetchData()
}

const handleTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const showAddSpaceModal = ref(false)
const addFormState = reactive<API.SpaceAddRequest>({
  spaceName: '',
  spaceLevel: undefined,
})

const handleAddSpace = async () => {
  try {
    const res = await addSpaceUsingPost(addFormState)
    if (res.data.code === 0) {
      message.success('空间创建成功')
      showAddSpaceModal.value = false
      addFormState.spaceName = ''
      addFormState.spaceLevel = undefined
      fetchData()
    } else {
      message.error('空间创建失败: ' + res.data.message)
    }
  } catch (e) {
    message.error('空间创建失败: ' + e)
  }
}

const showEditSpaceModal = ref(false)
const editFormState = reactive<API.SpaceUpdateRequest>({
  id: undefined,
  spaceName: '',
  spaceLevel: undefined,
  maxCount: undefined,
  maxSize: undefined,
})

const handleEdit = (record: API.SpaceVO) => {
  Object.assign(editFormState, record)
  showEditSpaceModal.value = true
}

const handleUpdateSpace = async () => {
  try {
    const res = await updateSpaceUsingPost(editFormState)
    if (res.data.code === 0) {
      message.success('空间更新成功')
      showEditSpaceModal.value = false
      fetchData()
    } else {
      message.error('空间更新失败: ' + res.data.message)
    }
  } catch (e) {
    message.error('空间更新失败: ' + e)
  }
}

const handleDelete = (record: API.SpaceVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除空间 "${record.spaceName}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteSpaceUsingPost({ id: record.id })
        if (res.data.code === 0) {
          message.success('空间删除成功')
          fetchData()
        } else {
          message.error('空间删除失败: ' + res.data.message)
        }
      } catch (e) {
        message.error('空间删除失败: ' + e)
      }
    },
  })
}

const formatDateTime = (dateTime?: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#spaceManagePage {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ant-table-thead th {
  background-color: #f0f2f5;
}

.ant-table-tbody td {
  padding: 16px;
}
</style>

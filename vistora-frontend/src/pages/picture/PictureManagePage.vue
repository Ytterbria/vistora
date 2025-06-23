<template>
  <div id="pictureManagePage">
    <h2>图片管理</h2>

    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="handleSearch">
      <a-form-item label="图片名称" name="name">
        <a-input v-model:value="searchParams.name" placeholder="请输入图片名称" allow-clear />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-input v-model:value="searchParams.category" placeholder="请输入分类" allow-clear />
      </a-form-item>
      <a-form-item label="标签" name="tags" style="min-width: 120px">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          placeholder="请选择标签"
          :options="tagOptions"
          allow-clear
          dropdown-class-name="tag-select-dropdown"
        />
      </a-form-item>
      <a-form-item label="审核状态" name="reviewStatus" style="min-width: 150px">
        <a-select
          v-model:value="searchParams.reviewStatus"
          placeholder="请选择审核状态"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="resetSearch">重置</a-button>
      </a-form-item>
    </a-form>

    <!-- 批量操作按钮 -->
    <a-row style="margin-bottom: 16px">
      <a-col>
        <a-space>
          <a-button type="primary" :disabled="!selectedRowKeys.length" @click="openBatchEditModal">
            批量编辑
          </a-button>
          <a-button
            type="primary"
            :disabled="!selectedRowKeys.length"
            @click="openBatchReviewModal"
          >
            批量审核
          </a-button>
          <span v-if="selectedRowKeys.length">已选择 {{ selectedRowKeys.length }} 项</span>
        </a-space>
      </a-col>
    </a-row>

    <!-- 图片列表表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <!-- 表格列内容渲染 -->
        <template v-if="column.dataIndex === 'url'">
          <img :src="record.thumbnailUrl || record.url" style="max-width: 100px" alt="预览图" />
        </template>
        <template v-if="column.dataIndex === 'tags'">
          <!-- 优化标签展示，兼容字符串数组 -->
          <template v-if="Array.isArray(record.tags)">
            <a-tag v-for="tag in record.tags" :key="tag">{{ tag }}</a-tag>
          </template>
          <template v-else-if="typeof record.tags === 'string'">
            <a-tag v-for="tag in parseTags(record.tags)" :key="tag">{{ tag }}</a-tag>
          </template>
        </template>
        <template v-if="column.dataIndex === 'reviewStatus'">
          {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleReview(record)">审核</a-button>
            <a-popconfirm
              title="确定要删除该图片吗？"
              ok-text="删除"
              cancel-text="取消"
              @confirm="() => handleDelete(record)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 批量编辑弹窗 -->
    <a-modal
      v-model:visible="showBatchEditModal"
      title="批量编辑图片"
      @ok="handleBatchEditSubmit"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="batchEditForm" layout="vertical">
        <a-form-item label="分类">
          <a-auto-complete
            v-model:value="batchEditForm.category"
            :options="categoryOptions"
            placeholder="请输入分类"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="标签">
          <a-select
            v-model:value="batchEditForm.tags"
            mode="tags"
            :options="tagOptions"
            placeholder="请选择标签"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批量审核弹窗 -->
    <a-modal
      v-model:visible="showBatchReviewModal"
      title="批量审核图片"
      @ok="handleBatchReviewSubmit"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="batchReviewForm" layout="vertical">
        <a-form-item label="审核状态">
          <a-select
            v-model:value="batchReviewForm.reviewStatus"
            :options="PIC_REVIEW_STATUS_OPTIONS"
            placeholder="请选择审核状态"
          />
        </a-form-item>
        <a-form-item label="审核信息">
          <a-textarea
            v-model:value="batchReviewForm.reviewMessage"
            placeholder="请输入审核信息"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 单个审核弹窗 -->
    <a-modal
      v-model:visible="showReviewModal"
      title="图片审核"
      @ok="handleReviewSubmit"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :model="reviewForm" layout="vertical">
        <a-form-item label="审核状态">
          <a-select
            v-model:value="reviewForm.reviewStatus"
            :options="PIC_REVIEW_STATUS_OPTIONS"
            placeholder="请选择审核状态"
          />
        </a-form-item>
        <a-form-item label="审核信息">
          <a-textarea
            v-model:value="reviewForm.reviewMessage"
            placeholder="请输入审核信息"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
  editPictureUsingPost,
  listPictureTagCategoryUsingGet,
  editPictureByBatchUsingPost,
  deletePictureUsingPost,
} from '@/api/pictureController'
import { useRouter } from 'vue-router'
import { PIC_REVIEW_STATUS_MAP, PIC_REVIEW_STATUS_OPTIONS } from '@/constants/picture.ts'
import dayjs from 'dayjs'
// 表格列定义
const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '预览图', dataIndex: 'url' },
  { title: '名称', dataIndex: 'name', width: 120 }, // 调整宽度
  { title: '分类', dataIndex: 'category' },
  { title: '标签', dataIndex: 'tags' },
  { title: '审核状态', dataIndex: 'reviewStatus' },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action', fixed: 'right', width: 140 },
]

// 状态定义
const router = useRouter()
const dataList = ref<API.Picture[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])
const showReviewModal = ref(false)
const showEditModal = ref(false)
const categoryOptions = ref<{ value: string; label: string }[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])

// 搜索参数
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 选中的图片ID
const selectedPictureIds = ref<number[]>([])

// 审核表单
const reviewForm = reactive({
  id: undefined as number | undefined,
  reviewStatus: undefined as number | undefined,
  reviewMessage: '',
})

// 编辑表单
const editForm = reactive({
  id: undefined as number | undefined,
  name: '',
  category: '',
  tags: [] as string[],
  introduction: '',
})

// 新增：批量编辑表单
const batchEditForm = reactive({
  category: '',
  tags: [] as string[],
})
const showBatchEditModal = ref(false)

// 新增：批量审核表单
const batchReviewForm = reactive({
  reviewStatus: undefined as number | undefined,
  reviewMessage: '',
})
const showBatchReviewModal = ref(false)

// 分页配置
const pagination = computed(() => ({
  current: searchParams.current,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

// 获取标签分类数据
const getTagCategory = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      tagOptions.value = (res.data.data.tagList || []).map((tag) => ({
        value: tag,
        label: tag,
      }))
      categoryOptions.value = (res.data.data.categoryList || []).map((category) => ({
        value: category,
        label: category,
      }))
    }
  } catch (e) {
    message.error('获取标签分类失败')
  }
}

// 获取图片列表数据
const fetchData = async () => {
  try {
    const res = await listPictureByPageUsingPost(searchParams)
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    message.error('获取图片列表失败')
  }
}

// 表格选择改变
const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

// 打开批量编辑弹窗
const openBatchEditModal = () => {
  batchEditForm.category = ''
  batchEditForm.tags = []
  showBatchEditModal.value = true
}

// 批量编辑提交
const handleBatchEditSubmit = async () => {
  if (!selectedRowKeys.value.length) return
  try {
    const res = await editPictureByBatchUsingPost({
      pictureIdList: selectedRowKeys.value,
      category: batchEditForm.category || undefined,
      tags: batchEditForm.tags.length ? batchEditForm.tags : undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('批量编辑成功')
      showBatchEditModal.value = false
      fetchData()
    } else {
      message.error('批量编辑失败：' + res.data.message)
    }
  } catch (e) {
    message.error('批量编辑异常')
  }
}

// 打开批量审核弹窗
const openBatchReviewModal = () => {
  batchReviewForm.reviewStatus = undefined
  batchReviewForm.reviewMessage = ''
  showBatchReviewModal.value = true
}

// 批量审核提交
const handleBatchReviewSubmit = async () => {
  if (!selectedRowKeys.value.length || batchReviewForm.reviewStatus === undefined) return
  try {
    // 并发审核
    const promises = selectedRowKeys.value.map((id) =>
      doPictureReviewUsingPost({
        id,
        reviewStatus: batchReviewForm.reviewStatus,
        reviewMessage: batchReviewForm.reviewMessage,
      }),
    )
    await Promise.all(promises)
    message.success('批量审核成功')
    showBatchReviewModal.value = false
    fetchData()
  } catch (e) {
    message.error('批量审核失败')
  }
}

// 单个审核按钮方法
const handleReview = (record: API.Picture) => {
  reviewForm.id = record.id
  reviewForm.reviewStatus = record.reviewStatus
  reviewForm.reviewMessage = record.reviewMessage || ''
  showReviewModal.value = true
}

// 单个审核提交
const handleReviewSubmit = async () => {
  if (!reviewForm.id || reviewForm.reviewStatus === undefined) return
  try {
    const res = await doPictureReviewUsingPost({
      id: reviewForm.id,
      reviewStatus: reviewForm.reviewStatus,
      reviewMessage: reviewForm.reviewMessage,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('审核成功')
      showReviewModal.value = false
      fetchData()
    } else {
      message.error('审核失败：' + res.data.message)
    }
  } catch (e) {
    message.error('审核异常')
  }
}

// 单个删除
const handleDelete = async (record: API.Picture) => {
  try {
    const res = await deletePictureUsingPost({ id: record.id })
    if (res.data.code === 0 && res.data.data) {
      message.success('删除成功')
      fetchData()
    } else {
      throw new Error(res.data.message || '删除失败')
    }
  } catch (e: any) {
    message.error(`删除失败: ${e.message}`)
  }
}

// 其他处理方法
const handleSearch = () => {
  searchParams.current = 1
  fetchData()
}

const resetSearch = () => {
  Object.assign(searchParams, {
    name: undefined,
    category: undefined,
    tags: undefined,
    reviewStatus: undefined,
    current: 1,
  })
  fetchData()
}

const handleTableChange = (pagination: any) => {
  searchParams.current = pagination.current
  searchParams.pageSize = pagination.pageSize
  fetchData()
}

const handleBatchEdit = () => {
  batchEditForm.category = ''
  batchEditForm.tags = []
  showBatchEditModal.value = true
}

const formatDateTime = (dateTime?: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : ''
}

// 优化标签栏展示，兼容字符串和数组
function parseTags(tags: string): string[] {
  try {
    // 兼容后端返回的 '["标签1","标签2"]' 字符串
    if (tags.startsWith('[')) {
      return JSON.parse(tags)
    }
    // 兼容逗号分隔
    return tags
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean)
  } catch {
    return []
  }
}

// 页面加载初始化
onMounted(() => {
  getTagCategory()
  fetchData()
})
</script>

<style scoped>
#pictureManagePage {
  padding: 24px;
  background: #fff;
  border-radius: 2px;
}

.ant-table-thead th {
  background: #fafafa;
}

.ant-form {
  margin-bottom: 24px;
}

.tag-select-dropdown {
  max-height: 400px;
  overflow-y: auto;
}
</style>

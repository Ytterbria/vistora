<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-button type="primary" href="/add_picture" target="_blank">+ 创建图片</a-button>
    </a-flex>

    <a-form layout="inline" :model="searchParams" @finish="handleSearch">
      <a-form-item label="关键词" name="searchText">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="类型" name="category">
        <a-input v-model:value="searchParams.category" placeholder="请输入类型" allow-clear />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          placeholder="请输入标签"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="审核状态" name="reviewStatus">
        <a-select
          v-model:value="searchParams.reviewStatus"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          placeholder="请输入审核状态"
          style="min-width: 180px"
          allow-clear
        />
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
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'id'">
          {{ record.id }}
        </template>

        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="120" />
        </template>

        <template v-if="column.dataIndex === 'name'">
          {{ record.name }}
        </template>

        <template v-if="column.dataIndex === 'introduction'">
          {{ record.introduction }}
        </template>

        <template v-if="column.dataIndex === 'category'">
          {{ record.category }}
        </template>

        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">{{ tag }}</a-tag>
          </a-space>
        </template>

        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式:{{ record.picFormat }}</div>
          <div>宽度: {{ record.picWidth }}</div>
          <div>高度: {{ record.picHeight }}</div>
          <div>比例: {{ record.picScale }}</div>
          <div>大小: {{ (record.picSize / 1024 / 1024).toFixed(2) }} MB</div>
        </template>

        <template v-if="column.dataIndex === 'userId'">
          {{ record.userId }}
        </template>

        <template v-else-if="column.key === 'createTime'">
          <span>
            {{ formatDateTime(record.createTime) }}
          </span>
        </template>

        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
        </template>

        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              type="link"
              @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
            >
              通过
            </a-button>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              type="link"
              danger
              @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
            >
              拒绝
            </a-button>
            <a-button type="link" :href="`/add_picture?id=${record.id}`" target="_blank"
              >编辑
            </a-button>
            <a-button type="link" danger @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { PlusOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
} from '@/api/pictureController.ts'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '../../constants/picture.ts'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '上传用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)
const router = useRouter()
// 查询参数,搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 6,
})
// 分页
// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 6,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取图片数据失败' + res.data.message)
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

const handleDelete = async (record: API.Picture) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除图片 "${record.name}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deletePictureUsingPost({ id: record.id })
        if (res.data.code === 0) {
          message.success('图片删除成功')
          await fetchData()
        } else {
          message.error('图片删除失败: ' + res.data.message)
        }
      } catch (error) {
        message.error('图片删除失败: ' + error)
      }
    },
  })
}

const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage =
    reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    // 重新获取列表
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#pictureManagePage {
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

.avatar-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.ant-table-thead th {
  background-color: #f0f2f5;
}

.ant-table-tbody td {
  padding: 16px;
}
</style>

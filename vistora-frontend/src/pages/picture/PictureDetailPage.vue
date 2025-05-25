<template>
  <div id="picture-detail-page">
    <a-spin :spinning="loading" tip="加载中..." size="large">
      <template v-if="picture.id">
        <a-row :gutter="[16, 16]">
          <!-- 图片展示区 -->
          <a-col :xs="24" :md="16" :xl="18">
            <a-card>
              <a-image
                :src="picture.url"
                :alt="picture.name"
                :preview="false"
                style="object-fit: contain; max-height: 680px"
              />
            </a-card>
          </a-col>

          <!-- 图片信息区 -->
          <a-col :xs="24" :md="8" :xl="6">
            <a-card
              title="详细信息"
              class="info-card"
              :bordered="false"
              :head-style="{ border: 'none', padding: '0 0 16px' }"
            >
              <!-- 作者信息 -->
              <div class="author-section">
                <a-avatar
                  :size="48"
                  :src="picture.user?.userAvatar || '/default-avatar.png'"
                  class="author-avatar"
                />
                <div class="author-info">
                  <div class="author-name">
                    {{ picture.user?.userName || '未知用户' }}
                  </div>
                  <div class="author-account">@{{ picture.user?.userAccount || 'anonymous' }}</div>
                </div>
              </div>

              <!-- 分隔线 -->
              <a-divider class="styled-divider" />

              <!-- 基本信息 -->
              <div class="info-group">
                <h3 class="info-label">
                  <TagsOutlined class="icon" />
                  基本信息
                </h3>
                <div class="info-content">
                  <div class="info-item">
                    <span class="item-label">名称 </span>
                    <span class="item-value">{{ picture.name }}</span>
                  </div>
                  <div class="info-item">
                    <span class="item-label">分类 </span>
                    <a-tag color="geekblue" class="category-tag">
                      {{ picture.category || '默认分类' }}
                    </a-tag>
                  </div>
                  <div class="info-item">
                    <span class="item-label">简介 </span>
                    <p class="item-value intro-text">{{ picture.introduction || '暂无描述' }}</p>
                  </div>
                </div>
              </div>

              <!-- 技术参数 -->
              <div class="info-group">
                <h3 class="info-label">
                  <ToolOutlined class="icon" />
                  图片参数
                </h3>
                <div class="info-content">
                  <div class="info-item">
                    <span class="item-label">分辨率 </span>
                    <span class="item-value">{{ picture.picWidth }}×{{ picture.picHeight }}px</span>
                  </div>
                  <div class="info-item">
                    <span class="item-label">文件大小 </span>
                    <span class="item-value">{{ formatSize(picture.picSize) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="item-label">格式 </span>
                    <span class="item-value format-badge">
                      {{ (picture.picFormat || '-').toUpperCase() }}
                    </span>
                  </div>
                </div>
              </div>
            </a-card>

            <!-- 操作按钮 -->
            <div v-if="canEdit" class="action-buttons">
              <a-button type="primary" @click="handleEdit" class="edit-btn" shape="round">
                <template #icon><EditOutlined /></template>
                编辑图片
              </a-button>
              <a-button danger @click="handleDeleteConfirm" class="delete-btn" shape="round">
                <template #icon><DeleteOutlined /></template>
                删除图片
              </a-button>
              <a-button type="primary" shape="round" @click="handleDownload">
                免费下载
                <template #icon>
                  <DownloadOutlined />
                </template>
              </a-button>
            </div>
          </a-col>
        </a-row>
      </template>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import { getPictureVoByIdUsingGet, deletePictureUsingPost } from '@/api/pictureController.ts'
import { useLoginUserStore } from '@/stores/userLoginUserStore'
import {
  EditOutlined,
  DeleteOutlined,
  ToolOutlined,
  TagsOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { downloadImage } from '@/utils'

const route = useRoute()
const router = useRouter()
const userStore = useLoginUserStore()
const loading = ref(true)
const picture = ref<API.PictureVO>({})

// 格式化工具函数
const formatSize = (bytes?: number) => {
  if (!bytes || bytes === 0) return '0 Bytes'
  const units = ['Bytes', 'KB', 'MB', 'GB']
  const exp = Math.floor(Math.log(bytes) / Math.log(1024))
  return `${(bytes / Math.pow(1024, exp)).toFixed(2)} ${units[exp]}`
}

const formatTime = (timeStr?: string) => {
  return timeStr
    ? new Date(timeStr).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '-'
}

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const id = route.params.id
    if (!id) throw new Error('无效的图片ID')

    const res = await getPictureVoByIdUsingGet({
      id,
    })
    console.log(res.data)
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error(res.data.message || '获取详情失败')
      router.back()
    }
  } catch (e: any) {
    message.error(`加载失败: ${e.message || '未知错误'}`)
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 权限判断
const canEdit = computed(() => {
  return (
    userStore.loginUser?.userRole === 'admin' || picture.value.userId === userStore.loginUser?.id
  )
})

// 编辑操作
const handleEdit = () => {
  router.push(`/add_picture?id=${picture.value.id}`)
}

// 处理下载
const handleDownload = () => {
  downloadImage(picture.value.url)
}

// 删除确认
const handleDeleteConfirm = () => {
  Modal.confirm({
    title: '确认删除？',
    content: '此操作将永久删除该图片，是否继续？',
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    onOk: handleDelete,
  })
}

// 删除执行
const handleDelete = async () => {
  try {
    const res = await deletePictureUsingPost({ id: picture.value.id })
    if (res.data.code === 0 && res.data.data) {
      message.success('删除成功')
      router.push('/')
    } else {
      throw new Error(res.data.message || '删除失败')
    }
  } catch (e: any) {
    message.error(`删除失败: ${e.message}`)
  }
}

onMounted(() => {
  fetchPictureDetail()
})
</script>

<style scoped lang="scss">
/* 全局字体优化 */
body {
  font-family:
    'Inter',
    system-ui,
    -apple-system,
    sans-serif;
}

/* 优化antd组件圆角 */
.ant-card {
  border-radius: 12px !important;
}

/* 统一按钮阴影 */
.ant-btn {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.ant-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

#picture-detail-page {
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px 48px;
  background: #f8fafc;

  .info-card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
    padding: 24px;

    .author-section {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 12px;
      background: rgba(241, 245, 249, 0.5);
      border-radius: 12px;

      .author-avatar {
        border: 2px solid white;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .author-info {
        .author-name {
          font-weight: 600;
          color: #1e293b;
        }

        .author-account {
          font-size: 0.85em;
          color: #64748b;
        }
      }
    }

    .styled-divider {
      margin: 24px 0;
      border-color: rgba(0, 0, 0, 0.06);
    }

    .info-group {
      margin-bottom: 28px;

      .info-label {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #475569;
        font-size: 1.1em;
        margin-bottom: 16px;

        .icon {
          color: #94a3b8;
        }
      }

      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;
        padding: 8px 0;

        .item-label {
          color: #64748b;
          font-size: 0.9em;
          flex-shrink: 0;
          width: 80px;
        }

        .item-value {
          color: #1e293b;
          text-align: right;
          flex-grow: 1;

          &.intro-text {
            line-height: 1.6;
            font-size: 0.95em;
            color: #475569;
            white-space: pre-wrap;
          }
        }

        .category-tag {
          border-radius: 6px;
          font-weight: 500;
        }

        .format-badge {
          display: inline-block;
          padding: 2px 8px;
          background: #e2e8f0;
          border-radius: 4px;
          font-family: monospace;
        }
      }
    }
  }

  .action-buttons {
    margin-top: 32px;
    display: grid;
    gap: 16px;

    button {
      height: 42px;
      font-weight: 500;
      transition: all 0.2s ease;
    }
  }
}

@media (max-width: 768px) {
  #picture-detail-page {
    padding: 16px;

    .preview-card {
      margin-bottom: 24px;
    }

    .author-section {
      padding: 8px !important;
    }
  }
}
</style>

<script setup lang="ts">
import FilePictureUpload from '@/components/FilePictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import BatchPictureUpload from '@/components/BatchPictureUpload.vue'

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const pictureUploadByBatchRequest = ref<API.PictureUploadByBatchRequest>({})
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])
const router = useRouter()
const route = useRoute() //router用于跳转,route获取历史页面
const uploadType = ref<'file' | 'url'>('file')

const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const onBatchSuccess = (uploadCount: number) => {
  message.success(`批量上传成功，共上传${uploadCount}张图片`)
}

const getTagCategory = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    message.success('获取标签分类成功')
  } else {
    message.error('获取标签分类失败')
  }
}

const handleSubmit = async (values: API.PictureEditRequest) => {
  const pictureId = picture.value?.id
  console.log(pictureId)
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    //跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败')
  }
}

const getOldPicture = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getTagCategory()
  getOldPicture()
})
</script>

<template>
  <div id="add-picture-page">
    <h2 style="margin-bottom: 20px">{{ route.query?.id ? '编辑图片' : '创建图片' }}</h2>
    <!--上传图片组件-->
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <FilePictureUpload :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <UrlPictureUpload :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>

      <a-tab-pane key="batch" tab="批量上传">
        <BatchPictureUpload
          :onSuccess="onBatchSuccess"
          v-model:searchText="pictureUploadByBatchRequest.searchText"
          v-model:count="pictureUploadByBatchRequest.count"
        />
      </a-tab-pane>
    </a-tabs>
    <!--图片展示表单-->
    <a-form
      v-if="picture"
      name="pictureForm"
      layout="vertical"
      :model="pictureForm"
      @finish="handleSubmit"
    >
      <a-form-item name="name" label="名称">
        <a-input v-model:value="pictureForm.name" placeholder="输入图片名称" />
      </a-form-item>
      <a-form-item name="introduction" label="图片介绍">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :autoSize="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="tags" label="标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请选择相应标签"
          :options="tagOptions"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">创建图片</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
#add-picture-page {
  margin-left: 5%;
  margin-right: 5%;
}
</style>

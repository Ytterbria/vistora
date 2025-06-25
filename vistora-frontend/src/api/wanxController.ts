// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** createImageTask POST /api/wanx/create/task */
export async function createImageTaskUsingPost(
  body: API.CreateImageTaskRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseCreateImageTaskResponse_>('/api/wanx/create/task', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** queryImageTask GET /api/wanx/task/${param0} */
export async function queryImageTaskUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryImageTaskUsingGETParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.BaseResponseQueryImageTaskResponse_>(`/api/wanx/task/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

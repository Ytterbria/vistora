// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** testDownloadFile GET /api/file/download */
export async function testDownloadFileUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/download', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

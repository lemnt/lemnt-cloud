export interface LoginLog {
  infoId: number;
  userName: string;
  ipaddr: string;
  status: string;
  msg?: string;
  accessTime: string;
}
export interface OperationLog {
  operId: number;
  title: string;
  businessType: number;
  method?: string;
  requestMethod?: string;
  operatorType?: number;
  operName: string;
  deptName?: string;
  operUrl?: string;
  operIp?: string;
  operLocation?: string;
  operParam?: string;
  jsonResult?: string;
  status: number;
  errorMsg?: string;
  operTime: string;
  // 操作耗时，单位为毫秒。
  costTime?: number;
}
export interface LoginLogQuery {
  pageNum?: number;
  pageSize?: number;
  userName?: string;
  ipaddr?: string;
  status?: string;
  beginTime?: string;
  endTime?: string;
}
export interface OperationLogQuery {
  pageNum?: number;
  pageSize?: number;
  title?: string;
  businessType?: number;
  status?: number;
  operName?: string;
  beginTime?: string;
  endTime?: string;
}

/** 后端日期参数为顶层 LocalDateTime，不能沿用旧 Vue 的 params 嵌套对象。 */
export function logDateRange(range?: string[]): { beginTime?: string; endTime?: string } {
  return {
    beginTime: range?.[0] ? `${range[0]}T00:00:00` : undefined,
    endTime: range?.[1] ? `${range[1]}T23:59:59` : undefined,
  };
}

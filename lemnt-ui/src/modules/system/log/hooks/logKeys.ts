import type { LoginLogQuery, OperationLogQuery } from '../types';
export const logKeys = {
  login: ['loginLogs'] as const,
  operations: ['operationLogs'] as const,
  loginList: (params: LoginLogQuery) => [...logKeys.login, params] as const,
  operationList: (params: OperationLogQuery) => [...logKeys.operations, params] as const,
};

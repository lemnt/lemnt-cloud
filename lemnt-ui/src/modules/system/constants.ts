export enum SystemStatus {
  Normal = '1',
  Disabled = '2',
}

export const systemStatusOptions = [
  { label: '正常', value: SystemStatus.Normal },
  { label: '停用', value: SystemStatus.Disabled },
];

export const systemStatusEnum = {
  [SystemStatus.Normal]: { text: '正常', status: 'Success' },
  [SystemStatus.Disabled]: { text: '停用', status: 'Error' },
} as const;

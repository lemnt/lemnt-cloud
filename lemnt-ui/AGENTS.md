# AI Code Generation Guidelines

本文件用于约束 AI 在本项目中生成、修改、重构代码时的行为。

AI 在执行任何代码任务前，必须遵守本文件。

---

# 1. 项目技术栈

本项目为 lemnt-cloud 通用管理后台。

核心技术栈：

- React
- TypeScript
- Vite
- Ant Design
- Ant Design Pro Components
- React Router
- TanStack Query
- Zustand
- Axios
- yarn

生成代码时必须优先使用现有技术栈。

禁止未经明确要求引入新的：

- UI 框架
- 状态管理框架
- HTTP 请求库
- 路由框架
- CSS 框架
- 表单框架

例如禁止自行引入：

- Material UI
- Chakra UI
- Tailwind CSS
- Redux
- MobX
- React Hook Form
- SWR

---

# 2. 项目目录结构

项目采用业务模块化结构。

```text
src/
├── api/
│   ├── request.ts
│   ├── interceptor.ts
│   └── types.ts
│
├── modules/
│   ├── auth/
│   ├── home/
│   └── system/
│
├── shared/
│   ├── components/
│   ├── hooks/
│   ├── utils/
│   ├── types/
│   └── constants/
│
├── layouts/
├── router/
├── stores/
├── App.tsx
├── main.tsx
└── vite-env.d.ts
```

业务代码必须优先放入对应的 `modules`。

例如：

```text
modules/system/user/
├── api/
│   └── userApi.ts
├── components/
├── hooks/
├── pages/
├── types.ts
└── constants.ts
```

禁止为了方便将业务代码随意放入：

```text
src/components
src/utils
src/hooks
```

只有真正跨业务模块复用的代码才能进入 `shared`。

---

# 3. 核心业务领域

当前保留认证、首页及系统管理（用户、角色、菜单、部门、岗位、字典、参数、日志、公告、个人中心）。

DSP 广告业务已移除，不得新增对旧广告接口或身份字段的依赖。

---

# 4. 基础代码格式

统一使用：

```text
缩进          2 spaces
字符串        Single Quote
分号          必须
最大行宽      100
尾逗号        必须
换行符        LF
编码          UTF-8
```

Prettier 配置：

```js
export default {
  printWidth: 100,
  tabWidth: 2,
  useTabs: false,
  semi: true,
  singleQuote: true,
  trailingComma: 'all',
  bracketSpacing: true,
  bracketSameLine: false,
  arrowParens: 'always',
  endOfLine: 'lf',
};
```

AI 生成的代码必须符合 Prettier 格式。

不要通过人为排版与 Prettier 对抗。

---

# 5. TypeScript 规则

必须启用严格类型。

禁止：

```ts
any
```

除非：

1. 第三方库没有类型定义；
2. 无法合理推断类型；
3. 用户明确要求。

未知类型优先使用：

```ts
unknown
```

禁止：

```ts
const data: any = response;
```

推荐：

```ts
const data: User = response;
```

所有：

- API 参数
- API 响应
- Component Props
- Hook 参数
- Hook 返回值
- Form Values
- Store

必须具有明确类型。

---

# 6. 类型命名

类型统一使用 PascalCase。

例如：

```ts
interface User {
  id: number;
  name: string;
}

interface UserQuery {
  name?: string;
  status?: SystemStatus;
}

interface CreateUserRequest {
  name: string;
}

interface UpdateUserRequest {
  name?: string;
}
```

禁止使用：

```text
IUser
TUser
UserInterface
UserType
```

直接：

```text
User
SystemStatus
UserQuery
```

---

# 7. interface 与 type

对象结构优先使用：

```ts
interface
```

例如：

```ts
interface User {
  id: number;
  name: string;
}
```

联合类型使用：

```ts
type
```

例如：

```ts
type SystemStatus = '1' | '2';
```

不要无意义地混用。

---

# 8. 文件命名

React Component：

```text
PascalCase.tsx
```

例如：

```text
UserForm.tsx
UserTable.tsx
NoticePreview.tsx
UserPicker.tsx
```

普通 TypeScript：

```text
camelCase.ts
```

例如：

```text
userApi.ts
userUtils.ts
userStore.ts
```

Hook：

```text
useXxx.ts
```

例如：

```text
useUser.ts
useUserList.ts
```

禁止混用：

```text
user.api.ts
user-api.ts
user_api.ts
UserAPI.ts
```

---

# 9. 禁止模糊文件名

禁止创建：

```text
Form.tsx
List.tsx
Table.tsx
Detail.tsx
Modal.tsx
Utils.ts
Common.ts
Helper.ts
```

必须体现业务含义：

```text
UserForm.tsx
UserList.tsx
UserDetail.tsx
UserTable.tsx

RoleForm.tsx

NoticePreview.tsx

UserPicker.tsx
```

保证通过文件名即可理解用途。

---

# 10. 变量命名

普通变量使用：

```text
camelCase
```

例如：

```ts
const userId = 1;
const userName = '测试用户';
const attachmentList = [];
```

Boolean 必须尽量体现语义：

```text
isXxx
hasXxx
canXxx
shouldXxx
```

推荐：

```ts
const isLoading = false;
const isEnabled = true;
const hasPermission = true;
const canEdit = true;
const shouldRefresh = false;
```

避免：

```ts
const loading = false;
const permission = true;
const edit = true;
```

---

# 11. 常量命名

真正的全局/模块常量使用：

```text
UPPER_SNAKE_CASE
```

例如：

```ts
const DEFAULT_PAGE_SIZE = 20;
const MAX_UPLOAD_SIZE = 10;
```

配置对象可以使用 camelCase：

```ts
const systemStatusOptions = [];
```

不要为了形式将所有 `const` 都写成大写。

---

# 12. React Component

组件使用函数组件。

推荐：

```tsx
interface UserFormProps {
  userId?: number;
  readonly?: boolean;
  onSuccess?: () => void;
}

export function UserForm({
  userId,
  readonly = false,
  onSuccess,
}: UserFormProps) {
  return <div />;
}
```

不推荐：

```tsx
const UserForm: React.FC<UserFormProps> = () => {
  return <div />;
};
```

禁止：

```tsx
const UserForm: React.FC<any> = () => {};
```

除非现有项目明确统一使用 `React.FC`，否则 AI 不得主动引入。

---

# 13. Props 规范

Props 类型命名：

```text
<ComponentName>Props
```

例如：

```ts
interface UserPickerProps {
  multiple?: boolean;
  value?: Attachment[];
  onChange?: (value: Attachment[]) => void;
}
```

组件内部事件：

```text
handleXxx
```

组件对外事件：

```text
onXxx
```

例如：

```tsx
function UserPicker({ onChange }: UserPickerProps) {
  const handleSelect = (attachment: Attachment) => {
    onChange?.([attachment]);
  };

  // ...
}
```

---

# 14. React Hooks

自定义 Hook 必须以：

```text
use
```

开头。

例如：

```text
useUser
useUserList
useUserList
usePermission
```

Hook 中禁止违反 React Hooks Rules。

禁止条件调用：

```ts
if (id) {
  useUser(id);
}
```

---

# 15. Server State

服务端数据统一优先使用 TanStack Query。

包括：

- User List
- User Detail
- Role
- Notice
- Attachment
- Dept
- Log Data

例如：

```ts
export function useUser(id: number) {
  return useQuery({
    queryKey: userKeys.detail(id),
    queryFn: () => getUserDetail(id),
    enabled: Boolean(id),
  });
}
```

禁止为了保存接口数据直接放进 Zustand。

---

# 16. Zustand 使用范围

Zustand 仅用于真正的客户端全局状态，例如：

```text
当前用户
权限
主题
全局 UI 状态
跨页面临时状态
```

禁止：

```text
User List → Zustand
Attachment List → Zustand
Notice List → Zustand
```

这些数据属于 Server State，应使用 TanStack Query。

---

# 17. Query Key

Query Key 必须集中管理。

例如：

```ts
export const userKeys = {
  all: ['user'] as const,

  lists: () => [...userKeys.all, 'list'] as const,

  list: (params: UserQuery) => [...userKeys.lists(), params] as const,

  details: () => [...userKeys.all, 'detail'] as const,

  detail: (id: number) => [...userKeys.details(), id] as const,
};
```

禁止在不同页面随意：

```ts
queryKey: ['user-list']
```

然后其他地方：

```ts
queryKey: ['userList']
```

---

# 18. API 规范

API 文件：

```text
userApi.ts
roleApi.ts
noticeApi.ts
attachmentApi.ts
deptApi.ts
```

API 函数统一：

```text
getXxxList
getXxxDetail
createXxx
updateXxx
deleteXxx
enableXxx
disableXxx
```

例如：

```ts
export function getUserList(params: UserQuery) {}

export function getUserDetail(id: number) {}

export function createUser(data: CreateUserRequest) {}

export function updateUser(id: number, data: UpdateUserRequest) {}

export function deleteUser(id: number) {}
```

禁止同一种操作混用：

```text
fetchUser
loadUser
queryUser
requestUser
```

---

# 19. API 层职责

API 层只负责：

```text
HTTP 请求
参数
响应类型
```

禁止 API 层：

- 操作 React State
- 操作 Modal
- 调用 message.success
- 跳转路由
- 操作 DOM

错误：

```ts
export async function deleteUser(id: number) {
  await request.delete(`/users/${id}`);

  message.success('删除成功');
}
```

正确：

```ts
export function deleteUser(id: number) {
  return request.delete(`/users/${id}`);
}
```

UI 提示由调用层处理。

---

# 20. DTO 规范

禁止一个类型承担所有场景。

禁止：

```ts
UserDTO
```

同时用于：

```text
新增
编辑
列表
详情
查询
```

应该根据语义拆分：

```text
User
UserQuery
CreateUserRequest
UpdateUserRequest
UserListResponse
```

字段可以重复。

不要为了消除几个重复字段而制造复杂类型继承。

---

# 21. Ant Design

UI 优先使用 Ant Design。

禁止已有 Ant Design Component 可以完成需求时重新造组件。

例如优先：

```text
Button
Form
Input
Select
Modal
Drawer
Tabs
Tag
Tooltip
Upload
DatePicker
```

---

# 22. Pro Components

后台 CRUD 页面优先考虑：

```text
PageContainer
ProTable
ProForm
ProDescriptions
ProCard
```

例如列表页优先：

```tsx
<PageContainer>
  <ProTable<User>
    rowKey="id"
    columns={columns}
    request={request}
  />
</PageContainer>
```

不要为了简单 CRUD 页面手写大量：

```text
Form + Table + Pagination
```

---

# 23. ProTable Columns

Columns 必须具有明确类型：

```ts
const columns: ProColumns<User>[] = [];
```

禁止：

```ts
const columns: any[] = [];
```

复杂 Column 配置应从 JSX 主体中分离。

页面主体应尽可能保持清晰。

---

# 24. 表单

后台业务表单优先使用：

```text
ProForm
ProFormText
ProFormSelect
ProFormDigit
ProFormDateTimePicker
```

复杂业务组件允许配合：

```text
Form.Item
```

表单数据必须定义：

```ts
interface UserFormValues {}
```

禁止：

```ts
const handleSubmit = (values: any) => {};
```

---

# 25. import 顺序

统一顺序：

```text
1. React
2. 第三方库
3. 项目业务模块
4. shared
5. type
6. style
```

例如：

```tsx
import { useMemo, useState } from 'react';

import { Button, Space } from 'antd';
import { ProTable } from '@ant-design/pro-components';

import { getUserList } from '@/modules/system/user/api/userApi';

import { StatusTag } from '@/shared/components/StatusTag';

import type { User } from '@/modules/system/user/types';

import styles from './index.module.less';
```

TypeScript 类型必须尽量使用：

```ts
import type
```

---

# 26. 路径规则

跨模块引用统一使用：

```text
@/
```

例如：

```ts
import { UserPicker } from '@/modules/system/user/components/UserPicker';
```

同模块内部允许相对路径：

```ts
import { SystemStatus } from '../constants';
```

禁止跨越多层：

```ts
../../../../shared/components
```

原则：

```text
跨模块 → @/
模块内部 → ./
模块内部 → ../
```

---

# 27. 模块边界

业务模块之间禁止形成循环依赖。

例如禁止：

```text
user
   ↓
role
   ↓
notice
   ↓
user
```

公共能力必须下沉至：

```text
shared
```

AI 修改代码前必须判断：

“这个能力属于业务模块，还是跨业务公共能力？”

不要因为两个地方使用就立即抽到 shared。

只有具有稳定、明确的跨业务语义时才抽取。

---

# 28. 组件拆分原则

禁止过度拆分。

以下内容通常不需要单独组件：

- 只有几行 JSX
- 只使用一次
- 没有独立业务含义
- 拆分后反而增加理解成本

应该拆分：

- 独立业务能力
- 多处复用
- 复杂 UI
- 复杂交互
- 页面主体明显过长

例如：

```text
NoticePreview
UserPicker
RolePermissionForm
DeptSelector
```

属于合理业务组件。

---

# 29. 函数原则

函数应保持单一职责。

推荐：

```ts
const handleDelete = async (id: number) => {
  await deleteUser(id);
  message.success('删除成功');
  await refetch();
};
```

复杂逻辑应提取。

但禁止为了所谓“Clean Code”把简单逻辑拆成大量一两行函数。

优先保证：

```text
可读性 > 抽象程度
```

---

# 30. 注释

不要给显而易见的代码写注释。

禁止：

```ts
// 获取计划列表
const userList = getUserList();
```

注释应该解释：

```text
为什么这么做
特殊业务规则
兼容原因
异常情况
复杂算法
```

而不是解释代码字面含义。

---

# 31. 禁止魔法值

错误：

```ts
if (user.status === 3) {}
```

正确：

```ts
if (user.status === SystemStatus.Normal) {}
```

错误：

```ts
if (file.size > 10485760) {}
```

正确：

```ts
const MAX_UPLOAD_SIZE = 10 * 1024 * 1024;

if (file.size > MAX_UPLOAD_SIZE) {}
```

---

# 32. 状态枚举

业务状态必须集中定义。

例如：

```ts
export enum SystemStatus {
  Normal = '1',
  Disabled = '2',
}
```

展示配置集中定义：

```ts
export const systemStatusOptions = [
  {
    label: '停用',
    value: SystemStatus.Disabled,
  },
  {
    label: '正常',
    value: SystemStatus.Normal,
  },
];
```

禁止页面中重复定义状态映射。

---

# 33. 时间处理

后端时间字段保持统一。

推荐：

```text
createdAt
updatedAt
startTime
endTime
```

不要混用：

```text
createTime
createdTime
created_at
createdAt
```

前端领域模型统一一种格式。

如果后端字段无法修改，应在 API/Adapter 层处理。

---

# 34. ID 类型

同一业务实体 ID 类型必须统一。

例如确定：

```ts
id: number;
userId: number;
noticeId: number;
```

后不得出现：

```ts
userId: string | number;
```

除非接口确实存在这种情况。

禁止为了“兼容”随意扩大类型范围。

---

# 35. 空值

明确区分：

```text
undefined
null
''
0
[]
```

不要通过：

```ts
value || defaultValue
```

处理所有默认值。

如果 `0` 是合法值，应使用：

```ts
value ?? defaultValue
```

---

# 36. 异步代码

优先：

```ts
async / await
```

避免不必要的 Promise 链：

```ts
getUser()
  .then(...)
  .then(...)
  .catch(...);
```

推荐：

```ts
try {
  const user = await getUserDetail(id);
} catch (error) {
  // ...
}
```

---

# 37. Error Handling

禁止：

```ts
catch (error) {
  console.log(error);
}
```

然后吞掉异常。

HTTP 通用错误由 Axios interceptor 统一处理。

业务特殊错误由业务层处理。

避免同一个错误同时出现：

```text
Interceptor message
+
Component message
```

导致重复提示。

---

# 38. React 性能优化

禁止无意义使用：

```text
useMemo
useCallback
memo
```

只有存在明确性能原因时使用。

不要生成：

```ts
const handleClick = useCallback(() => {
  setOpen(true);
}, []);
```

仅仅为了“最佳实践”。

优先代码可读性。

---

# 39. useEffect

减少使用 `useEffect` 处理可以直接计算的数据。

禁止：

```tsx
const [fullName, setFullName] = useState('');

useEffect(() => {
  setFullName(`${firstName}${lastName}`);
}, [firstName, lastName]);
```

应该：

```ts
const fullName = `${firstName}${lastName}`;
```

Server State 不通过 `useEffect + useState` 手动同步。

优先 TanStack Query。

---

# 40. CSS

优先：

1. Ant Design Component Props
2. Ant Design Token
3. CSS Modules

禁止大量 inline style：

```tsx
<div
  style={{
    marginTop: 20,
    padding: 20,
    display: 'flex',
  }}
/>
```

复杂样式放入：

```text
xxx.module.less
```

或项目当前采用的 CSS Module 方案。

---

# 41. 权限

权限判断必须通过统一权限能力。

例如：

```ts
const { can } = usePermission();

const canEdit = can('user:update');
```

禁止页面自行：

```ts
if (user.role === 'admin') {}
```

业务代码不应该直接绑定角色名称。

应该判断：

```text
Permission
```

而不是：

```text
Role
```

---

# 42. 删除操作

删除、批量删除等危险操作必须具有确认步骤。

优先：

```text
Popconfirm
Modal.confirm
```

例如：

```tsx
<Popconfirm
  title="确认删除该用户？"
  onConfirm={() => handleDelete(record.id)}
>
  <Button type="link" danger>
    删除
  </Button>
</Popconfirm>
```

---

# 43. Loading

所有异步操作必须考虑 Loading。

例如：

```text
页面加载
表格加载
提交按钮
删除操作
上传文件
```

禁止用户点击提交后 UI 没有任何反馈。

同时必须避免重复提交。

---

# 48. 修改已有代码

AI 修改代码时：

1. 先阅读相关文件；
2. 理解现有实现；
3. 优先遵循已有架构；
4. 最小范围修改；
5. 不修改无关代码；
6. 不进行无关重构；
7. 不随意改变公共 API；
8. 不随意修改已有类型；
9. 不随意移动文件。

禁止为了实现一个简单功能重构整个模块。

---

# 49. 创建新文件

创建新文件前必须判断：

1. 是否已有相同能力；
2. 是否可以扩展已有组件；
3. 属于哪个业务模块；
4. 是否真的属于 shared；
5. 是否会形成循环依赖。

禁止随意创建：

```text
utils2.ts
helper.ts
common.ts
temp.ts
newUtils.ts
```

---

# 50. 依赖管理

未经明确要求禁止新增 npm dependency。

如果现有依赖可以实现需求，必须优先使用现有依赖。

需要新增 dependency 时，应说明：

```text
依赖名称
用途
为什么现有依赖无法满足
```

禁止 AI 为一个简单函数安装第三方库。

---

# 51. 生成代码前

执行任务前必须先：

1. 阅读本文件；
2. 阅读目标模块目录；
3. 阅读相关类型；
4. 阅读相关 API；
5. 阅读现有相似实现；
6. 确认项目已有依赖。

不要在不了解项目现状时直接生成一套新的架构。

---

# 52. 生成代码后

完成代码后必须检查：

```bash
yarn typecheck
yarn lint
yarn format:check
```

如果项目存在测试：

```bash
yarn test
```

如果修改影响构建：

```bash
yarn build
```

AI 不得声称检查通过，除非实际执行成功。

---

# 53. 禁止行为

AI MUST NOT：

- 使用 `any` 逃避 TypeScript 错误
- 使用 `@ts-ignore` 逃避错误
- 使用 `eslint-disable` 逃避规则
- 删除测试让构建通过
- 修改无关代码
- 随意增加依赖
- 随意创建公共组件
- 随意修改 API 数据结构
- 猜测后端字段
- 猜测业务规则
- 重复实现已有能力
- 制造循环依赖
- 将 Server State 放入 Zustand
- 大量使用 useEffect 同步状态
- 过度使用 useMemo/useCallback
- 为简单逻辑过度抽象
- 在 API 层调用 UI 组件
- 在页面中重复定义业务状态
- 创建无业务含义的文件名

---

# 54. 不确定时的处理原则

如果需求存在业务歧义，例如：

```text
字段单位不明确
状态值不明确
用户和角色关系不明确
文件尺寸规则不明确
API 字段不明确
权限编码不明确
```

不得自行猜测。

优先：

1. 检查项目现有代码；
2. 检查现有类型；
3. 检查 API；
4. 检查相似业务；
5. 仍无法确定时询问用户。

---

# 55. 核心原则

生成代码始终遵循：

```text
Correctness
    ↓
Type Safety
    ↓
Business Clarity
    ↓
Readability
    ↓
Maintainability
    ↓
Reuse
    ↓
Abstraction
```

不要为了：

```text
更炫
更抽象
更通用
代码更少
所谓最佳实践
```

牺牲业务可读性。

本项目是 lemnt-cloud 通用管理后台。

代码首先应该让开发人员能够快速理解：

```text
这是哪个业务？
这个数据是什么？
这个操作做什么？
这个状态代表什么？
这个组件属于哪里？
```

在满足这些条件之后，再考虑抽象和复用。
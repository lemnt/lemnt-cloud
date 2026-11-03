# lemnt-ui

lemnt-cloud 管理前端，使用 React、TypeScript、Vite、Ant Design / Pro Components、TanStack Query、Zustand 和 Axios。

## 保留功能

- 登录、验证码、锁屏、退出和用户会话。
- 基于后端授权菜单的导航、页面缓存及首页快捷入口。
- 用户、角色、菜单、部门、岗位、字典、参数、日志、公告和个人中心。
- 通知、文件上传和头像裁剪等系统管理功能。
- 会员资料、等级、标签、分组、地址与权益记录。
- 商品目录、规格、评价、优惠券、活动与结算维护。
- 订单履约、售后退款、支付配置、钱包、分销及自提维护。

当前接口与统一规范见[代码规范](../docs/code-standards.md)，验证结果见[第165批交付](../work-items/member-domain-structure/165-project-code-standards-delivery.md)。代码提供的页面不等于已完成真实环境和权限验收。

## 环境与启动

Node.js >=22.13.0 且 <23，包管理器使用 Yarn 1.22.22。依赖版本以 `package.json` 和 `yarn.lock` 为准。

```bash
yarn install --frozen-lockfile
yarn dev
```

开发地址为 `http://localhost:5173`，`/dev-api` 由 Vite 转发至 `http://localhost:8080` 并移除前缀。启动前需准备后端认证、系统管理和网关服务。

生产构建使用 `yarn build`，产物为 `dist/`。部署层需要将 `/prod-api` 转发到网关并移除该前缀，同时为前端路由配置 `index.html` 回退。

## 检查命令

```bash
yarn typecheck
yarn lint
yarn format:check
yarn test
yarn build
```

这些检查不代替真实后端联调。开发规范见 [AGENTS.md](AGENTS.md)，后端配置与启动步骤见 [项目 README](../README.md)。
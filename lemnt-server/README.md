# lemnt-cloud

## 一、项目简介

lemnt-cloud 是一个基于 **Spring Cloud 微服务架构** 的通用开发框架，包含认证、权限与系统管理模块，支持高并发访问、统一鉴权、网关路由与服务治理。
项目采用 **Gateway + 多业务微服务** 架构， **同时支持 Servlet（MVC）与 Reactive（WebFlux）两种 Web 技术模型**，RPC 通信使用
**Apache Dubbo**，服务注册与配置中心使用 **Nacos**。

---

## 二、模块结构

```
lemnt-server
├── lemnt-gateway         网关模块 [8080]
├── lemnt-auth            认证中心 [9200]
│   ├── lemnt-auth-adapter          Web Controller 层
│   ├── lemnt-auth-application      应用层
│   ├── lemnt-auth-domain           领域层
│   ├── lemnt-auth-infrastructure   基础设施层
│   └── lemnt-auth-start            启动引导
├── lemnt-system          系统管理 [9100]
│   ├── lemnt-system-client        Dubbo 接口定义
│   ├── lemnt-system-adapter       Web Controller 层
│   ├── lemnt-system-application   应用层（CQRS）
│   ├── lemnt-system-domain        领域层
│   ├── lemnt-system-infrastructure 基础设施层
│   └── lemnt-system-start         启动引导
├── lemnt-open            开放平台 [9300]
│   ├── lemnt-open-adapter         Web Controller 层
│   ├── lemnt-open-application     应用层
│   ├── lemnt-open-domain          领域层
│   ├── lemnt-open-infrastructure  基础设施层
│   └── lemnt-open-start           启动引导
└── lemnt-third-sdk       第三方 SDK
    └── demo-sdk           HTTP 调用示例
```

### 模块说明

| 模块         | 端口 | 说明                                    |
|--------------|------|-----------------------------------------|
| lemnt-gateway   | 8080 | 网关层，统一鉴权、限流、路由转发        |
| lemnt-auth      | 9200 | 认证中心，登录/注册/Token 管理          |
| lemnt-system    | 9100 | 系统管理，用户/角色/菜单/字典/配置/审计 |
| lemnt-open      | 9300 | 开放平台，对外 API                      |
| lemnt-third-sdk | —    | 第三方 SDK 调用示例    |

---

## 三、技术栈

| 层面       | 技术                           |
|------------|--------------------------------|
| 基础框架   | Spring Boot 4.1.0, Spring Cloud 2025.1.2 |
| 云组件     | Spring Cloud Alibaba 2025.1.0.0          |
| 网关       | Spring Cloud Gateway (WebFlux) |
| RPC        | Apache Dubbo                   |
| 注册配置   | Nacos Server 3.2.1             |
| ORM        | MyBatis-Plus                   |
| 数据库     | MySQL                          |
| 缓存       | Redis                          |
| 消息队列   | RabbitMQ / RocketMQ            |
| 流控       | Sentinel                       |
| 任务调度   | XXL-Job                        |

## 开发约定

保留微服务与 CQRS/DDD 分层，业务模块使用 client、adapter、application、domain、infrastructure、start 分层。
认证仅依赖系统用户服务；用户初始化信息包含用户、角色和权限。

---

## 四、核心业务能力

| 模块           | 功能                                  |
|----------------|---------------------------------------|
| **认证中心**   | 登录/登出、注册、Token 刷新与校验     |
| **用户管理**   | 系统用户、角色、权限分配              |
| **菜单管理**   | 菜单树、路由配置                      |
| **组织架构**   | 部门管理、岗位管理                    |
| **字典管理**   | 字典类型、字典数据                    |
| **参数配置**   | 系统参数动态配置                      |
| **操作审计**   | 操作日志、登录日志                    |
| **通知公告**   | 通知发布与管理                        |
| **第三方对接** | 通用 HTTP 调用示例 |
| **开放平台**   | 对外 API                              |

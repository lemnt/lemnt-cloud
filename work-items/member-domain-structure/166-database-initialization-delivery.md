# 第166批交付：业务服务SQL单文件整合

2026-11-03。按用户要求整合数据库文件，并遵守随后补充的“system项目不调整”。只处理member、mall、trade；system及公共目录中的系统SQL逐文件与本批起点比较保持一致。

## 最终文件

| 服务 | 唯一初始化SQL | 表数 | 字段数 |
|---|---|---:|---:|
| member | [lemnt_member.sql](../../lemnt-server/lemnt-member/sql/lemnt_member.sql) | 17 | 149 |
| mall | [lemnt_mall.sql](../../lemnt-server/lemnt-mall/sql/lemnt_mall.sql) | 35 | 379 |
| trade | [lemnt_trade.sql](../../lemnt-server/lemnt-trade/sql/lemnt_trade.sql) | 29 | 403 |

原三个服务90份SQL收敛为3份，额外清理6份Finder SQL元数据及第164批旧结构草案。原增量列、索引和约束已折叠进最终CREATE TABLE，不是按旧脚本拼接。只允许CREATE TABLE及必要INSERT，当前这三个服务没有必须预置的业务记录，因此没有加入会员、商品、订单、积分或日志演示数据。菜单、字典、默认参数和原system基础数据不变。

## 验证

- 在两个独立H2 MySQL模式数据库分别执行原V脚本链和最终文件，对81张表逐项比较：列类型/长度/精度、NULL/default/自增/注释、主键、索引列顺序与唯一性、外键及检查表达式一致。H2自动生成约束名不作为比较依据。
- 三个完整文件各执行两次成功；IF NOT EXISTS不会改旧表或补写历史数据，不能据此当作旧库升级器。
- 全项目98表/1100字段注释清单与合并前一致，缺失0；system SQL逐文件校验和不变。
- trade311、mall197、member122，共630项后端测试通过，三服务package通过；SQL工具9项通过。首次clean构建发现赠券测试仍假设存在旧升级索引，已改为验证最终组合唯一键与幂等重放，未删除测试或放宽重复赠券断言。
- 历史升级脚本专项测试调整为最终结构下的历史NULL、编号、审计、旧规格JSON、外键与请求重放兼容验证，不再声称验证已删除的升级实现。
- 测试资源和正式文件引用同步；现行规范及数据库入口更新，历史文档的旧执行步骤标明仅作追溯，链接转向最终结构。

机器证据见[验证记录](evidence/database-initialization-verification.json)，完整范围见[文件清单](evidence/database-initialization-files.json)，使用说明见[数据库初始化](../../docs/database-initialization.md)。

## 边界与工作区

本轮没有连接或修改运行中的数据库，也未在目标MySQL验证DDL。三个新文件不含DROP、ALTER、UPDATE、DELETE、USE、跨库复制或历史回填；已有库另行核对实际结构和制定变更。本批不改变system项目；工作区并行的监控模块及依赖调整保留，不纳入本批。

助手没有执行git commit或push。期间工作区出现外部的“调整SQL”提交，保留现有提交，不作回退；文件清单以本批起点01b711aa为比较基准，避免遗漏期间已提交的本批改动。

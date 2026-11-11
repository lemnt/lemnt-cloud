# 收货地址地区名称交付

2026-10-31，承接125；参考本地芋道AddressConvert.convertAreaIdToAreaName和AreaUtils.format。

system复用既有源CSV及area域，启动构建不可变全称索引，新增RemoteAreaService批量只读接口，省市区空格连接，国内省以下不加中国、国外保留国家，未知编号不编造。member通过address域Gateway读取，列表去重批量一次，空列表/缺失地址无RPC；可选provider引用check=false，RPC异常仅降级名称为未知。

AddressDTO附加可选areaName并保留原七字段构造，App列表/详情/默认三个独立Res及后台地址Res平铺返显。写请求/写响应、地址RPC、订单快照保持；名称不落库不回写。React地址表、小程序地址簿及结算选址显示全称，缺失/null保留编号。新名称provider未部署时仍可显示原地址编号。

新增system1项实际资源批量格式测试，member4项涵盖去重一次、未知大ID、空/缺失不查询、详情/默认同投影、RPC异常降级、App三个响应平铺。React新增1项解码兼容/非法名称/大ID；小程序新增1项解码及保存白名单，名称不回写。H2真实地址HTTP用例仍保留并已适配新查询构造。

首次联合构建在system既有MinIO本地HttpServer测试因沙箱禁止绑定端口失败；允许本机端口后完整联合verify通过，最后可选引用注解调整后的member verify通过。member117/auth20/mall119/trade158共414业务测试，system136、gateway34、common-security29均失败/错误/跳过0；网关禁用Dubbo依赖构建规则继续通过。React223、35文件，小程序124通过，前端build/typecheck/lint/format:check通过，构建仅既有大chunk提示。git diff --check通过。

日志/tmp/member-address-area-*.log，证据evidence/member-address-area-name-verification.json，接口docs/member-address-area-name-api.md。无DDL、commit/push；本机测试HttpServer为现有S3替身，没有向真实OSS写入。未真实跨进程Dubbo/Nacos/Redis/部署HTTP/浏览器/微信验收；完整源契约仍in_progress。

# 收货地址地区名称设计

2026-10-31，承接124，参考芋道AddressConvert.convertAreaIdToAreaName及AreaUtils.format：地区全称以空格连接省市区，国内不显示中国，未知编号null。

复用system唯一源CSV及已加载地区树，新增area域批量名称读取契约RemoteAreaService.getNames(List<Long>)，系统实现仅格式化已有树，不复制CSV或新增表。member通过address域MemberAddressAreaGateway调用，列表去重一次批量请求；空列表/缺失地址不请求。仅展示名称，RPC不可用时保留编号并返回未知名称，不影响地址本身；不猜测名称。

应用AddressDTO附加可选areaName，保留7字段构造用于写结果及既有调用；App列表/详情/默认三个独立Res及后台地址Res追加areaName，写Req和create/update Res、订单地址RPC保持。名称不落库、不回写，不改历史订单快照。

后台地址表和小程序地址簿/结算选择器显示全称，旧响应/未知名称回退编号。验证实际system资源路径/批量格式、member批量和空数据/缺失、独立响应与请求白名单、前端/小程序解码及显示。system/member/auth/gateway/mall/trade联合verify、前端完整检查、小程序回归；无DDL/提交/真实RPC部署。

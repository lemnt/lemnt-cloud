# 评价图片上传设计

2026-10-29，复用system FileUploadCmdExe/FileGateway，新增adapter/app/file独立Req/Res与POST /files/app/upload；网关仅该完整路径按会员身份认证，不公开，不改变后台/files/upload。multipart file只接收非空image/jpeg/png/webp且不超过10MiB；文件签名校验后送现有存储。图片URL由服务器返回，客户端只接受HTTPS。

小程序封装wx.chooseMedia和wx.uploadFile，会员Bearer，最多9张、每张10MiB，逐张上传，成功回填已有评价图片地址；部分成功保留，失败不伪装全成功，换号后不回填旧结果。未知上传不自动重发；评价结果未知时禁用图片修改。先接同页订单及原单项评价。无需新表/存储设施。验证网关身份、上传边界与小程序传输/页面，真实OSS、网关路由发布及真机需部署验收。不commit/push。

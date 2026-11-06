# 评价图片上传交付

2026-10-29，承接61设计。单项与同页多商品评价新增相册/拍照选择，wx.uploadFile使用会员Bearer上传到独立POST /files/app/upload。该完整路径按会员域校验，未公开、不改变后台上传身份。新增system adapter/app/file独立Req/Res/Controller，复用既有FileUploadCmdExe/FileGateway，不新增表或存储实现。

限制9张、每张10MiB、JPEG/PNG/WebP，服务端验证文件头及类型，文件后缀按校验类型生成。逐张上传，部分成功保留，失败停止，不自动重发；只回填HTTPS地址，换号拒绝旧响应。评价结果未知时禁用图片变更，上传中禁止提交评价。

system及gateway相关Maven verify通过；增强网关正向会员域验证后gateway测试再次通过。system133、gateway32项，失败/错误/跳过均0；小程序112项通过。新测试覆盖真实执行器流转、文件头/缺失/超限拒绝、会员Bearer及字段名、换号拒绝、取消、部分成功保留。文件存储已有本地端口测试最初受环境限制，经授权放开端口后完整回归通过。新小程序文件格式化、git diff --check通过。

未发布外部网关路由、未配置小程序合法域名、未写真实OSS、未真机验收；multipart服务端限制需部署配置配合。未commit/push、未执行DDL；无React变更未重跑。上传中断可能留未引用文件，不承诺事务清理。文章继续排除。

接口/部署要求：docs/order-item-comment-api.md；证据：evidence/comment-upload-verification.json。

# 设计附件验证

此目录不是运行时促销引擎，也不应被应用导入。算术校验器独立核对人工固定的pricing-vectors预期结果、分配守恒、随机排列不变性与旧券百分比舍入；没有数据库、鉴权、缓存或渠道行为实现。

运行环境：Python3、jsonschema==4.23.0（仅验证工具）。可在临时venv安装，不写入Java/React/小程序依赖：

```bash
python -m venv /tmp/lemnt-promotion-design-venv
/tmp/lemnt-promotion-design-venv/bin/pip install jsonschema==4.23.0
/tmp/lemnt-promotion-design-venv/bin/python docs/promotion/validation/validate_design.py
```

本轮实际使用临时PYTHONPATH安装位置，等效命令：

```bash
PYTHONPATH=/tmp/promotion-schema-validation python docs/promotion/validation/validate_design.py
```

检查内容：15个Schema定义、16份有效示例、文档JSON解析、13份应拒绝的Schema输入、4份跨字段错误草稿、23个固定算例、2000组随机分摊/单位分割、1500组legacy券百分比等价，以及14个实际源码文件的SHA256基线。随机种子固定314159。

source-baseline.json记录20ec7bd的实际文件。未来修改这些源文件会触发基线失配，应重新核对设计和迁移影响，不要为了通过而静默删除检查。Schema的custom format必须启用；默认JSON Schema实现可能忽略未知格式，服务端仍要执行long精确转换和领域校验。

PASS仅指设计附件。Java引擎、SQL迁移、Spring请求反序列化、并发事务、Maven/React/native完整回归和真实环境验证都不由此证明；对应T1..T7仍PLANNED。实际输出保存在work-items/yudao-member-mall-trade-migration/evidence/promotion-design/，无生产可用性声明。

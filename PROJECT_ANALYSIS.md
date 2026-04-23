# 校园跑腿与交易系统 - 项目深度分析与流程文档

本文件旨在深入剖析系统的业务架构、角色分工及其核心业务流向，为毕业论文的“系统设计”与“流程分析”章节提供素材。

---

## 1. 系统核心思路图 (Mind Map)

系统的设计理念是围绕“校园内微循环交易”展开，通过权限分层实现安全管控。

```mermaid
mindmap
  root((校园交易系统))
    用户端(User)
      购物: 商品浏览 / 购物车 / 订单管理 / 地址簿
      跑腿: 发布任务 / 历史记录 / 进度追踪
      维权: 投诉系统 / 退款申请
    跑腿端(Runner)
      抢单: 任务广场 / 并发控制
      配送: 状态更新 / 送达确认
      收益: 钱包余额 / 金额流水
    商家端(Merchant)
      商品: 商品发布 / 价格库存管理 / 下架
      订单: 待发货处理 / 确认发货
      结算: 收入统计 / 提现管理
    管理端(Admin)
      安全: 用户禁用与激活 / 角色审核
      合规: 商品审核 / 任务内容监管
      仲裁: 投诉处理 / 争议裁定
```

---

## 2. 核心角色任务流程图 (Flow Charts)

### 2.1 校园跑腿业务流程 (Errand Lifecycle)
跑腿业务涉及用户发布与跑腿员抢单，采用了并发锁控制与事务回滚机制。

```mermaid
graph TD
    A[用户: 发布任务] --> B[管理员: 内容审核]
    B -- 违规 --> C[任务驳回/删除]
    B -- 合规 --> D[任务大厅: 待接单]
    D --> E[跑腿员: 点击抢单]
    E --> F{并发校验: 状态=1?}
    F -- 失败: 已被抢 --> G[提示: 任务已被领取]
    F -- 成功 --> H[事务开启: 扣除余额/冻结金]
    H --> I[跑腿员: 配送中]
    I --> J[用户: 确认送达]
    J --> K[事务提交: 赏金转入跑腿员钱包]
```

### 2.2 商家商品交易流程 (Product Transaction)
涵盖了从商品发布到最终确认收货的闭环。

```mermaid
graph LR
    S1[商家: 发布商品] --> S2[用户: 加入购物车]
    S2 --> S3[库存校验: stock > count?]
    S3 -- 否 --> S4[拦截下单]
    S3 -- 是 --> S5[生成订单: 待付款]
    S5 --> S6[用户支付]
    S6 --> S7[商家: 确认发货]
    S7 --> S8[用户: 确认收货]
    S8 --> S9[结算流水: 记录商家收入]
```

### 2.3 投诉与仲裁处理流程 (Complaint Arbitration)
系统特有的维权流程，由管理员介入解决买卖双方争议。

```mermaid
sequenceDiagram
    participant U as 用户
    participant A as 管理员
    participant M as 商家/跑腿员
    U->>A: 发起投诉 (关联订单ID)
    Note over A: 后台显示 "待处理" 投诉
    A->>M: 查阅违规证据 (如未按时送达)
    A->>A: 判定责任方
    A->>U: 反馈处理结果 (处理完成)
    Note over U: 我的投诉可见管理员回复
```

---

## 3. 核心功能模块设计

### 3.1 身份认证与权限分流 (RBAC)
系统采用 `userType` 字段（0:Admin, 1:User, 2:Runner, 3:Merchant）进行权限隔离。
- **前端实现**：[App.vue](file:///Users/develop/final_college/demo/frontend/src/App.vue) 根据 `localStorage` 中的 `userType` 动态渲染底栏 Dock 菜单。
- **后端实现**：[JwtInterceptor.java](file:///Users/develop/final_college/demo/src/main/java/com/example/demo/interceptor/JwtInterceptor.java) 拦截 `/admin/**` 路径，强制校验管理员身份。

### 3.2 高并发接单安全性
为了防止多人抢同一单，在 [ErrandServiceImpl.java](file:///Users/develop/final_college/demo/src/main/java/com/example/demo/service/impl/ErrandServiceImpl.java) 中使用了 SQL 层面的条件更新：
```sql
update errand_order set status=2, runner_id=#{id} 
where order_no=#{no} and status=1 and runner_id is null;
```
若更新行数为 0，则说明任务已被抢走，抛出异常触发事务回滚。

### 3.3 购物流水与记录管理
商家和跑腿员在“我的”页面均有专门的“金额流水”模块。
- **商家端**：关注商品销售毛利。
- **跑腿员**：关注每单配送赏金的到账情况。
- **管理端**：可调取所有异常流水的原始记录。

---

## 4. 论文思路引导
在撰写论文时，您可以按照以下逻辑引用本 MD 文档：
1.  **系统分析**：引用“系统核心思路图”说明软件架构的完整性。
2.  **详细设计**：引用“核心角色任务流程图”说明业务逻辑的严密性。
3.  **技术实现**：引用“高并发接单安全性”和“RBAC 权限分流”作为技术亮点进行深入描述。

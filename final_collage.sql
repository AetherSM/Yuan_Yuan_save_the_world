create table categories
(
    category_id   int auto_increment
        primary key,
    category_name varchar(50)                         not null comment '分类名称',
    parent_id     int       default 0                 null comment '父分类ID,0表示顶级分类',
    sort_order    int       default 0                 null comment '排序',
    icon          varchar(255)                        null comment '图标URL',
    status        tinyint   default 1                 null comment '状态:0-禁用,1-启用',
    create_time   timestamp default CURRENT_TIMESTAMP null
)
    comment '商品分类表';

create index idx_parent
    on categories (parent_id);

create table users
(
    user_id      bigint auto_increment
        primary key,
    phone        varchar(11)                              not null comment '手机号',
    password     varchar(255)                             not null comment '密码(加密)',
    nickname     varchar(50)                              not null comment '昵称',
    avatar       varchar(255)                             null comment '头像URL',
    gender       tinyint        default 0                 null comment '性别:0-未知,1-男,2-女',
    student_id   varchar(20)                              null comment '学号',
    school       varchar(100)                             null comment '学校',
    dormitory    varchar(100)                             null comment '宿舍地址',
    balance      decimal(10, 2) default 0.00              null comment '账户余额',
    credit_score int            default 100               null comment '信用分',
    user_type    tinyint        default 1                 null comment '用户类型:1-普通用户,2-跑腿员,3-商家',
    status       tinyint        default 1                 null comment '状态:0-禁用,1-正常',
    create_time  timestamp      default CURRENT_TIMESTAMP null,
    update_time  timestamp      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint phone
        unique (phone),
    constraint users_pk
        unique (phone)
)
    comment '用户表';

create table addresses
(
    address_id    bigint auto_increment
        primary key,
    user_id       bigint                              not null comment '用户ID',
    contact_name  varchar(50)                         not null comment '收货人',
    contact_phone varchar(11)                         not null comment '联系电话',
    address       varchar(255)                        not null comment '详细地址',
    building      varchar(50)                         null comment '楼栋',
    room          varchar(20)                         null comment '房间号',
    is_default    tinyint   default 0                 null comment '是否默认:0-否,1-是',
    create_time   timestamp default CURRENT_TIMESTAMP null,
    update_time   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint addresses_ibfk_1
        foreign key (user_id) references users (user_id)
)
    comment '收货地址表';

create index idx_user
    on addresses (user_id);

create table errand_orders
(
    order_id         bigint auto_increment
        primary key,
    order_no         varchar(32)                              not null comment '订单号',
    user_id          bigint                                   not null comment '下单用户ID',
    runner_id        bigint                                   null comment '接单跑腿员ID',
    errand_type      tinyint                                  not null comment '跑腿类型:1-取快递,2-送外卖,3-代买,4-其他',
    title            varchar(100)                             not null comment '订单标题',
    description      text                                     null comment '详细描述',
    pickup_address   varchar(255)                             not null comment '取件地址',
    delivery_address varchar(255)                             not null comment '送达地址',
    contact_name     varchar(50)                              not null comment '联系人',
    contact_phone    varchar(11)                              not null comment '联系电话',
    pickup_code      varchar(10)                              null comment '取件码',
    expected_time_range varchar(50)                           null comment '期望送达时间段',
    reward           decimal(10, 2)                           not null comment '赏金',
    tip              decimal(10, 2) default 0.00              null comment '小费',
    total_amount     decimal(10, 2)                           not null comment '总金额',
    images           text                                     null comment '图片,JSON格式',
    deadline         timestamp                                null comment '期望完成时间',
    order_status     tinyint        default 1                 null comment '订单状态:1-待接单,2-已接单,3-配送中,4-已完成,5-已取消',
    cancel_reason    varchar(255)                             null comment '取消原因',
    remark           text                                     null comment '备注',
    accepted_at      timestamp                                null comment '接单时间',
    completed_at     timestamp                                null comment '完成时间',
    create_time      timestamp      default CURRENT_TIMESTAMP null,
    update_time      timestamp      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint order_no
        unique (order_no),
    constraint errand_orders_ibfk_1
        foreign key (user_id) references users (user_id),
    constraint errand_orders_ibfk_2
        foreign key (runner_id) references users (user_id)
)
    comment '跑腿订单表';

create index idx_order_no
    on errand_orders (order_no);

create index idx_runner
    on errand_orders (runner_id);

create index idx_status
    on errand_orders (order_status);

create index idx_user
    on errand_orders (user_id);

create table product_orders
(
    order_id         bigint auto_increment
        primary key,
    order_no         varchar(32)                         not null comment '订单号',
    user_id          bigint                              not null comment '买家ID',
    seller_id        bigint                              not null comment '卖家ID',
    total_amount     decimal(10, 2)                      not null comment '订单总额',
    delivery_address varchar(255)                        not null comment '收货地址',
    contact_name     varchar(50)                         not null comment '收货人',
    contact_phone    varchar(11)                         not null comment '联系电话',
    order_status     tinyint   default 1                 null comment '订单状态:1-待支付,2-待发货,3-待收货,4-已完成,5-已取消',
    pay_time         timestamp                           null comment '支付时间',
    ship_time        timestamp                           null comment '发货时间',
    confirm_time     timestamp                           null comment '确认收货时间',
    cancel_reason    varchar(255)                        null comment '取消原因',
    remark           varchar(255)                        null comment '买家备注',
    create_time      timestamp default CURRENT_TIMESTAMP null,
    update_time      timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint order_no
        unique (order_no),
    constraint product_orders_ibfk_1
        foreign key (user_id) references users (user_id),
    constraint product_orders_ibfk_2
        foreign key (seller_id) references users (user_id)
)
    comment '商品订单表';

create index idx_order_no
    on product_orders (order_no);

create index idx_seller
    on product_orders (seller_id);

create index idx_status
    on product_orders (order_status);

create index idx_user
    on product_orders (user_id);

create table products
(
    product_id     bigint auto_increment
        primary key,
    seller_id      bigint                              not null comment '卖家ID',
    category_id    int                                 not null comment '分类ID',
    product_name   varchar(100)                        not null comment '商品名称',
    description    text                                null comment '商品描述',
    price          decimal(10, 2)                      not null comment '价格',
    original_price decimal(10, 2)                      null comment '原价',
    stock          int       default 0                 null comment '库存',
    sales_count    int       default 0                 null comment '销量',
    main_image     varchar(255)                        null comment '主图',
    images         text                                null comment '商品图片,JSON格式',
    shipping_address varchar(255)                      null comment '发货地址',
    status         tinyint   default 1                 null comment '状态:0-下架,1-上架',
    create_time    timestamp default CURRENT_TIMESTAMP null,
    update_time    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint products_ibfk_1
        foreign key (seller_id) references users (user_id),
    constraint products_ibfk_2
        foreign key (category_id) references categories (category_id)
)
    comment '商品表';

create table order_items
(
    item_id       bigint auto_increment
        primary key,
    order_id      bigint                              not null comment '订单ID',
    product_id    bigint                              not null comment '商品ID',
    product_name  varchar(100)                        not null comment '商品名称',
    product_image varchar(255)                        null comment '商品图片',
    price         decimal(10, 2)                      not null comment '商品价格',
    quantity      int                                 not null comment '购买数量',
    subtotal      decimal(10, 2)                      not null comment '小计',
    create_time   timestamp default CURRENT_TIMESTAMP null,
    constraint order_items_ibfk_1
        foreign key (order_id) references product_orders (order_id),
    constraint order_items_ibfk_2
        foreign key (product_id) references products (product_id)
)
    comment '订单详情表';

create index idx_order
    on order_items (order_id);

create index product_id
    on order_items (product_id);

-- 购物车
create table if not exists cart_items
(
    cart_item_id bigint auto_increment primary key,
    user_id      bigint       not null comment '用户ID',
    product_id   bigint       not null comment '商品ID',
    quantity     int          not null default 1 comment '数量',
    create_time  timestamp    default CURRENT_TIMESTAMP null,
    update_time  timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint cart_items_user_product unique (user_id, product_id),
    constraint fk_cart_user foreign key (user_id) references users (user_id),
    constraint fk_cart_product foreign key (product_id) references products (product_id)
);

-- 购物记录
create table if not exists shopping_records
(
    record_id     bigint auto_increment primary key,
    user_id       bigint       not null,
    order_no      varchar(32)  not null,
    product_id    bigint       not null,
    product_name  varchar(100) not null,
    product_image varchar(255) null,
    price         decimal(10,2) not null,
    quantity      int          not null,
    subtotal      decimal(10,2) not null,
    created_at    timestamp    default CURRENT_TIMESTAMP null,
    index idx_user_created(user_id, created_at),
    constraint fk_sr_user foreign key (user_id) references users (user_id),
    constraint fk_sr_product foreign key (product_id) references products (product_id)
);


create index idx_category
    on products (category_id);

create index idx_seller
    on products (seller_id);

create index idx_status
    on products (status);

create table reviews
(
    review_id    bigint auto_increment
        primary key,
    order_id     bigint                              not null comment '订单ID',
    order_type   tinyint                             not null comment '订单类型:1-商品订单,2-跑腿订单',
    reviewer_id  bigint                              not null comment '评价人ID',
    reviewee_id  bigint                              not null comment '被评价人ID',
    rating       tinyint                             not null comment '评分:1-5星',
    content      text                                null comment '评价内容',
    images       text                                null comment '评价图片,JSON格式',
    is_anonymous tinyint   default 0                 null comment '是否匿名:0-否,1-是',
    create_time  timestamp default CURRENT_TIMESTAMP null,
    constraint reviews_ibfk_1
        foreign key (reviewer_id) references users (user_id),
    constraint reviews_ibfk_2
        foreign key (reviewee_id) references users (user_id)
)
    comment '评价表';

create index idx_order
    on reviews (order_id, order_type);

create index idx_reviewee
    on reviews (reviewee_id);

create index reviewer_id
    on reviews (reviewer_id);

create index idx_phone
    on users (phone);

create index idx_student_id
    on users (student_id);

create table wallet_transactions
(
    transaction_id   bigint auto_increment
        primary key,
    user_id          bigint                              not null comment '用户ID',
    transaction_type tinyint                             not null comment '交易类型:1-充值,2-提现,3-支付,4-收入,5-退款',
    amount           decimal(10, 2)                      not null comment '金额',
    balance_before   decimal(10, 2)                      not null comment '交易前余额',
    balance_after    decimal(10, 2)                      not null comment '交易后余额',
    related_order_no varchar(32)                         null comment '关联订单号',
    description      varchar(255)                        null comment '交易描述',
    create_time      timestamp default CURRENT_TIMESTAMP null,
    constraint wallet_transactions_ibfk_1
        foreign key (user_id) references users (user_id)
)
    comment '钱包流水表';

create index idx_order
    on wallet_transactions (related_order_no);

create index idx_user
    on wallet_transactions (user_id);



-- 优惠券表
create table coupons
(
    coupon_id   bigint auto_increment
        primary key,
    name        varchar(50)                         not null comment '优惠券名称',
    type        tinyint                             not null comment '类型:1-满减,2-折扣',
    value       decimal(10, 2)                      not null comment '面值/折扣率',
    min_spend   decimal(10, 2) default 0.00         null comment '最低消费门槛',
    total_count int                                 null comment '发行总量, null或0表示不限制',
    received_count int       default 0              null comment '已领取数量',
    start_time  timestamp                           null comment '有效期开始',
    end_time    timestamp                           null comment '有效期结束',
    status      tinyint        default 1            null comment '状态:0-无效,1-有效',
    create_time timestamp      default CURRENT_TIMESTAMP null,
    update_time timestamp      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '优惠券表';

-- 用户优惠券表
create table user_coupons
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                              not null comment '用户ID',
    coupon_id     bigint                              not null comment '优惠券ID',
    status        tinyint   default 0                 null comment '状态:0-未使用,1-已使用,2-已过期',
    used_order_id bigint                              null comment '使用的订单ID',
    create_time   timestamp default CURRENT_TIMESTAMP null,
    used_time     timestamp                           null comment '使用时间',
    constraint user_coupons_ibfk_1
        foreign key (user_id) references users (user_id),
    constraint user_coupons_ibfk_2
        foreign key (coupon_id) references coupons (coupon_id)
)
    comment '用户优惠券表';

create index idx_user_coupon
    on user_coupons (user_id);

-- 聊天记录表
create table chat_messages
(
    message_id  bigint auto_increment
        primary key,
    sender_id   bigint                              not null comment '发送者ID',
    receiver_id bigint                              not null comment '接收者ID',
    content     text                                null comment '消息内容',
    msg_type    tinyint   default 1                 null comment '类型:1-文字,2-图片',
    is_read     tinyint   default 0                 null comment '是否已读',
    create_time timestamp default CURRENT_TIMESTAMP null,
    constraint chat_messages_ibfk_1
        foreign key (sender_id) references users (user_id),
    constraint chat_messages_ibfk_2
        foreign key (receiver_id) references users (user_id)
)
    comment '聊天记录表';

create index idx_sender_receiver
    on chat_messages (sender_id, receiver_id);

-- 求购信息表
create table wanted_posts
(
    post_id     bigint auto_increment
        primary key,
    user_id     bigint                              not null comment '发布人ID',
    title       varchar(100)                        not null comment '标题',
    description text                                null comment '描述',
    price_range varchar(50)                         null comment '期望价格范围',
    status      tinyint   default 1                 null comment '状态:1-求购中,2-已买到,3-已关闭',
    create_time timestamp default CURRENT_TIMESTAMP null,
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint wanted_posts_ibfk_1
        foreign key (user_id) references users (user_id)
)
    comment '求购信息表';

-- 投诉表
create table complaints
(
    complaint_id   bigint auto_increment
        primary key,
    order_id       bigint                              not null comment '订单ID',
    order_type     tinyint                             not null comment '订单类型:1-商品,2-跑腿',
    complainant_id bigint                              not null comment '投诉人ID',
    reason         varchar(255)                        not null comment '投诉原因',
    status         tinyint   default 0                 null comment '状态:0-待处理,1-处理中,2-已完成',
    result         text                                null comment '处理结果',
    create_time    timestamp default CURRENT_TIMESTAMP null,
    update_time    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint complaints_ibfk_1
        foreign key (complainant_id) references users (user_id)
)
    comment '投诉表';

create index idx_complainant
    on complaints (complainant_id);

-- 角色申请表
create table role_applications
(
    id            bigint auto_increment primary key,
    user_id       bigint                              not null comment '用户ID',
    current_type  tinyint                             null comment '当前角色',
    target_type   tinyint                             not null comment '目标角色',
    reason        varchar(255)                        not null comment '申请理由',
    status        tinyint   default 0                 not null comment '状态:0-待审核,1-通过,2-拒绝',
    handler_id    bigint                              null comment '审核管理员ID',
    handle_remark varchar(255)                        null comment '审核备注',
    create_time   timestamp default CURRENT_TIMESTAMP null,
    update_time   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint role_applications_ibfk_1
        foreign key (user_id) references users (user_id)
)
    comment '角色申请表';

create index idx_role_applications_user
    on role_applications (user_id);

create index idx_role_applications_status
    on role_applications (status);

-- 评价回复表
create table review_replies
(
    reply_id       bigint auto_increment
        primary key,
    review_id      bigint                              not null comment '评价ID',
    replier_id     bigint                              not null comment '回复人ID',
    content        text                                not null comment '回复内容',
    create_time    timestamp default CURRENT_TIMESTAMP null,
    constraint review_replies_ibfk_1
        foreign key (review_id) references reviews (review_id),
    constraint review_replies_ibfk_2
        foreign key (replier_id) references users (user_id)
)
    comment '评价回复表';

create index idx_review
    on review_replies (review_id);

-- 退款申请表
create table refund_requests
(
    id            bigint auto_increment primary key,
    order_type    tinyint                             not null comment '订单类型: 1-商品订单, 2-跑腿订单',
    order_id      bigint                              not null comment '订单ID',
    order_no      varchar(32)                         not null comment '订单号',
    applicant_id  bigint                              not null comment '申请人ID(用户)',
    refund_amount decimal(10, 2)                      not null comment '退款金额',
    reason        varchar(500)                        null comment '退款原因',
    status        tinyint   default 0                 not null comment '状态: 0-待处理, 1-已同意, 2-已退款, 3-已拒绝',
    handler_id    bigint                              null comment '处理人ID(商家/跑腿员/管理员)',
    handle_remark varchar(255)                        null comment '处理备注',
    handle_time   timestamp                           null comment '处理时间',
    create_time   timestamp default CURRENT_TIMESTAMP null,
    update_time   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint refund_requests_ibfk_1 foreign key (applicant_id) references users (user_id)
) comment '退款申请表';

create index idx_refund_order on refund_requests (order_type, order_id);
create index idx_refund_applicant on refund_requests (applicant_id);
create index idx_refund_status on refund_requests (status);


-- 模拟数据插入脚本
USE final_college;

-- 1. 插入用户数据 (密码均为 123456 -> e10adc3949ba59abbe56e057f20f883e)
INSERT INTO users (user_id, phone, password, nickname, avatar, gender, student_id, school, dormitory, balance, credit_score, user_type, status) VALUES
(99, '13800000000', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 1, NULL, '管理中心', '行政楼101', 0.00, 100, 0, 1), -- 管理员
(1, '13800000001', 'e10adc3949ba59abbe56e057f20f883e', '校园超市', 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', 1, NULL, '清华大学', '商业街101', 10000.00, 100, 3, 1), -- 商家
(2, '13800000002', 'e10adc3949ba59abbe56e057f20f883e', '飞毛腿跑腿', 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', 1, '2021002', '清华大学', '紫荆公寓1号楼', 500.00, 100, 2, 1), -- 跑腿员
(3, '13800000003', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', 1, '2021003', '清华大学', '紫荆公寓2号楼', 1000.00, 100, 1, 1), -- 普通用户
(4, '13800000004', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', 2, '2021004', '清华大学', '紫荆公寓3号楼', 200.00, 100, 1, 1); -- 普通用户

-- 2. 插入地址数据
INSERT INTO addresses (address_id, user_id, contact_name, contact_phone, address, building, room, is_default) VALUES
(1, 1, '店长', '13800000001', '商业街101号', '商业街', '101', 1),
(2, 3, '张三', '13800000003', '紫荆公寓2号楼301', '2号楼', '301', 1),
(3, 4, '李四', '13800000004', '紫荆公寓3号楼402', '3号楼', '402', 1);

-- 3. 插入商品分类
INSERT INTO categories (category_id, category_name, parent_id, sort_order, status) VALUES
(1, '餐饮美食', 0, 1, 1),
(2, '二手书籍', 0, 2, 1),
(3, '数码产品', 0, 3, 1),
(4, '日用百货', 0, 4, 1),
(5, '零食饮料', 1, 1, 1),
(6, '教材教辅', 2, 1, 1);

-- 4. 插入商品数据 (商家ID: 1)
INSERT INTO products (product_id, seller_id, category_id, product_name, description, price, original_price, stock, sales_count, main_image, status) VALUES
(1, 1, 5, '可口可乐 330ml', '碳酸饮料，快乐水', 3.00, 3.50, 100, 50, '/uploads/products/coke.png', 1),
(2, 1, 5, '卫龙辣条', '童年回忆，经典美味', 5.00, 6.00, 200, 100, '/uploads/products/latiao.png', 1),
(3, 1, 6, 'Java编程思想', '经典Java教材，二手九成新', 45.00, 99.00, 1, 0, '/uploads/products/java_book.png', 1),
(4, 1, 3, '二手机械键盘', 'Cherry青轴，手感极佳', 150.00, 400.00, 1, 0, '/uploads/products/keyboard.png', 1),
(5, 1, 4, '洗衣液 2kg', '深层清洁，护衣护色', 25.00, 39.90, 50, 10, '/uploads/products/detergent.png', 1);

-- 5. 插入优惠券
INSERT INTO coupons (coupon_id, name, type, value, min_spend, start_time, end_time, status) VALUES
(1, '新用户专享券', 1, 10.00, 20.00, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(2, '全场9折券', 2, 0.90, 0.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

-- 更多优惠券（包含发行总量与已领取数）
INSERT INTO coupons (name, type, value, min_spend, total_count, received_count, start_time, end_time, status) VALUES
('满100减20', 1, 20.00, 100.00, 500, 0, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1),
('全场95折', 2, 0.95, 0.00, 1000, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
('饮料专享券', 1, 5.00, 15.00, 300, 0, NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), 1),
('新人立减5元', 1, 5.00, 10.00, NULL, 0, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1);

-- 6. 用户领取优惠券
INSERT INTO user_coupons (user_id, coupon_id, status) VALUES
(3, 1, 0), -- 张三领取了新用户券
(4, 2, 0); -- 李四领取了折扣券

-- 7. 插入跑腿订单
-- 订单1: 待接单 (张三发布)
INSERT INTO errand_orders (order_no, user_id, errand_type, title, description, pickup_address, delivery_address, contact_name, contact_phone, reward, total_amount, order_status, create_time) VALUES
('E202310010001', 3, 1, '帮取快递', '中通快递，取件码12-34-56', '东门菜鸟驿站', '紫荆公寓2号楼301', '张三', '13800000003', 5.00, 5.00, 1, NOW());

-- 订单2: 已接单 (李四发布，跑腿员接单)
INSERT INTO errand_orders (order_no, user_id, runner_id, errand_type, title, description, pickup_address, delivery_address, contact_name, contact_phone, reward, total_amount, order_status, accepted_at, create_time) VALUES
('E202310010002', 4, 2, 2, '帮买午饭', '紫荆园食堂二楼麻辣香锅', '紫荆园食堂', '紫荆公寓3号楼402', '李四', '13800000004', 8.00, 8.00, 2, NOW(), DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 订单3: 已完成 (张三发布，跑腿员完成)
INSERT INTO errand_orders (order_no, user_id, runner_id, errand_type, title, description, pickup_address, delivery_address, contact_name, contact_phone, reward, total_amount, order_status, accepted_at, completed_at, create_time) VALUES
('E202310010003', 3, 2, 3, '代买感冒药', '需要购买感冒灵颗粒', '校医院药房', '紫荆公寓2号楼301', '张三', '13800000003', 10.00, 10.00, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 8. 插入商品订单
-- 订单1: 待支付 (张三买可乐)
INSERT INTO product_orders (order_no, user_id, seller_id, total_amount, delivery_address, contact_name, contact_phone, order_status, create_time) VALUES
('P202310010001', 3, 1, 15.00, '紫荆公寓2号楼301', '张三', '13800000003', 1, NOW());

INSERT INTO order_items (order_id, product_id, product_name, product_image, price, quantity, subtotal) VALUES
(1, 1, '可口可乐 330ml', 'https://example.com/coke.jpg', 3.00, 5, 15.00);

-- 订单2: 已完成 (李四买辣条)
INSERT INTO product_orders (order_no, user_id, seller_id, total_amount, delivery_address, contact_name, contact_phone, order_status, pay_time, ship_time, confirm_time, create_time) VALUES
('P202310010002', 4, 1, 25.00, '紫荆公寓3号楼402', '李四', '13800000004', 4, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO order_items (order_id, product_id, product_name, product_image, price, quantity, subtotal) VALUES
(2, 2, '卫龙辣条', 'https://example.com/latiao.jpg', 5.00, 5, 25.00);

-- 9. 插入钱包流水
-- 张三充值
INSERT INTO wallet_transactions (user_id, transaction_type, amount, balance_before, balance_after, description, create_time) VALUES
(3, 1, 1000.00, 0.00, 1000.00, '余额充值', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 10. 插入求购信息
INSERT INTO wanted_posts (user_id, title, description, price_range, status, create_time) VALUES
(3, '求购二手自行车', '九成新最好，价格可议', '100-200元', 1, NOW()),
(4, '求购考研英语资料', '红宝书、黄皮书', '50元左右', 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 11. 插入评价
INSERT INTO reviews (order_id, order_type, reviewer_id, reviewee_id, rating, content, create_time) VALUES
(3, 2, 3, 2, 5, '跑腿小哥速度很快，态度很好！', NOW()), -- 张三评价跑腿员
(2, 1, 4, 1, 4, '辣条味道不错，就是物流稍微慢了点。', NOW()); -- 李四评价商家

-- 12. 插入聊天记录
INSERT INTO chat_messages (sender_id, receiver_id, content, msg_type, is_read, create_time) VALUES
(3, 2, '你好，请问多久能送到？', 1, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 3, '大概10分钟左右。', 1, 1, DATE_SUB(NOW(), INTERVAL 55 MINUTE));

-- 13. 插入投诉
INSERT INTO complaints (order_id, order_type, complainant_id, reason, status, create_time) VALUES
(2, 1, 4, '物流太慢', 0, NOW());

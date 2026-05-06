-- 买家端「删除订单记录」：仅从买家列表隐藏，不影响商家与数据行
ALTER TABLE product_orders
    ADD COLUMN hidden_from_buyer TINYINT NOT NULL DEFAULT 0
        COMMENT '买家是否已从我的订单中移除(0否1是，软删)'
    AFTER remark;

CREATE INDEX idx_product_orders_buyer_hidden ON product_orders (user_id, hidden_from_buyer);

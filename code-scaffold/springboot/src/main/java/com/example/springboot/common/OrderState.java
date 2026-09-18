package com.example.springboot.common;

/**
 * 订单状态字典（前后端唯一权威来源，业务代码禁止再写散字符串）。
 *
 * 状态流转：
 * 待付款 →（支付）→ 已支付 →（商户发货）→ 已发货 →（用户确认收货，预留）→ 已完成
 * 待付款 →（取消）→ 已取消
 * 退款相关（退款中/已退款）属 Phase 4，暂不引入。
 */
public final class OrderState {

    public static final String PENDING_PAY = "待付款";
    public static final String PAID = "已支付";
    public static final String SHIPPED = "已发货";
    public static final String COMPLETED = "已完成";
    public static final String CANCELLED = "已取消";

    private OrderState() {
    }

    /** 是否为合法状态值 */
    public static boolean isValid(String state) {
        return PENDING_PAY.equals(state) || PAID.equals(state) || SHIPPED.equals(state)
                || COMPLETED.equals(state) || CANCELLED.equals(state);
    }
}

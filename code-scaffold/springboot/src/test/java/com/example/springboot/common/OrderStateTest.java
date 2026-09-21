package com.example.springboot.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderState 状态字典单元测试。
 */
@DisplayName("OrderState 订单状态字典")
class OrderStateTest {

    @Test
    @DisplayName("五个合法状态值与流转路径")
    void validStates() {
        assertTrue(OrderState.isValid(OrderState.PENDING_PAY));
        assertTrue(OrderState.isValid(OrderState.PAID));
        assertTrue(OrderState.isValid(OrderState.SHIPPED));
        assertTrue(OrderState.isValid(OrderState.COMPLETED));
        assertTrue(OrderState.isValid(OrderState.CANCELLED));

        assertFalse(OrderState.isValid("退款中"), "Phase 4 状态暂不引入");
        assertFalse(OrderState.isValid(""));
        assertFalse(OrderState.isValid(null));
        assertFalse(OrderState.isValid("任意字符串"));
    }

    @Test
    @DisplayName("状态值字面量稳定（前后端契约，改了会破坏存量数据语义）")
    void literalsAreStable() {
        assertEquals("待付款", OrderState.PENDING_PAY);
        assertEquals("已支付", OrderState.PAID);
        assertEquals("已发货", OrderState.SHIPPED);
        assertEquals("已完成", OrderState.COMPLETED);
        assertEquals("已取消", OrderState.CANCELLED);
    }
}

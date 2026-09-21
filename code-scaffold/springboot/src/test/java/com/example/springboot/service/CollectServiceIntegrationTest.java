package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.CollectMapper;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.utils.TokenUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 收藏 toggle 集成测试（连接本地真实库 bil_mall）。
 *
 * 登录态模拟：TokenUtils 从 request attribute 取身份（JwtInterceptor 写入），
 * 测试里用 MockHttpServletRequest 伪造已登录用户。
 * 事务回滚：@Transactional 让每个测试的数据改动自动回滚，不污染库。
 */
@SpringBootTest
@DisplayName("收藏服务（toggle 语义）")
class CollectServiceIntegrationTest {

    /** 复用真实存在的测试用户（user 表 id=4，账号 123），只写自己的收藏 */
    private static final Integer TEST_USER_ID = 4;

    @Autowired
    private ICollectService collectService;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @BeforeEach
    void setUp() {
        mockLogin(TEST_USER_ID, TokenUtils.ROLE_USER);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void mockLogin(Integer userId, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TokenUtils.ATTR_LOGIN_ID, userId);
        request.setAttribute(TokenUtils.ATTR_LOGIN_ROLE, role);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Goods createTestGoods() {
        Goods goods = new Goods();
        goods.setName("__test_collect_goods__");
        goods.setPrice(9.99);
        goods.setStore(50);
        goods.setState("上架");
        goods.setMerchantId(1);
        goods.setTypeId(1);
        goods.setSales(0);
        goods.setDate("2026-09-21");
        goodsMapper.insert(goods);
        return goods;
    }

    private boolean exists(Integer goodsId) {
        LambdaQueryWrapper<Collect> qw = new LambdaQueryWrapper<>();
        qw.eq(Collect::getUserId, TEST_USER_ID);
        qw.eq(Collect::getGoodsId, goodsId);
        return collectMapper.selectOne(qw) != null;
    }

    @Test
    @Transactional
    @DisplayName("toggle：未收藏 → 收藏成功且记录落库")
    void toggleAddsCollect() {
        Goods goods = createTestGoods();
        Collect c = new Collect();
        c.setGoodsId(goods.getId());

        collectService.save(c);   // 不抛异常 = 收藏成功

        assertTrue(exists(goods.getId()), "toggle 后 collect 表应有记录");
    }

    @Test
    @Transactional
    @DisplayName("toggle：已收藏再点 → 删除记录并抛 201 已取消收藏")
    void toggleRemovesCollectAndThrows201() {
        Goods goods = createTestGoods();
        Collect c = new Collect();
        c.setGoodsId(goods.getId());
        collectService.save(c);
        assertTrue(exists(goods.getId()));

        ServiceException e = assertThrows(ServiceException.class, () -> collectService.save(c));
        assertEquals("201", e.getCode(), "取消收藏语义用 code=201 传递");
        assertFalse(exists(goods.getId()), "二次 toggle 后记录应被删除");
    }
}

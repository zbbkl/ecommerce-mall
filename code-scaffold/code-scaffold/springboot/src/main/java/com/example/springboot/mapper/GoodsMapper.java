package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 原子扣减库存并累加销量。
     * 库存判断放进 SQL，靠数据库保证并发下不超卖；返回 0 表示库存不足。
     */
    @Update("update goods set store = store - #{nums}, sales = sales + #{nums} " +
            "where id = #{goodsId} and store >= #{nums}")
    int deductStore(@Param("goodsId") Integer goodsId, @Param("nums") Integer nums);
}
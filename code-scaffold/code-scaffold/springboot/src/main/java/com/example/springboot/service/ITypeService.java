package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Type;

import java.util.List;

public interface ITypeService {

    /**
     * 新增
     */
    void save(Type type);

    /**
     * 修改
     */
    void update(Type type);

    /**
     * 删除
     */
    void remove(Integer id);

    /**
     * 查询全部数据
     */
    List<Type> selectAll();

    /**
     * 根据ID查询数据
     */
    Type selectById(Integer id);

    /**
     * 分页查询数据
     */
    IPage<Type> selectPage(Integer pageNum, Integer pageSize, String name);

}

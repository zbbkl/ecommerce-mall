package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 修改密码：原始密码校验改在 Service 层做（BCrypt 散列无法走 SQL 等值比较），
     * 这里只按 id 覆盖新密码。
     */
    @Update("update user set password = #{newPassword} where id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("newPassword") String newPassword);

    /**
     * 充值：服务端原子加余额（amount 必须为正，防并发丢更新）
     */
    @Update("update user set account = account + #{amount} where id = #{id}")
    int recharge(@Param("id") Integer id, @Param("amount") java.math.BigDecimal amount);
}

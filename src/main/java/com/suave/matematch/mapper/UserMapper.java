package com.suave.matematch.mapper;

import com.suave.matematch.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author Suave
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}



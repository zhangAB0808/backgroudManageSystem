package com.zhang.mapper;

import com.zhang.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Entity com.zhang.entity.RoleMenu
 */
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    @Delete("delete from sys_role_menu where role_id=#{roleId}")
    int deleteByRoleId(Integer roleId);

    @Select("select menu_id from sys_role_menu where role_id=#{roleId}")
    List<Integer> selectByRoleId(Integer roleId);
}





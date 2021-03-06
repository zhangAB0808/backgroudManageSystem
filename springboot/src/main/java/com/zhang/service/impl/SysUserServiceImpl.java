package com.zhang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.common.Result;
import com.zhang.controller.dto.UserDto;
import com.zhang.entity.SysMenu;
import com.zhang.entity.SysUser;
import com.zhang.exception.ServiceException;
import com.zhang.mapper.RoleMenuMapper;
import com.zhang.mapper.SysRoleMapper;
import com.zhang.mapper.SysUserMapper;
import com.zhang.service.SysMenuService;
import com.zhang.service.SysUserService;
import com.zhang.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zhang.common.Constants.*;

/**
 *
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private SysMenuService menuService;

    public Map<String, Object> findPage(Integer pageNum, Integer pageSize, String username, String email, String address) {
        IPage<SysUser> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        if (!"".equals(username)) {
            queryWrapper.like("username", username);
        }

        if (!"".equals(email)) {
            queryWrapper.like("email", email);
        }
        if (!"".equals(address)) {
            queryWrapper.like("address", address);
        }
        queryWrapper.orderByDesc("id");
        IPage<SysUser> iPage = this.page(page, queryWrapper);
        List<SysUser> records = iPage.getRecords();
        long total = iPage.getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("records", records);
        map.put("total", total);
        return map;
    }

    @Override
    public Result login(UserDto user) {
        String username = user.getUsername();
        String password = user.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            return Result.error(CODE_400, "????????????");
        }
        //???????????????MD5??????????????? ??????
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password);
        SysUser userOne;
        try {
            userOne = userMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new ServiceException(CODE_500, "????????????");
        }

        if (userOne != null) {
            BeanUtils.copyProperties(userOne, user);
            String token = TokenUtils.genToken(userOne.getId().toString(),userOne.getPassword());
            user.setPassword("");
            user.setToken(token);
            String roleName = userOne.getRole();
            //???????????????????????????????????????
            List<SysMenu> roleMenus = getRoleMenus(roleName);
            user.setMenus(roleMenus);
            return Result.success(user);
        } else {
            throw new ServiceException(CODE_600, "????????????????????????");
        }
    }

    @Override
    public Result register(UserDto user) {
        String username = user.getUsername();
        String password = user.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            return Result.error(CODE_400, "????????????");
        }
        //??????????????????????????????MD5??????????????? ????????????????????????
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password);
        SysUser userOne = userMapper.selectOne(queryWrapper);
        if (userOne == null) {
            userOne = new SysUser();
           BeanUtils.copyProperties(user,userOne);
            if (this.save(userOne)) {
                return Result.success();
            } else {
                return Result.error();
            }
        } else {
            return Result.error(CODE_600, "???????????????");
        }
    }


    /**
     * ?????????????????????????????????
     * @param roleName
     * @return
     */
    private List<SysMenu> getRoleMenus(String roleName){
        //????????????????????????id
        Integer roleId = roleMapper.getRoleIdByName(roleName);
        //?????????????????????????????????id??????
        List<Integer> menuIds = roleMenuMapper.selectByRoleId(roleId);
        //????????????????????????
        List<SysMenu> menus = menuService.findMenus("");

        ArrayList<SysMenu> roleMenus = new ArrayList<>();
        //?????????????????????????????????
        for (SysMenu menu : menus) {
            if(menuIds.contains(menu.getId())){
                roleMenus.add(menu);
            }
            List<SysMenu> children = menu.getChildren();
            //??????children????????????menuIds????????????????????????
            children.removeIf(child->!menuIds.contains(child.getId()));
        }
        return roleMenus;
    }

}





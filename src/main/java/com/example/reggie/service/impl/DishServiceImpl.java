package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.mapper.DishMapper;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品，同时保存对应的口味数据
    @Override
    @Transactional  //设计多张表操作 开启事务
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品
        this.save(dishDto);

        //保存口味
        //实体中会自动生成id，这里再获取id
        long dishId = dishDto.getId();
        //将DishFlavor中每个实体都赋值Id
        List<DishFlavor> list = dishDto.getFlavors();
        List<DishFlavor> flavors = list.stream().map(new Function<DishFlavor, DishFlavor>() {
            @Override
            public DishFlavor apply(DishFlavor dishFlavor) {
                dishFlavor.setDishId(dishId);
                return dishFlavor;
            }
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(long id) {
        //查询基本信息
        Dish dish = this.getById(id);
        //查询口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //与新增不完全相同，因为更新时可能涉及到口味的增加和减少
        //这里分为三部

        //更新dish表基本信息
        this.updateById(dishDto);

        //更新flavor表
        //清理当前菜品口味数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //添加提交的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(new Function<DishFlavor, DishFlavor>() {
            @Override
            public DishFlavor apply(DishFlavor dishFlavor) {
                dishFlavor.setDishId(dishDto.getId());
                return dishFlavor;
            }
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}

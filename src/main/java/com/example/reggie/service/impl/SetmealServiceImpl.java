package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;
import com.example.reggie.mapper.SetmealMapper;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息
        this.save(setmealDto);
        //保存套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).toList();
        setmealDishService.saveBatch(list);
    }

    //多选删除套餐
    @Override
    @Transactional
    public void removeWithDish(long[] ids) {
        //查询套餐状态，确认是否可以删除
        List<Long> idsList = Arrays.stream(ids).boxed().toList();
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .in(Setmeal::getId, idsList)
                .eq(Setmeal::getStatus, 1);
        int count = (int) this.count(lambdaQueryWrapper);   //售卖中的套餐数量
        //不能删除则抛出异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //可以删，先删套餐表

        this.removeByIds(idsList);
        //再删关系表
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(SetmealDish::getSetmealId, idsList);
        setmealDishService.remove(lambdaQueryWrapper1);
    }
}

package com.devvv.user.web.manager;

import com.devvv.commons.common.enums.user.UserKeyMarkType;
import com.devvv.user.web.dao.entity.UUserKeyMark;
import com.devvv.user.web.dao.mapper.UUserKeyMarkMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Create by WangSJ on 2024/07/08
 */
@Slf4j
@Component
public class UserKeyMarkManager {

    @Resource
    private UUserKeyMarkMapper userKeyMarkMapper;

    /**
     * 获取用户标记
     */
    public UUserKeyMark getUserKeyMark(Long userId, UserKeyMarkType key) {
        return userKeyMarkMapper.selectByPrimaryKey(new UUserKeyMark(userId, key));
    }

    /**
     * 更新用户标记
     */
    public void setUserKeyMark(Long userId, UserKeyMarkType key, String value) {
        setUserKeyMark(userId, key, value, null);
    }
    public void setUserKeyMark(Long userId, UserKeyMarkType key, String value, String ext) {
        UUserKeyMark userKeyMark = new UUserKeyMark(userId, key);
        userKeyMark.setValue(value);
        userKeyMark.setExt(ext);
        if (userKeyMarkMapper.updateByPrimaryKeySelective(userKeyMark) <= 0) {
            userKeyMark.setCreateTime(new Date());
            userKeyMarkMapper.insert(userKeyMark);
        }
    }

    /**
     * 删除用户标记
     */
    public void deleteUserKeyMark(Long userId, UserKeyMarkType key) {
        UUserKeyMark userKeyMark = new UUserKeyMark(userId, key);
        userKeyMarkMapper.deleteByPrimaryKey(userKeyMark);
    }
}

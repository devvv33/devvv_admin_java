package com.devvv.commons.token.listener;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.StpLogic;

/**
 * Create by WangSJ on 2024/07/08
 */
public class ClearTokenListener extends SaTokenListenerForSimple {

    /**
     * 每次被踢下线时触发
     * @param loginType 账号类别
     * @param loginId 账号id
     * @param tokenValue token值
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue){
        // 原逻辑：给旧token设置一个状态值，等待其登录时再检查状态并提醒
        // 这里我们直接删除TokenSession
        StpLogic stpLogic = SaManager.getStpLogic(loginType, false);
        stpLogic.deleteTokenSession(tokenValue);            // 删除tokenSession
        stpLogic.deleteTokenToIdMapping(tokenValue);        // 删除token记录
    }

    /**
     * 每次被顶下线时触发
     * @param loginType 账号类别
     * @param loginId 账号id
     * @param tokenValue token值
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue){
        // 直接删除TokenSession，不需要等待其登录时再检查状态并提醒
        // 这里我们直接删除TokenSession
        StpLogic stpLogic = SaManager.getStpLogic(loginType, false);
        stpLogic.deleteTokenSession(tokenValue);            // 删除tokenSession
        stpLogic.deleteTokenToIdMapping(tokenValue);        // 删除token记录
    }
}

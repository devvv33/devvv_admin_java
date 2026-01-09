package com.devvv.commons.manager.sys.dao.sys.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统参数配置-历史记录表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysSettingHistory {
    /**
     * 日志ID
     */
    private Long id;

    /**
     * 参数名
     */
    private String key;

    /**
     * 参数值
     */
    private String value;

    /**
     * 是否只读
     */
    private Boolean readOnly;

    /**
     * 序号
     */
    private Integer sort;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 变动说明
     */
    private String changeRemark;

    /**
     * 修改人
     */
    private Long changeAdminId;

    /**
     * 修改时间
     */
    private Date changeTime;

    /**
     * 其他字段
     */
    private String changeAdminName;
}
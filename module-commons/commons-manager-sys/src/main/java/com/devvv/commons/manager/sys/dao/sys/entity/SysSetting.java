package com.devvv.commons.manager.sys.dao.sys.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统参数配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysSetting {
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
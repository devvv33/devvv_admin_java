package com.devvv.commons.manager.sys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Create by WangSJ on 2024/07/22
 */
@ComponentScan("com.devvv.commons.manager.sys")
@MapperScan(basePackages = "com.devvv.commons.manager.**.mapper")
public class SysModuleImport {
}

package com.devvv.commons.common.enums.app;


import java.lang.annotation.*;

/**
 * 应用标识
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApplicationType {

    AppType value();
}

package com.devvv.commons.core.config.cache.table.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.core.config.cache.table.manager.CacheExecutor;
import com.devvv.commons.core.config.cache.table.manager.MethodDefaultDelegater;
import com.devvv.commons.core.config.datasource.annotation.Table;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by WangSJ on 2023/08/07
 *
 * 表缓存拦截器
 */
@Slf4j
@Aspect
public class TableCacheAspect {

    @Resource
    private CacheExecutor executor;

    @Pointcut("@within(com.devvv.commons.core.config.datasource.annotation.Table)")
    public void tablePoint(){}

    /**
     * 切面
     */
    @Around(value = "execution(* com.devvv..mapper..*.insert*(*)) && tablePoint()")
    public Object insert(ProceedingJoinPoint point) throws Throwable {
        // insert语句，先执行插入
        Object result = point.proceed();
        if (!(result instanceof Integer rows) || rows <= 0) {
            return result;
        }
        // 再处理缓存
        Pair<Class<?>, Table> tablePair = findTableAnnotation(point.getTarget().getClass());
        if (tablePair == null || !tablePair.getValue().useTableCache()) {
            return result;
        }
        Object[] args = point.getArgs();
        if (args == null || args.length != 1) {
            return result;
        }
        Class<?> targetClass = tablePair.getKey();
        Table tableAnnotation = tablePair.getValue();
        String key = buildKeyFromBean(tableAnnotation, args[0], targetClass.getSimpleName());
        if (StrUtil.isBlank(key)) {
            return result;
        }
        String selectAllKey = buildSelectAllKey(targetClass.getSimpleName());
        executor.insert(tableAnnotation, key, selectAllKey, args[0]);
        return result;
    }
    @Around(value = "execution(* com.devvv..mapper..*.updateByPrimaryKey(*)) && tablePoint()")
    public Object updateByPrimaryKey(ProceedingJoinPoint point) throws Throwable {
        Pair<Class<?>, Table> tablePair = findTableAnnotation(point.getTarget().getClass());
        if (tablePair == null || !tablePair.getValue().useTableCache()) {
            return point.proceed();
        }
        Object[] args = point.getArgs();
        if (args == null || args.length != 1) {
            return point.proceed();
        }
        Class<?> targetClass = tablePair.getKey();
        Table tableAnnotation = tablePair.getValue();
        String key = buildKeyFromBean(tableAnnotation, args[0], targetClass.getSimpleName());
        if (StrUtil.isBlank(key)) {
            return point.proceed();
        }
        String selectAllKey = buildSelectAllKey(targetClass.getSimpleName());
        return executor.updateByPrimaryKey(tableAnnotation, key, selectAllKey, new MethodDefaultDelegater<Integer>(point));
    }

    @Around(value = "execution(* com.devvv..mapper..*.updateByPrimaryKeySelective(*)) && tablePoint()")
    public Object updateByPrimaryKeySelective(ProceedingJoinPoint point) throws Throwable {
        Pair<Class<?>, Table> tablePair = findTableAnnotation(point.getTarget().getClass());
        if (tablePair == null || !tablePair.getValue().useTableCache()) {
            return point.proceed();
        }
        Object[] args = point.getArgs();
        if (args == null || args.length != 1) {
            return point.proceed();
        }
        Class<?> targetClass = tablePair.getKey();
        Table tableAnnotation = tablePair.getValue();
        String key = buildKeyFromBean(tableAnnotation, args[0], targetClass.getSimpleName());
        if (StrUtil.isBlank(key)) {
            return point.proceed();
        }
        String selectAllKey = buildSelectAllKey(targetClass.getSimpleName());
        return executor.updateByPrimaryKey(tableAnnotation, key, selectAllKey, new MethodDefaultDelegater<Integer>(point));
    }

    @Around(value = "execution(* com.devvv..mapper..*.deleteByPrimaryKey(..)) && tablePoint()")
    public Object deleteByPrimaryKey(ProceedingJoinPoint point) throws Throwable {
        Pair<Class<?>, Table> tablePair = findTableAnnotation(point.getTarget().getClass());
        if (tablePair == null || !tablePair.getValue().useTableCache()) {
            return point.proceed();
        }
        Object[] args = point.getArgs();
        if (args == null || args.length < 1) {
            return point.proceed();
        }
        Class<?> targetClass = tablePair.getKey();
        Table tableAnnotation = tablePair.getValue();
        String key = buildKeyFromArgs(args, targetClass.getSimpleName());
        if (StrUtil.isBlank(key)) {
            return point.proceed();
        }
        String selectAllKey = buildSelectAllKey(targetClass.getSimpleName());
        return executor.deleteByPrimaryKey(tableAnnotation, key, selectAllKey, new MethodDefaultDelegater<Integer>(point));
    }

    @Around(value = "execution(* com.devvv..mapper..*.selectByPrimaryKey(..)) && tablePoint()")
    public Object selectByPrimaryKey(ProceedingJoinPoint point) throws Throwable {
        Pair<Class<?>, Table> tablePair = findTableAnnotation(point.getTarget().getClass());
        if (tablePair == null || !tablePair.getValue().useTableCache()) {
            return point.proceed();
        }
        Object[] args = point.getArgs();
        if (args == null || args.length < 1) {
            return point.proceed();
        }
        Class<?> targetClass = tablePair.getKey();
        Table tableAnnotation = tablePair.getValue();
        String key = buildKeyFromArgs(args, targetClass.getSimpleName());
        if (StrUtil.isBlank(key)) {
            return point.proceed();
        }

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = method.getReturnType();
        return executor.selectByPrimaryKey(tableAnnotation, key, returnType, new MethodDefaultDelegater<Object>(point));
    }

    @Around(value = "@annotation(com.devvv.commons.core.config.cache.table.annotation.SelectAll) && tablePoint()")
    public <T> Object selectAll(ProceedingJoinPoint point) throws Throwable {
        Pair<Class<?>, Table> tablePair = findTableAnnotation(point.getTarget().getClass());
        if (tablePair == null || !tablePair.getValue().useTableCache()) {
            return point.proceed();
        }
        // 我们要求 在使用 @SelectAll 时，必须在Table注解上也标记 启用了SelectAll
        // 以便在更新数据时清理 所有缓存
        if (!tablePair.getValue().useSelectAll()) {
            log.warn("[表缓存]- @SelectAll 注解使用不当，必须在Table注解上也标记 启用了useSelectAll");
            return point.proceed();
        }

        Class<?> targetClass = tablePair.getKey();
        Table tableAnnotation = tablePair.getValue();
        String selectAllKey = buildSelectAllKey(targetClass.getSimpleName());

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 获取返回值类型
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                Class<?> actualClass = (Class<?>) actualTypeArguments[0];   // 实际的泛型类型
                return executor.selectAll(tableAnnotation, selectAllKey, actualClass, new MethodDefaultDelegater<List<Object>>(point));
            }
        }
        // 如果不是泛型，降级处理
        return point.proceed();
    }


    private String buildSelectAllKey(String targetClass) {
        return StrUtil.format("{}:#:_ALL_", targetClass);
    }
    private String buildKeyFromBean(Table tableAnnotation, Object bean, String targetClass) {
        try {
            return Arrays.stream(tableAnnotation.primaryKey())
                    .map(k -> BeanUtil.getProperty(bean, k))
                    .reduce((a, b) -> a + "_" + b)
                    .map(s -> targetClass + ":" + s)
                    .orElse(null);
        } catch (Exception e) {
            log.error("[表缓存]- 构建缓存key失败！", e);
            return null;
        }
    }
    private String buildKeyFromArgs(Object[] args, String targetClass) {
        try {
            return Arrays.stream(args)
                    .reduce((a, b) -> a + "_" + b)
                    .map(s -> targetClass + ":" + s)
                    .orElse(null);
        } catch (Exception e) {
            log.error("[表缓存]- 构建缓存key失败！", e);
            return null;
        }
    }

    private final ConcurrentHashMap<Class<?>, Opt<Pair<Class<?>, Table>>> LOCAL_CACHE = new ConcurrentHashMap<>();
    private Pair<Class<?>, Table> findTableAnnotation(Class<?> targetClass) {
        Opt<Pair<Class<?>, Table>> opt = LOCAL_CACHE.get(targetClass);
        if (opt != null) {
            return opt.orElse(null);
        }
        // 1. 检查当前类是否标注了 @Table
        Table annotation = targetClass.getAnnotation(Table.class);
        if (annotation != null) {
            Pair<Class<?>, Table> result = Pair.of(targetClass, annotation);
            LOCAL_CACHE.put(targetClass, Opt.of(result));
            return result;
        }

        // 2. 获取当前类实现的所有接口
        Class<?>[] interfaces = targetClass.getInterfaces();
        for (Class<?> iface : interfaces) {
            // 递归检查接口是否标注了 @Table
            annotation = iface.getAnnotation(Table.class);
            if (annotation != null) {
                Pair<Class<?>, Table> result = Pair.of(iface, annotation);
                LOCAL_CACHE.put(targetClass, Opt.of(result));
                return result;
            }
        }
        // // 3. 如果当前类有父类，也需要检查父类 (通常 Mapper 接口不会有父类)
        // Class<?> superClass = targetClass.getSuperclass();
        // if (superClass != null && superClass != Object.class) {
        //     annotation = findTableAnnotation(superClass);
        //     if (annotation != null) {
        //         LOCAL_CACHE.put(targetClass, Opt.of(annotation));
        //         return annotation;
        //     }
        // }
        LOCAL_CACHE.put(targetClass, Opt.empty());
        return null; // 没有找到 @Table 注解
    }
}

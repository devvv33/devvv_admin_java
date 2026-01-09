package com.devvv.commons.core.config.datasource.transaction.busi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Create by WangSJ on 2023/07/05
 */
@Slf4j
public class BusiTransactionResourceManager {

    private static final ThreadLocal<LinkedHashMap<Object, BusiTransactionResource>> resources = new NamedThreadLocal<>("TransactionResources");


    public static boolean inTransaction() {
        return resources.get() != null;
    }

    public static void initTransactionResource() {
        if (inTransaction()) {
            throw new IllegalStateException("Cannot activate Transaction resource - already active");
        }
        resources.set(new LinkedHashMap<>());
    }

    public static Map<Object, BusiTransactionResource> getResourceMap() {
        if (!inTransaction()) {
            throw new IllegalStateException("The Transaction resource is not active");
        }
        Map<Object, BusiTransactionResource> map = resources.get();
        return Collections.unmodifiableMap(map);
    }

    public static boolean hasResource(Object key) {
        Object value = doGetResource(key);
        return (value != null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getResource(Object key) {
        return (T)doGetResource(key);
    }

    private static BusiTransactionResource doGetResource(Object actualKey) {
        Map<Object, BusiTransactionResource> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(actualKey);
    }

    public static void bindResource(Object key, BusiTransactionResource value){
        Assert.notNull(value, "Value must not be null");
        LinkedHashMap<Object, BusiTransactionResource> map = resources.get();
        // set ThreadLocal Map if none found
        if (map == null) {
            throw new IllegalStateException("The Transaction resource is not active");
        }
        map.put(key, value);
        if (log.isTraceEnabled()) {
            log.trace("Bound value [" + value + "] for key [" + key + "] to thread [" + Thread.currentThread().getName() + "]");
        }
    }

    public static Object unbindResource(Object key){
        Map<Object, BusiTransactionResource> map = resources.get();
        if (map == null) {
            throw new IllegalStateException("The Transaction resource is not active");
        }
        Object value = map.remove(key);
        // Remove entire ThreadLocal if empty...
        if (map.isEmpty()) {
            resources.remove();
        }
        if (value != null && log.isTraceEnabled()) {
            log.trace("Removed value [" + value + "] for key [" + key + "] from thread [" + Thread.currentThread().getName() + "]");
        }
        return value;
    }

    public static void clear() {
        if (!inTransaction()) {
            throw new IllegalStateException("Cannot deactivate transaction resource - not active");
        }

        resources.remove();
        if (log.isTraceEnabled()) {
            log.trace("Transactional resource has been cleared.");
        }
    }
}

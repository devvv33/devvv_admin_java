package com.devvv.commons.common.utils;

import cn.hutool.core.lang.Pid;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Create by WangSJ on 2022/08/09
 */
public class CommonUtil {

    /**
     * 默认的字符串分隔符
     */
    public static final String DEFAULT_SEPARATOR = "[,;，。、； \r\n]";

    /**
     * 将字符串，按照 ,.，。 、 切割，并转成对应类型的集合
     * @return 不为null，里边不包含null元素
     */
    public static Set<Integer> splitIntegerSet(String str) {
        Set<Integer> set = new HashSet<>();
        converter(str, DEFAULT_SEPARATOR, Integer::parseInt, set);
        return set;
    }
    public static List<Integer> splitIntegerList(String str) {
        List<Integer> list = new ArrayList<>();
        converter(str, DEFAULT_SEPARATOR, Integer::parseInt, list);
        return list;
    }
    public static Set<Long> splitLongSet(String str) {
        Set<Long> set = new HashSet<>();
        converter(str, DEFAULT_SEPARATOR, Long::parseLong, set);
        return set;
    }
    public static List<Long> splitLongList(String str) {
        List<Long> list = new ArrayList<>();
        converter(str, DEFAULT_SEPARATOR, Long::parseLong, list);
        return list;
    }
    public static List<Long> splitLongList(String str, String regex) {
        List<Long> list = new ArrayList<>();
        converter(str, regex, Long::parseLong, list);
        return list;
    }
    public static List<BigDecimal> splitDecimalList(String str) {
        List<BigDecimal> list = new ArrayList<>();
        converter(str, DEFAULT_SEPARATOR, BigDecimal::new, list);
        return list;
    }
    public static Set<BigDecimal> splitDecimalSet(String str) {
        Set<BigDecimal> set = new HashSet<>();
        converter(str, DEFAULT_SEPARATOR, BigDecimal::new, set);
        return set;
    }

    /**
     * 将字符串切分成集合
     * @param str           源字符串
     * @param regex         分隔符，支持正则 eg: [,\.，。 、]
     * @return 切分后的集合，不为null，不包含空字符串
     */
    public static List<String> splitStringList(String str, String regex) {
        List<String> list = new ArrayList<>();
        converter(str, regex, item -> StrUtil.isBlank(item) ? null : item.trim(), list);
        return list;
    }
    public static Set<String> splitStringSet(String str, String regex) {
        Set<String> set = new HashSet<>();
        converter(str, regex, item -> StrUtil.isBlank(item) ? null : item.trim(), set);
        return set;
    }
    public static List<String> splitStringList(String str) {
        return splitStringList(str, DEFAULT_SEPARATOR);
    }
    public static Set<String> splitStringSet(String str) {
        return splitStringSet(str, DEFAULT_SEPARATOR);
    }

    // 转换填充器
    private static <T> void converter(String str, String separator, Function<String, T> converter, Collection<T> collection) {
        if (StrUtil.isBlank(str)) {
            return;
        }
        String[] itemArr = str.split(separator);
        for (String item : itemArr) {
            if (StrUtil.isBlank(item)) {
                continue;
            }
            try {
                T result = converter.apply(item);
                if (result == null) {
                    continue;
                }
                collection.add(result);
            } catch (NumberFormatException ignore) {
                // 异常，掠过
            }
        }
    }


    /**
     * 将数字转为指定长度的字符串，长度不够前边补0
     */
    public static String formatNumber(int number, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("0");
        }
        return sb.append(number).substring(sb.length() - length);
    }
    public static String getNextNumberStr(String numberStr, int length) {
        try {
            int number = Integer.parseInt(numberStr);
            return formatNumber(number + 1, length);
        } catch (Exception e) {
            throw new RuntimeException("数字转化失败：" + numberStr);
        }
    }

    public static String listToString(List list) {
        if (list != null && !list.isEmpty()) {
            StringBuilder result = new StringBuilder();
            Iterator var2 = list.iterator();

            while (var2.hasNext()) {
                String string = var2.next().toString();
                result.append(string);
                result.append(',');
            }

            return result.substring(0, result.length() - 1);
        } else {
            return null;
        }
    }


    /**
     * 获取当前栈帧信息
     * 常用于打印输出
     */
    public static String getCurrentStackTrace() {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(Objects::nonNull)
                .forEach(stack -> sb.append("  ").append(stack.getClassName()).append(".").append(stack.getMethodName()).append("  ").append(stack.getLineNumber()).append("\n"));
        return sb.toString();
    }

    /**
     * 对象转Map，包含自己类对象和父类对象
     *
     * @param obj 要转的对象
     */
    public static Map<String, String> toStringMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return JSONObject.parseObject(JSONObject.toJSONString(obj), new TypeReference<Map<String, String>>(){});
    }
    public static Map<String, Object> toObjMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return JSONObject.parseObject(JSONObject.toJSONString(obj));
    }


    // 排除 字符串中包含utf8mb4的字符，防止数据库插入报错
    public static String removeUtf8mb4(String source) {
        if (source == null) {
            return null;
        }
        final int LAST_BMP = 0xFFFF;
        StringBuilder sb = new StringBuilder(source.length());
        for (int i = 0; i < source.length(); i++) {
            int codePoint = source.codePointAt(i);
            if (codePoint < LAST_BMP) {
                sb.appendCodePoint(codePoint);
            } else {
                i++;
            }
        }
        return sb.toString();
    }


    /**
     * 获取运行时程序信息
     */
    public static String getRuntimeMsg() {
        Runtime runtime = Runtime.getRuntime();
        int core = runtime.availableProcessors();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return StrUtil.format("PID:{}  CPU:{}  内存[Max:{} Total:{} Free:{}]", Pid.INSTANCE.get(), core, formatMemory(maxMemory), formatMemory(totalMemory), formatMemory(freeMemory));
    }
    private static String formatMemory(Long byteMemory) {
        return byteMemory / 1024 / 1024 + "M";
    }

}

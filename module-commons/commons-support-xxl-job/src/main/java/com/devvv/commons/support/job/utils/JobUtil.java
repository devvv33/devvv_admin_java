package com.devvv.commons.support.job.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Create by WangSJ on 2024/07/10
 */
public class JobUtil {

    /**
     * 根据定时任务分片索引、分片数等信息计算当前分片可处理的缓存分片索引信息
     *
     * @param jobShardIndex 定时任务分片索引
     * @param jobShardTotal 定时任务分片总数
     * @param totalShardNum  任务总分片数
     * @return
     */
    public static List<Integer> getSharedIndexes(int jobShardIndex, int jobShardTotal, int totalShardNum) {
        if (jobShardIndex > jobShardTotal - 1) {
            throw new IllegalArgumentException("定时任务分片索引错误");
        }
        // 任务执行者多于任务分片数，靠后的执行者无需处理
        if (jobShardIndex > totalShardNum - 1) {
            return Collections.emptyList();
        }
        // 任务执行者总数，完全覆盖任务分片数，每人领取1个分片（自己对应的那片）
        if (jobShardTotal >= totalShardNum) {
            return Collections.singletonList(jobShardIndex);
        }

        // 将任务分片，拆分成jobTotal份，每个执行者分配一部分
        List<Integer> ownerShardIndexes = new ArrayList<>();
        for (int i = 0; i < totalShardNum; i++) {
            int shardIndex = i % jobShardTotal;
            if (shardIndex == jobShardIndex) {
                ownerShardIndexes.add(i);
            }
        }
        return ownerShardIndexes;
    }
}

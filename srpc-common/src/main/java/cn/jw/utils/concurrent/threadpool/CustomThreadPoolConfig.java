package cn.jw.utils.concurrent.threadpool;

import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Data
public class CustomThreadPoolConfig {
    public static final int DEFAULT_CORE_POOL_SIZE = 10;
    public static final int DEFAULT_MAXI_MUN_POOL_SIZE = 100;
    public static final int DEFAULT_KEEP_ALIVE_TIME = 1;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    public static final int DEFAULT_BLOCK_QUEUE_SIZE = 100;

    public int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    public int maximumPoolSize = DEFAULT_MAXI_MUN_POOL_SIZE;
    public long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    public TimeUnit unit = DEFAULT_TIME_UNIT;

    public BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(DEFAULT_BLOCK_QUEUE_SIZE);
}

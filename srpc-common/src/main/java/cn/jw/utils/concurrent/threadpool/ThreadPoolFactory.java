package cn.jw.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.deploy.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 *  线程池的管理
 */
@Slf4j
public class ThreadPoolFactory {

    private ThreadPoolFactory() {}

    private static final Map<String, ExecutorService> THREAD_POOL = new ConcurrentHashMap<>();

    /**
     *  线程工厂的创建
     * @param threadFactoryPrefix 前缀名
     * @param daemon 是否守护线程
     * @return
     */
    public static ThreadFactory buildThreadFactory(String threadFactoryPrefix, Boolean daemon) {
        if (threadFactoryPrefix != null && threadFactoryPrefix.length() > 0) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadFactoryPrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadFactoryPrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     *  创建线程池
     * @param customThreadPoolConfig 线程池的配置项
     * @param threadFactoryPrefix 线程工厂的配置项
     * @param daemon 是否是守护线
     * @return
     */
    public static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadFactoryPrefix, Boolean daemon) {
        ThreadFactory threadFactory = buildThreadFactory(threadFactoryPrefix, daemon);
        return new ThreadPoolExecutor(
                customThreadPoolConfig.getCorePoolSize(),
                customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getUnit(),
                customThreadPoolConfig.getWorkQueue(),
                threadFactory
                );
    }

    /**
     *  关闭所有的线程池
     */
    public static void shutdownAllThreadPool() {
        log.info("关闭所有线程池。");
        for (Map.Entry<String, ExecutorService> entry : THREAD_POOL.entrySet()) {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭线程池，这个池子的信息：【{}】, [{}]", executorService, executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("在主线程等待线程池关闭的时候出现问题。");
                executorService.shutdown();
            }
        }
    }

    /**
     *  重载方法创建线程池
     * @param threadPoolPrefix
     * @return
     */
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadPoolPrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadPoolPrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadPoolPrefix, boolean daemon) {
        ExecutorService executorService = THREAD_POOL.computeIfAbsent(threadPoolPrefix, new Function<String, ExecutorService>() {
            @Override
            public ExecutorService apply(String s) {
                return createThreadPool(customThreadPoolConfig, threadPoolPrefix, daemon);
            }
        });
        if (executorService.isShutdown() || executorService.isTerminated()) {
            THREAD_POOL.remove(threadPoolPrefix);
            executorService = createThreadPool(customThreadPoolConfig, threadPoolPrefix, daemon);
            THREAD_POOL.put(threadPoolPrefix, executorService);
        }
        return executorService;
    }


}

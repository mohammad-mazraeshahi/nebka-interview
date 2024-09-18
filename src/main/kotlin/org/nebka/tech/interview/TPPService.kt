package org.nebka.tech.interview

import org.nebka.tech.interview.config.properties.ApplicationProperties
import org.nebka.tech.interview.config.PoolTaskDecorator
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.util.concurrent.Executor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@Service
class TPPService(
    private val properties: ApplicationProperties
) {
    private val fixedThreadPools = mutableMapOf<String, Executor>()

    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()

        threadPoolTaskExecutor.setTaskDecorator(PoolTaskDecorator())
        threadPoolTaskExecutor.setThreadFactory(threadFactory(properties.poolName))
        threadPoolTaskExecutor.setCorePoolSize(properties.poolSize)
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        threadPoolTaskExecutor.setAwaitTerminationSeconds(properties.awaitTime)
        threadPoolTaskExecutor.initialize()

        registerFixedThreadPool(properties.poolName, threadPoolTaskExecutor)

        return threadPoolTaskExecutor
    }

    fun threadFactory(name: String): ThreadFactory {
        return object : ThreadFactory {
            private val threadNumber = AtomicInteger(1)
            private val namePrefix = "EqSolver-$name-"
            override fun newThread(runnable: Runnable): Thread {
                val thread = Thread(runnable, namePrefix + threadNumber.getAndIncrement())
                if (thread.isDaemon) thread.setDaemon(false)
                if (thread.priority != Thread.NORM_PRIORITY) thread.setPriority(Thread.NORM_PRIORITY)
                return thread
            }
        }
    }

    fun registerFixedThreadPool(name: String, pool: Executor) {
        if (name in fixedThreadPools)
            registerFixedThreadPool("${name}_", pool)
        else
            fixedThreadPools[name] = pool
    }
}
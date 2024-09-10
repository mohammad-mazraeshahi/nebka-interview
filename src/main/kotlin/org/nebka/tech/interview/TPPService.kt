package org.nebka.tech.interview

import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@Service
class TPPService {
    private val fixedThreadPools = mutableMapOf<String, ExecutorService>()

    fun getFixedThreadPool(parallelism: Int, name: String): ExecutorService {
        val pool = Executors.newFixedThreadPool(
                /* nThreads = */ parallelism,
                /* threadFactory = */ object : ThreadFactory {
            private val threadNumber = AtomicInteger(1)
            private val namePrefix = "EqSolver-$name-"
            override fun newThread(runnable: Runnable): Thread {
                val thread = Thread(runnable, namePrefix + threadNumber.getAndIncrement())
                if (thread.isDaemon) thread.setDaemon(false)
                if (thread.priority != Thread.NORM_PRIORITY) thread.setPriority(Thread.NORM_PRIORITY)
                return thread
            }
        })
        registerFixedThreadPool(name, pool)
        return pool
    }

    fun registerFixedThreadPool(name: String, pool: ExecutorService) {
        if (name in fixedThreadPools)
            registerFixedThreadPool("${name}_", pool)
        else
            fixedThreadPools[name] = pool
    }
}
package org.nebka.tech.interview.config

import io.opentelemetry.context.Context
import org.springframework.core.task.TaskDecorator
import org.springframework.lang.NonNull

class PoolTaskDecorator : TaskDecorator {
    override fun decorate(@NonNull task: Runnable): Runnable {
        Context.current() //TODO: you can do somethings

        return Runnable {
            try {
                task.run()
            } finally {
            }
        }
    }
}
package org.nebka.tech.interview

import io.micrometer.core.annotation.Timed
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/solver")
class EquationController(
        private val simulator: EquationSimulator
) {
    companion object {
        // TODO: temporary, provide a better security
        private const val CLI_SECRET = "cy9tYXZlbi9dCltXQVJOSU5HXSBGYWlsZWQgdG8gZG93bmxvYWQgdG9tY2F0LWVtYmVkLXdlYnNvY2tldC0xMC4xLjI4LmphciBbaHR0cHM6Ly9naXRsYWIucm5nLWFkbS5jb20vYXBpL3Y0L3Byb2plY3RzLzE2L3BhY2thZ2VzL21hdmVuL10KCg=="
    }

    @PostMapping
    @Timed(value = "response_time", histogram = true, percentiles = [0.5, 0.75, 0.9, 0.95])
    fun solveANewEq(secret: String, @RequestBody request: EquationRequest): EquationResponse? {
        if (secret != CLI_SECRET)
            return null

        return simulator.gimmeAnswer(request)
    }

}
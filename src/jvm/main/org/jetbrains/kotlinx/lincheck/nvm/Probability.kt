/*-
 * #%L
 * Lincheck
 * %%
 * Copyright (C) 2019 - 2020 JetBrains s.r.o.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.jetbrains.kotlinx.lincheck.nvm

import org.jetbrains.kotlinx.lincheck.runner.RecoverableStateContainer
import java.lang.Integer.max
import kotlin.random.Random

object Probability {
    private val random = Random(42)

    @Volatile
    var totalPossibleCrashes = 0
        set(value) {
            field = value
            updateSingleCrashProbability()
        }

    @Volatile
    var totalActors = 0
        set(value) {
            field = value
            updateSingleCrashProbability()
        }

    @Volatile
    var expectedCrashes = 10
        set(value) {
            field = value
            updateSingleCrashProbability()
        }

    @Volatile
    private var singleCrashProbability = 0.0f

    private fun updateSingleCrashProbability() {
        singleCrashProbability = expectedCrashes / max(1, totalPossibleCrashes * totalActors).toFloat()
    }

    private const val RANDOM_FLUSH_PROBABILITY = 0.2f
    private const val RANDOM_SYSTEM_CRASH_PROBABILITY = 0.3f

    fun shouldFlush() = bernoulli(RANDOM_FLUSH_PROBABILITY)
    fun shouldCrash() = RecoverableStateContainer.crashesEnabled
        && (RecoverableStateContainer.crashesCount() <= expectedCrashes)
        && bernoulli(singleCrashProbability)

    fun shouldSystemCrash() = bernoulli(RANDOM_SYSTEM_CRASH_PROBABILITY)

    private fun bernoulli(probability: Float) = random.nextFloat() < probability
}
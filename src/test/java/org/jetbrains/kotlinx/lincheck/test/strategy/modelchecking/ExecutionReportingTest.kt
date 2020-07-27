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
package org.jetbrains.kotlinx.lincheck.test.strategy.modelchecking

import kotlinx.coroutines.delay
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.appendFailure
import org.jetbrains.kotlinx.lincheck.checkImpl
import org.jetbrains.kotlinx.lincheck.strategy.managed.forClasses
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Test

class ExecutionReportingTest : VerifierState() {
    @Volatile
    var a = 0
    @Volatile
    var b = 0
    @Volatile
    var canEnterForbiddenSection = false

    @Operation
    fun operation1(): Int {
        if (canEnterForbiddenSection) {
            return 1
        }
        return 0
    }

    @Operation
    suspend fun operation2() {
        b++
        delay(0)
        treatedAsAtomic()
        uselessIncrements(2)
        intermediateMethod()
    }

    private fun intermediateMethod() {
        resetFlag()
    }

    private fun treatedAsAtomic() {}

    @Synchronized
    private fun resetFlag() {
        canEnterForbiddenSection = true
        canEnterForbiddenSection = false
    }

    private fun uselessIncrements(count: Int): Boolean {
        repeat(count) {
            b++
        }
        return false
    }

    private fun ignored() {
        b++
        b++
    }

    @Test
    fun test() {
        val options = ModelCheckingOptions()
                .actorsAfter(0)
                .actorsBefore(0)
                .actorsPerThread(1)
                .addGuarantee(forClasses(ExecutionReportingTest::class.java.name).methods("treatedAsAtomic").treatAsAtomic())
                .addGuarantee(forClasses(ExecutionReportingTest::class.java.name).methods("ignored").ignore())
        val failure = options.checkImpl(this::class.java)
        check(failure != null) { "test should fail" }
        val log = StringBuilder().appendFailure(failure).toString()
        check("operation1" in log)
        check("canEnterForbiddenSection.WRITE(true) at ExecutionReportingTest.resetFlag" in log)
        check("canEnterForbiddenSection.WRITE(false) at ExecutionReportingTest.resetFlag" in log)
        check("b.READ: 0 at ExecutionReportingTest.operation2" in log)
        check("b.WRITE(1) at ExecutionReportingTest.operation2" in log)
        check("MONITOR ENTER at ExecutionReportingTest.resetFlag" in log)
        check("MONITOR EXIT at ExecutionReportingTest.resetFlag" in log)
        check("uselessIncrements(2): false at" in log) { "increments in uselessIncrements method should be compressed" }
        check("treatedAsAtomic() at" in log) { "treated as atomic methods should be reported" }
        check("ignored" !in log) { "ignored methods should not be present in log" }
        check("label" !in log) { "suspend state machine related fields should not be reported" }
        check("L$0" !in log) { "suspend state machine related fields should not be reported" }
    }

    override fun extractState() = "$a $b $canEnterForbiddenSection"
}
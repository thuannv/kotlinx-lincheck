/*
 * Lincheck
 *
 * Copyright (C) 2019 - 2021 JetBrains s.r.o.
 *
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
 * <http://www.gnu.org/licenses/lgpl-3.0.html>
 */

package org.jetbrains.kotlinx.lincheck.runner

import org.jetbrains.kotlinx.lincheck.execution.*

expect class ThreadDump

/**
 * Represents results for invocations, see [Runner.run].
 */
open class InvocationResult

/**
 * The invocation completed successfully, the output [results] are provided.
 */
class CompletedInvocationResult(
    val results: ExecutionResult
) : InvocationResult()

/**
 * The invocation has completed with an unexpected exception.
 */
class UnexpectedExceptionInvocationResult(
    val exception: Throwable
) : InvocationResult()

/**
 * The invocation successfully completed, but the
 * [validation function][org.jetbrains.kotlinx.lincheck.annotations.Validate]
 * check failed.
 */
class ValidationFailureInvocationResult(
    val scenario: ExecutionScenario,
    val functionName: String,
    val exception: Throwable
) : InvocationResult()

/**
 * Obstruction freedom check is requested,
 * but an invocation that hangs has been found.
 */
class ObstructionFreedomViolationInvocationResult(
    val reason: String
) : InvocationResult()

/**
 * Indicates that the invocation has run into deadlock or livelock.
 */
class DeadlockInvocationResult(
    val threadDump: ThreadDump
) : InvocationResult()
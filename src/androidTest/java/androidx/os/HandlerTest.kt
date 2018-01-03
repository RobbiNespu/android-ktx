/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.os

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class HandlerTest {
    private val handlerThread = HandlerThread("handler-test")
    private lateinit var handler: Handler

    @Before fun before() {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    @After fun after() {
        handlerThread.quitSafely()
    }

    @Test fun postDelayedWithToken() {
        var called = 0
        val runnable = Runnable { called++ }
        handler.postDelayed(runnable, Any(), 10)

        handler.await(20, MILLISECONDS)
        assertEquals(1, called)
    }

    @Test fun postDelayedWithTokenCancelWithToken() {
        var called = 0
        val runnable = Runnable { called++ }
        val token = Any()
        handler.postDelayed(runnable, token, 10)
        handler.removeCallbacksAndMessages(token)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    @Test fun postDelayedLambdaMillis() {
        var called = 0
        handler.postDelayed(10) {
            called++
        }

        handler.await(20, MILLISECONDS)
        assertEquals(1, called)
    }

    @Test fun postDelayedLambdaMillisRemoved() {
        var called = 0
        val runnable = handler.postDelayed(10) {
            called++
        }
        handler.removeCallbacks(runnable)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    @Test fun postDelayedLambdaTimeUnit() {
        var called = 0
        handler.postDelayed(10, MILLISECONDS) {
            called++
        }

        handler.await(20, MILLISECONDS)
        assertEquals(1, called)
    }

    @Test fun postDelayedLambdaTimeUnitRemoved() {
        var called = 0
        val runnable = handler.postDelayed(10, MILLISECONDS) {
            called++
        }
        handler.removeCallbacks(runnable)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    @Test fun postDelayedLambdaDuration() {
        var called = 0
        handler.postDelayed(Duration.ofMillis(10)) {
            called++
        }

        handler.await(20, MILLISECONDS)
        assertEquals(1, called)
    }

    @Test fun postDelayedLambdaDurationRemoved() {
        var called = 0
        val runnable = handler.postDelayed(Duration.ofMillis(10)) {
            called++
        }
        handler.removeCallbacks(runnable)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    @Test fun postAtTimeLambda() {
        var called = 0
        handler.postAtTime(SystemClock.uptimeMillis() + 10) {
            called++
        }

        handler.await(20, MILLISECONDS)
        assertEquals(1, called)
    }

    @Test fun postAtTimeLambdaRemoved() {
        var called = 0
        val runnable = handler.postAtTime(SystemClock.uptimeMillis() + 10) {
            called++
        }
        handler.removeCallbacks(runnable)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    @Test fun postAtTimeLambdaWithTokenRuns() {
        val token = Any()
        var called = 0
        handler.postAtTime(SystemClock.uptimeMillis() + 10, token) {
            called++
        }

        handler.await(20, MILLISECONDS)
        assertEquals(1, called)
    }

    @Test fun postAtTimeLambdaWithTokenCancelWithToken() {
        // This test uses the token to cancel the runnable as it's the only way we have to verify
        // that the Runnable was actually posted with the token.

        val token = Any()
        var called = 0
        handler.postAtTime(SystemClock.uptimeMillis() + 10, token) {
            called++
        }
        handler.removeCallbacksAndMessages(token)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    @Test fun postAtTimeLambdaWithTokenRemoved() {
        var called = 0
        val runnable = handler.postAtTime(SystemClock.uptimeMillis() + 10) {
            called++
        }
        handler.removeCallbacks(runnable)

        handler.await(20, MILLISECONDS)
        assertEquals(0, called)
    }

    private fun Handler.await(amount: Long, unit: TimeUnit) {
        val latch = CountDownLatch(1)
        postDelayed(latch::countDown, unit.toMillis(amount))

        // Wait up to 1s longer than desired to account for time skew.
        val wait = unit.toMillis(amount) + SECONDS.toMillis(1)
        assertTrue(latch.await(wait, MILLISECONDS))
    }
}

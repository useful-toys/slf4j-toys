/*
 * Copyright 2026 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usefultoys.slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parameterized unit tests for {@link CallerStackTraceThrowable#removeInternalFrames(StackTraceElement[])}.
 * <p>
 * These tests validate the stack trace filtering logic by providing synthetic stack traces
 * and verifying the expected output. Tests are organized into categories:
 * <ul>
 *   <li><b>Category A:</b> Empty/Minimal stack traces</li>
 *   <li><b>Category B:</b> User code only (no library frames)</li>
 *   <li><b>Category C:</b> Library + User code combinations</li>
 *   <li><b>Category D:</b> Reflection/Mockito frame filtering</li>
 *   <li><b>Category E:</b> Test class detection (ending in "Test" or "Tests")</li>
 *   <li><b>Category F:</b> Special cases (CallerStackTraceThrowable direct calls, subpackages, all-library stacks)</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("CallerStackTraceThrowable.removeInternalFrames(): Parameterized Tests")
public class CallerStackTraceThrowableRemoveInternalFramesTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideRemoveFramesTestCases")
    @DisplayName("should remove internal frames correctly for various stack trace scenarios")
    void shouldRemoveInternalFramesCorrectly(
            final String scenario,
            final StackTraceElement[] input,
            final StackTraceElement[] expectedOutput) {

        // Given: a CallerStackTraceThrowable instance and input stack trace (from scenario)

        // When: removeInternalFrames is called
        final CallerStackTraceThrowable throwable = new CallerStackTraceThrowable();
        final StackTraceElement[] result = throwable.removeInternalFrames(input);

        // Then: result should match expected output
        assertEquals(expectedOutput.length, result.length,
                scenario + ": Stack trace length should match");
        assertArrayEquals(expectedOutput, result,
                scenario + ": Stack trace content should match exactly");
    }

    /**
     * Provides test cases for removeInternalFrames parameterized tests.
     * Each test case consists of:
     * <ul>
     *   <li>Scenario description</li>
     *   <li>Input stack trace (synthetic)</li>
     *   <li>Expected output stack trace after filtering</li>
     * </ul>
     *
     * @return stream of test case arguments
     */
    private static Stream<Arguments> provideRemoveFramesTestCases() {
        return Stream.of(
                // ========================================
                // Category A: Empty/Minimal Stack Traces
                // ========================================

                Arguments.of(
                        "A1: Empty stack trace (0 frames)",
                        new StackTraceElement[]{},
                        CallerStackTraceThrowable.EMPTY_STACK_TRACE
                ),

                Arguments.of(
                        "A2: Stack with only 1 frame (less than minimum 3)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace")
                        },
                        CallerStackTraceThrowable.EMPTY_STACK_TRACE
                ),

                Arguments.of(
                        "A3: Stack with only 2 frames (less than minimum 3)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace")
                        },
                        CallerStackTraceThrowable.EMPTY_STACK_TRACE
                ),

                Arguments.of(
                        "A4: Stack with exactly 3 system frames (no user code)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>")
                        },
                        CallerStackTraceThrowable.EMPTY_STACK_TRACE
                ),

                // ========================================
                // Category B: User Code Only
                // ========================================

                Arguments.of(
                        "B1: User code only (no library frames)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("com.example.UserClass", "userMethod"),
                                frame("com.example.Main", "main")
                        },
                        new StackTraceElement[]{
                                frame("com.example.UserClass", "userMethod"),
                                frame("com.example.Main", "main")
                        }
                ),

                Arguments.of(
                        "B2: User code only with single frame",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "B3: User code only with multiple frames",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("com.example.Service", "processRequest"),
                                frame("com.example.Controller", "handleRequest"),
                                frame("com.acme.Application", "execute"),
                                frame("com.acme.Main", "main")
                        },
                        new StackTraceElement[]{
                                frame("com.example.Service", "processRequest"),
                                frame("com.example.Controller", "handleRequest"),
                                frame("com.acme.Application", "execute"),
                                frame("com.acme.Main", "main")
                        }
                ),

                // ========================================
                // Category C: Library + User Code
                // ========================================

                Arguments.of(
                        "C1: Library entry point + user code",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.Service", "processRequest"),
                                frame("com.example.Main", "main")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.Service", "processRequest"),
                                frame("com.example.Main", "main")
                        }
                ),

                Arguments.of(
                        "C2: Multiple library frames + user code (nested library calls)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.internal.MeterImpl", "doStart"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.Service", "processRequest"),
                                frame("com.example.Main", "main")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.Service", "processRequest"),
                                frame("com.example.Main", "main")
                        }
                ),

                Arguments.of(
                        "C3: Deep library nesting + user code",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.internal.ValidationHelper", "validate"),
                                frame("org.usefultoys.slf4j.internal.MeterImpl", "doStart"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.Service", "processRequest")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.Service", "processRequest")
                        }
                ),

                Arguments.of(
                        "C4: Library subpackage (watcher) + user code",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.watcher.Watcher", "watch"),
                                frame("com.example.Monitor", "monitorResource")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.watcher.Watcher", "watch"),
                                frame("com.example.Monitor", "monitorResource")
                        }
                ),

                Arguments.of(
                        "C5: Library subpackage (reporter) + user code",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.reporter.Reporter", "report"),
                                frame("com.example.Diagnostics", "generateReport")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.reporter.Reporter", "report"),
                                frame("com.example.Diagnostics", "generateReport")
                        }
                ),

                // ========================================
                // Category D: Reflection/Mockito Filtering
                // ========================================

                Arguments.of(
                        "D1: Mockito frames filtered out",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.mockito.internal.creation.bytebuddy.MockMethodInterceptor", "doIntercept"),
                                frame("org.mockito.internal.handler.MockHandlerImpl", "handle"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "D2: sun.reflect frames filtered out",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("sun.reflect.NativeMethodAccessorImpl", "invoke"),
                                frame("sun.reflect.DelegatingMethodAccessorImpl", "invoke"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "D3: java.lang.invoke frames filtered out",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("java.lang.invoke.MethodHandle", "invoke"),
                                frame("java.lang.invoke.LambdaForm", "interpretWithArguments"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "D4: java.lang.reflect frames filtered out",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("java.lang.reflect.Method", "invoke"),
                                frame("java.lang.reflect.Constructor", "newInstance"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "D5: Mixed reflection + Mockito frames filtered out",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.mockito.internal.handler.MockHandlerImpl", "handle"),
                                frame("sun.reflect.NativeMethodAccessorImpl", "invoke"),
                                frame("java.lang.invoke.MethodHandle", "invoke"),
                                frame("java.lang.reflect.Method", "invoke"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.Meter", "start"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                // ========================================
                // Category E: Test Class Detection
                // ========================================

                Arguments.of(
                        "E1: Test class ending in 'Test' stops filtering",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.meter.MeterTest", "shouldStartMeter"),
                                frame("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.meter.MeterTest", "shouldStartMeter"),
                                frame("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute")
                        }
                ),

                Arguments.of(
                        "E2: Test class ending in 'Tests' stops filtering",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.watcher.WatcherTests", "shouldWatchResource"),
                                frame("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.watcher.WatcherTests", "shouldWatchResource"),
                                frame("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute")
                        }
                ),

                Arguments.of(
                        "E3: Test class with Mockito + reflection frames",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.mockito.internal.creation.bytebuddy.MockMethodInterceptor", "doIntercept"),
                                frame("sun.reflect.NativeMethodAccessorImpl", "invoke"),
                                frame("org.usefultoys.slf4j.meter.MeterTest", "shouldRejectInvalidState"),
                                frame("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute")
                        },
                        new StackTraceElement[]{
                                frame("sun.reflect.NativeMethodAccessorImpl", "invoke"),
                                frame("org.usefultoys.slf4j.meter.MeterTest", "shouldRejectInvalidState"),
                                frame("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute")
                        }
                ),

                // ========================================
                // Category F: Special Cases
                // ========================================

                Arguments.of(
                        "F1: CallerStackTraceThrowable called directly from user code",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("com.example.UserClass", "userMethod"),
                                frame("com.example.Main", "main")
                        },
                        new StackTraceElement[]{
                                frame("com.example.UserClass", "userMethod"),
                                frame("com.example.Main", "main")
                        }
                ),

                Arguments.of(
                        "F2: All frames are library frames (no user code)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.internal.ValidationHelper", "validate"),
                                frame("org.usefultoys.slf4j.internal.MeterImpl", "doStart"),
                                frame("org.usefultoys.slf4j.meter.Meter", "start")
                        },
                        CallerStackTraceThrowable.EMPTY_STACK_TRACE
                ),

                Arguments.of(
                        "F3: Extended library subpackage",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.extended.CustomFeature", "customMethod"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.extended.CustomFeature", "customMethod"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "F4: LibraryHelper nested calls (similar to existing integration tests)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.usefultoys.slf4j.LibraryHelper", "innerMethod"),
                                frame("org.usefultoys.slf4j.LibraryHelper", "middleMethod"),
                                frame("org.usefultoys.slf4j.LibraryHelper", "outerMethod"),
                                frame("com.example.UserClass", "userMethod")
                        },
                        new StackTraceElement[]{
                                frame("org.usefultoys.slf4j.LibraryHelper", "outerMethod"),
                                frame("com.example.UserClass", "userMethod")
                        }
                ),

                Arguments.of(
                        "F5: Only reflection frames after system frames (no library or user code)",
                        new StackTraceElement[]{
                                frame("java.lang.Thread", "getStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "fillInStackTrace"),
                                frame("org.usefultoys.slf4j.CallerStackTraceThrowable", "<init>"),
                                frame("org.mockito.internal.creation.bytebuddy.MockMethodInterceptor", "doIntercept"),
                                frame("sun.reflect.NativeMethodAccessorImpl", "invoke"),
                                frame("java.lang.invoke.MethodHandle", "invoke")
                        },
                        CallerStackTraceThrowable.EMPTY_STACK_TRACE
                )
        );
    }

    /**
     * Helper method to create a synthetic StackTraceElement with minimal boilerplate.
     *
     * @param className the fully qualified class name
     * @param methodName the method name
     * @return a StackTraceElement with the given class and method, file derived from class name, line 1
     */
    private static StackTraceElement frame(final String className, final String methodName) {
        final String fileName = className.substring(className.lastIndexOf('.') + 1) + ".java";
        return new StackTraceElement(className, methodName, fileName, 1);
    }
}

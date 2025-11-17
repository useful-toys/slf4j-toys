/*
 * Copyright 2025 Daniel Felix Ferber
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
package org.usefultoys.slf4j.meter;

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeterAnalysisTest {

    // Classe de cenário de testes que implementa MeterAnalysis
    @Getter
    static class MeterAnalysisScenario implements MeterAnalysis {
        long lastCurrentTime;
        String category;
        String operation;
        String parent;
        long createTime;
        long startTime;
        long stopTime;
        long timeLimit;
        long currentIteration;
        String okPath;
        String rejectPath;
        String failPath;

        // Campos esperados para as asserções
        String expectedFullID;
        boolean expectedIsStarted;
        boolean expectedIsStopped;
        boolean expectedIsOK;
        boolean expectedIsReject;
        boolean expectedIsFail;
        String expectedPath;
        double expectedIterationsPerSecond;
        long expectedExecutionTime;
        long expectedWaitingTime;
        boolean expectedIsSlow;

        // Construtor completo para facilitar a criação de cenários
        public MeterAnalysisScenario(
                long lastCurrentTime,
                String category, String operation, String parent,
                long createTime, long startTime, long stopTime, long timeLimit,
                long currentIteration, String okPath, String rejectPath, String failPath,
                String expectedFullID, boolean expectedIsStarted, boolean expectedIsStopped,
                boolean expectedIsOK, boolean expectedIsReject, boolean expectedIsFail,
                String expectedPath, double expectedIterationsPerSecond, long expectedExecutionTime,
                long expectedWaitingTime, boolean expectedIsSlow) {
            this.lastCurrentTime = lastCurrentTime;
            this.category = category;
            this.operation = operation;
            this.parent = parent;
            this.createTime = createTime;
            this.startTime = startTime;
            this.stopTime = stopTime;
            this.timeLimit = timeLimit;
            this.currentIteration = currentIteration;
            this.okPath = okPath;
            this.rejectPath = rejectPath;
            this.failPath = failPath;
            this.expectedFullID = expectedFullID;
            this.expectedIsStarted = expectedIsStarted;
            this.expectedIsStopped = expectedIsStopped;
            this.expectedIsOK = expectedIsOK;
            this.expectedIsReject = expectedIsReject;
            this.expectedIsFail = expectedIsFail;
            this.expectedPath = expectedPath;
            this.expectedIterationsPerSecond = expectedIterationsPerSecond;
            this.expectedExecutionTime = expectedExecutionTime;
            this.expectedWaitingTime = expectedWaitingTime;
            this.expectedIsSlow = expectedIsSlow;
        }

        @Override
        public String toString() {
            return String.format("Scenario(category='%s', operation='%s')", category, operation);
        }
    }

    // Método que fornece os cenários de teste
    static Stream<MeterAnalysisScenario> provideMeterAnalysisScenarios() {
        return Stream.of(
                // Cenário 1: Operação em andamento, sem falha/rejeição
                new MeterAnalysisScenario(
                        1000L,
                        "cat1", "op1", null,
                        100L, 500L, 0L, 0L,
                        10L, "ok", null, null,
                        "cat1/op1#1", true, false,
                        false, false, false,
                        "ok", 10.0 / (1000.0 - 500.0) * 1_000_000_000, 500L,
                        400L, false
                ),
                // Cenário 2: Operação concluída com sucesso
                new MeterAnalysisScenario(
                        1500L,
                        "cat1", "op1", null,
                        100L, 500L, 1200L, 0L,
                        20L, "ok", null, null,
                        "cat1/op1#2", true, true,
                        true, false, false,
                        "ok", 20.0 / (1200.0 - 500.0) * 1_000_000_000, 700L,
                        400L, false
                ),
                // Cenário 3: Operação rejeitada
                new MeterAnalysisScenario(
                        2000L,
                        "cat2", "op2", null,
                        200L, 600L, 1800L, 0L,
                        5L, null, "rejected", null,
                        "cat2/op2#3", true, true,
                        false, true, false,
                        "rejected", 5.0 / (1800.0 - 600.0) * 1_000_000_000, 1200L,
                        400L, false
                ),
                // Cenário 4: Operação falhou
                new MeterAnalysisScenario(
                        2500L,
                        "cat3", "op3", null,
                        300L, 700L, 2200L, 0L,
                        1L, null, null, "failed",
                        "cat3/op3#4", true, true,
                        false, false, true,
                        "failed", 1.0 / (2200.0 - 700.0) * 1_000_000_000, 1500L,
                        400L, false
                ),
                // Cenário 5: Operação não iniciada
                new MeterAnalysisScenario(
                        500L,
                        "cat4", "op4", null,
                        100L, 0L, 0L, 0L,
                        0L, null, null, null,
                        "cat4/op4#5", false, false,
                        false, false, false,
                        null, 0.0, 0L,
                        400L, false
                ),
                // Cenário 6: Operação lenta
                new MeterAnalysisScenario(
                        3000L,
                        "cat5", "op5", null,
                        400L, 800L, 2800L, 1000L, // timeLimit = 1000, executionTime = 2000
                        10L, "ok", null, null,
                        "cat5/op5#6", true, true,
                        true, false, false,
                        "ok", 10.0 / (2800.0 - 800.0) * 1_000_000_000, 2000L,
                        400L, true
                ),
                // Cenário 7: Operação rápida (não lenta)
                new MeterAnalysisScenario(
                        3500L,
                        "cat6", "op6", null,
                        500L, 900L, 1200L, 1000L, // timeLimit = 1000, executionTime = 300
                        5L, "ok", null, null,
                        "cat6/op6#7", true, true,
                        true, false, false,
                        "ok", 5.0 / (1200.0 - 900.0) * 1_000_000_000, 300L,
                        400L, false
                ),
                // Cenário 8: getFullID sem operação
                new MeterAnalysisScenario(
                        3500L,
                        "cat7", null, null,
                        500L, 900L, 1200L, 1000L,
                        5L, "ok", null, null,
                        "cat7#8", true, true,
                        true, false, false,
                        "ok", 5.0 / (1200.0 - 900.0) * 1_000_000_000, 300L,
                        400L, false
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isStarted should correctly indicate if the operation has started")
    void testIsStarted(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsStarted, scenario.isStarted(),
                "isStarted should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isStopped should correctly indicate if the operation has stopped")
    void testIsStopped(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsStopped, scenario.isStopped(),
                "isStopped should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isOK should correctly indicate if the operation completed successfully")
    void testIsOK(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsOK, scenario.isOK(),
                "isOK should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isReject should correctly indicate if the operation was rejected")
    void testIsReject(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsReject, scenario.isReject(),
                "isReject should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isFail should correctly indicate if the operation failed")
    void testIsFail(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsFail, scenario.isFail(),
                "isFail should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getPath should return the correct outcome path")
    void testGetPath(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedPath, scenario.getPath(),
                "Path should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getIterationsPerSecond should calculate the correct iterations per second")
    void testGetIterationsPerSecond(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIterationsPerSecond, scenario.getIterationsPerSecond(), 0.000000001, // Delta para comparação de doubles
                "Iterations per second should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getExecutionTime should calculate the correct execution time")
    void testGetExecutionTime(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedExecutionTime, scenario.getExecutionTime(),
                "Execution time should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getWaitingTime should calculate the correct waiting time")
    void testGetWaitingTime(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedWaitingTime, scenario.getWaitingTime(),
                "Waiting time should match the expected value for scenario: " + scenario);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isSlow should correctly indicate if the operation is slow")
    void testIsSlow(MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsSlow, scenario.isSlow(),
                "isSlow should match the expected value for scenario: " + scenario);
    }
}

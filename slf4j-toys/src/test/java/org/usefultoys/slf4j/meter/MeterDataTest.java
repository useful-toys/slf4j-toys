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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Daniel
 */
public class MeterDataTest {

    @BeforeAll
    public static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetMeterConfigBeforeEach() {
        // Reinitialize MeterConfig to ensure clean configuration before each test
        MeterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetMeterConfigAfterAll() {
        // Reinitialize MeterConfig to ensure clean configuration for further tests
        MeterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @Test
    void testConstructorAndGetters() {
        // Create a map for context
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("key1", "value1");
        contextMap.put("key2", "value2");

        // Create MeterData with all fields populated
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                2, // time
                3, // heap_commited
                4, // heap_max
                5, // heap_used
                6, // nonHeap_commited
                7, // nonHeap_max
                8, // nonHeap_used
                9, // objectPendingFinalizationCount
                10, // classLoading_loaded
                11, // classLoading_total
                12, // classLoading_unloaded
                13, // compilationTime
                14, // garbageCollector_count
                15, // garbageCollector_time
                16, // runtime_usedMemory
                17, // runtime_maxMemory
                18, // runtime_totalMemory
                19.0, // systemLoad
                "categoryTest", // category
                "operationTest", // operation
                "parentTest", // parent
                "descriptionTest", // description
                20, // createTime
                21, // startTime
                22, // stopTime
                23, // timeLimit
                24, // currentIteration
                25, // expectedIterations
                "okPathTest", // okPath
                null, // rejectPath
                null, // failPath
                null, // failMessage
                contextMap // context
        );

        // Verify inherited fields from SystemData
        assertEquals("abc", meterData.getSessionUuid());
        assertEquals(1, meterData.getPosition());
        assertEquals(2L, meterData.getTime());
        assertEquals(3L, meterData.getHeap_commited());
        assertEquals(4L, meterData.getHeap_max());
        assertEquals(5L, meterData.getHeap_used());
        assertEquals(6L, meterData.getNonHeap_commited());
        assertEquals(7L, meterData.getNonHeap_max());
        assertEquals(8L, meterData.getNonHeap_used());
        assertEquals(9L, meterData.getObjectPendingFinalizationCount());
        assertEquals(10L, meterData.getClassLoading_loaded());
        assertEquals(11L, meterData.getClassLoading_total());
        assertEquals(12L, meterData.getClassLoading_unloaded());
        assertEquals(13L, meterData.getCompilationTime());
        assertEquals(14L, meterData.getGarbageCollector_count());
        assertEquals(15L, meterData.getGarbageCollector_time());
        assertEquals(16L, meterData.getRuntime_usedMemory());
        assertEquals(17L, meterData.getRuntime_maxMemory());
        assertEquals(18L, meterData.getRuntime_totalMemory());
        assertEquals(19.0, meterData.getSystemLoad());

        // Verify MeterData specific fields
        assertEquals("categoryTest", meterData.getCategory());
        assertEquals("operationTest", meterData.getOperation());
        assertEquals("parentTest", meterData.getParent());
        assertEquals("descriptionTest", meterData.getDescription());
        assertEquals(20L, meterData.getCreateTime());
        assertEquals(21L, meterData.getStartTime());
        assertEquals(22L, meterData.getStopTime());
        assertEquals(23L, meterData.getTimeLimit());
        assertEquals(24L, meterData.getCurrentIteration());
        assertEquals(25L, meterData.getExpectedIterations());
        assertEquals("okPathTest", meterData.getOkPath());
        assertNull(meterData.getRejectPath());
        assertNull(meterData.getFailPath());
        assertNull(meterData.getFailMessage());

        // Verify context map
        Map<String, String> returnedContext = meterData.getContext();
        assertEquals(2, returnedContext.size());
        assertEquals("value1", returnedContext.get("key1"));
        assertEquals("value2", returnedContext.get("key2"));

        // Verify status methods
        assertTrue(meterData.isStarted());
        assertTrue(meterData.isStopped());
        assertTrue(meterData.isOK());
        assertFalse(meterData.isReject());
        assertFalse(meterData.isFail());
    }

    @Test
    void testResetClearsFields() {
        // Create a map for context
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("key1", "value1");

        // Create MeterData with all fields populated
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                2, // time
                3, // heap_commited
                4, // heap_max
                5, // heap_used
                6, // nonHeap_commited
                7, // nonHeap_max
                8, // nonHeap_used
                9, // objectPendingFinalizationCount
                10, // classLoading_loaded
                11, // classLoading_total
                12, // classLoading_unloaded
                13, // compilationTime
                14, // garbageCollector_count
                15, // garbageCollector_time
                16, // runtime_usedMemory
                17, // runtime_maxMemory
                18, // runtime_totalMemory
                19.0, // systemLoad
                "categoryTest", // category
                "operationTest", // operation
                "parentTest", // parent
                "descriptionTest", // description
                20, // createTime
                21, // startTime
                22, // stopTime
                23, // timeLimit
                24, // currentIteration
                25, // expectedIterations
                "okPathTest", // okPath
                null, // rejectPath
                null, // failPath
                null, // failMessage
                contextMap // context
        );

        // Reset the meterData
        meterData.reset();

        // Verify inherited fields from SystemData are cleared
        assertNull(meterData.getSessionUuid());
        assertEquals(0L, meterData.getPosition());
        assertEquals(0L, meterData.getTime());
        assertEquals(0L, meterData.getHeap_commited());
        assertEquals(0L, meterData.getHeap_max());
        assertEquals(0L, meterData.getHeap_used());
        assertEquals(0L, meterData.getNonHeap_commited());
        assertEquals(0L, meterData.getNonHeap_max());
        assertEquals(0L, meterData.getNonHeap_used());
        assertEquals(0L, meterData.getObjectPendingFinalizationCount());
        assertEquals(0L, meterData.getClassLoading_loaded());
        assertEquals(0L, meterData.getClassLoading_total());
        assertEquals(0L, meterData.getClassLoading_unloaded());
        assertEquals(0L, meterData.getCompilationTime());
        assertEquals(0L, meterData.getGarbageCollector_count());
        assertEquals(0L, meterData.getGarbageCollector_time());
        assertEquals(0L, meterData.getRuntime_usedMemory());
        assertEquals(0L, meterData.getRuntime_maxMemory());
        assertEquals(0L, meterData.getRuntime_totalMemory());
        assertEquals(0.0, meterData.getSystemLoad());

        // Verify MeterData specific fields are cleared
        assertNull(meterData.getCategory());
        assertNull(meterData.getOperation());
        assertNull(meterData.getParent());
        assertNull(meterData.getDescription());
        assertEquals(0L, meterData.getCreateTime());
        assertEquals(0L, meterData.getStartTime());
        assertEquals(0L, meterData.getStopTime());
        assertEquals(0L, meterData.getTimeLimit());
        assertEquals(0L, meterData.getCurrentIteration());
        assertEquals(0L, meterData.getExpectedIterations());
        assertNull(meterData.getOkPath());
        assertNull(meterData.getRejectPath());
        assertNull(meterData.getFailPath());
        assertNull(meterData.getFailMessage());
        assertNull(meterData.getContext());

        // Verify status methods
        assertFalse(meterData.isStarted());
        assertFalse(meterData.isStopped());
        assertFalse(meterData.isOK());
        assertFalse(meterData.isReject());
        assertFalse(meterData.isFail());
    }

    @Test
    void testJsonMessageAllAttributes() {
        // Criar um mapa para o contexto
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("key1", "value1");
        contextMap.put("key2", "value2");

        // Criar MeterData com todos os campos populados
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                2, // time
                3, // heap_commited
                4, // heap_max
                5, // heap_used
                6, // nonHeap_commited
                7, // nonHeap_max
                8, // nonHeap_used
                9, // objectPendingFinalizationCount
                10, // classLoading_loaded
                11, // classLoading_total
                12, // classLoading_unloaded
                13, // compilationTime
                14, // garbageCollector_count
                15, // garbageCollector_time
                16, // runtime_usedMemory
                17, // runtime_maxMemory
                18, // runtime_totalMemory
                19.0, // systemLoad
                "categoryTest", // category
                "operationTest", // operation
                "parentTest", // parent
                "descriptionTest", // description
                20, // createTime
                21, // startTime
                22, // stopTime
                23, // timeLimit
                24, // currentIteration
                25, // expectedIterations
                "okPathTest", // okPath
                "reject", // rejectPath
                "fail", // failPath
                "Fail message", // failMessage
                contextMap // context
        );

        final String json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0,d:'descriptionTest',r:reject,p:okPathTest,f:fail,fm:'Fail message',c:categoryTest,n:operationTest,ep:parentTest,t0:20,t1:21,t2:22,i:24,ei:25,tl:23,ctx:{key1:value1,key2:value2}}", json);
    }

    @Test
    void testJsonMessageWithCategoryAndOperation() {
        // Criar MeterData apenas com categoria e operação
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, // time
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "categoryTest", // category
                "operationTest", // operation
                null, // parent
                null, 0, 0, 0, 0, 0, 0, null, null, null, null, null
        );

        final String json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,c:categoryTest,n:operationTest}", json);
    }

    @Test
    void testJsonMessageWithPaths() {
        // Teste para okPath
        MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null, null, 0, 0, 0, 0, 0, 0,
                "okPathTest", // okPath
                null, null, null, null
        );

        String json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,p:okPathTest}", json);

        // Teste para rejectPath
        meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null, null, 0, 0, 0, 0, 0, 0,
                null, // okPath
                "rejectPathTest", // rejectPath
                null, null, null
        );

        json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,r:rejectPathTest}", json);

        // Teste para failPath
        meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null, null, 0, 0, 0, 0, 0, 0,
                null, // okPath
                null, // rejectPath
                "failPathTest", // failPath
                "failMessageTest", // failMessage
                null
        );

        json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,f:failPathTest,fm:'failMessageTest'}", json);
    }

    @Test
    void testJsonMessageWithTimes() {
        // Teste para os tempos (create, start, stop, limit)
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null, null,
                100, // createTime
                200, // startTime
                300, // stopTime
                400, // timeLimit
                0, 0, null, null, null, null, null
        );

        final String json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,t0:100,t1:200,t2:300,tl:400}", json);
    }

    @Test
    void testJsonMessageWithIterations() {
        // Teste para iterações (atual e esperada)
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null, null, 0, 0, 0, 0,
                50, // currentIteration
                100, // expectedIterations
                null, null, null, null, null
        );

        final String json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,i:50,ei:100}", json);
    }

    @Test
    void testJsonMessageWithDescription() {
        // Teste para descrição
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null,
                "This is a test description", // description
                0, 0, 0, 0, 0, 0, null, null, null, null, null
        );

        final String json = meterData.json5Message();
        assertEquals("{_:abc,$:1,t:0,d:'This is a test description'}", json);
    }

    @Test
    void testJsonMessageWithContext() {
        // Criar um mapa para o contexto
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("env", "production");
        contextMap.put("user", "admin");
        contextMap.put("priority", "high");

        // Teste para o contexto
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null,
                contextMap // context
        );

        final String json = meterData.json5Message();
        assertTrue(json.contains("ctx:{"));
        assertTrue(json.contains("env:production"));
        assertTrue(json.contains("user:admin"));
        assertTrue(json.contains("priority:high"));
    }

    @Test
    void testReadJson5MeterDataMessage() {
        final MeterData meterData = new MeterData();

        meterData.readJson5("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0,d:'descriptionTest',r:reject,p:okPathTest,f:fail,fm:'Fail message',c:categoryTest,n:operationTest,ep:parentTest,t0:20,t1:21,t2:22,i:24,ei:25,tl:23,ctx:{key1:value1,key2:value2}}");

        // Verificar campos do SystemData
        assertEquals("abc", meterData.getSessionUuid());
        assertEquals(1L, meterData.getPosition());
        assertEquals(2L, meterData.getTime());
        assertEquals(16L, meterData.getRuntime_usedMemory());
        assertEquals(18L, meterData.getRuntime_totalMemory());
        assertEquals(17, meterData.getRuntime_maxMemory());
        assertEquals(5L, meterData.getHeap_used());
        assertEquals(3L, meterData.getHeap_commited());
        assertEquals(4L, meterData.getHeap_max());
        assertEquals(6L, meterData.getNonHeap_commited());
        assertEquals(7L, meterData.getNonHeap_max());
        assertEquals(8L, meterData.getNonHeap_used());
        assertEquals(9L, meterData.getObjectPendingFinalizationCount());
        assertEquals(10L, meterData.getClassLoading_loaded());
        assertEquals(11L, meterData.getClassLoading_total());
        assertEquals(12L, meterData.getClassLoading_unloaded());
        assertEquals(13L, meterData.getCompilationTime());
        assertEquals(14L, meterData.getGarbageCollector_count());
        assertEquals(15L, meterData.getGarbageCollector_time());
        assertEquals(16L, meterData.getRuntime_usedMemory());
        assertEquals(17L, meterData.getRuntime_maxMemory());
        assertEquals(18L, meterData.getRuntime_totalMemory());
        assertEquals(19.0, meterData.getSystemLoad(), 0.01);
        assertEquals("categoryTest", meterData.getCategory());
        assertEquals("operationTest", meterData.getOperation());
        assertEquals("parentTest", meterData.getParent());
        assertEquals("descriptionTest", meterData.getDescription());
        assertEquals(20L, meterData.getCreateTime());
        assertEquals(21L, meterData.getStartTime());
        assertEquals(22L, meterData.getStopTime());
        assertEquals(23L, meterData.getTimeLimit());
        assertEquals(24L, meterData.getCurrentIteration());
        assertEquals(25L, meterData.getExpectedIterations());
        assertEquals("okPathTest", meterData.getOkPath());
        assertEquals("reject", meterData.getRejectPath());
        assertEquals("fail", meterData.getFailPath());
        assertEquals("Fail message", meterData.getFailMessage());
    }

    private static Arguments example(final String expected, final String operation,
                                     final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit) {
        return Arguments.of(new MockMeterData("uuid", 1, null, operation, null, null,
                createTime, startTime,stopTime,currentTime,timeLimit,0,0,
                null,null,null,null), expected);
    }

    private static Arguments example(final String expected, final String operation,
                                     final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                     final long currentIteration, final long expectedIterations, String okPath, String rejectPath) {
        return Arguments.of(new MockMeterData("uuid", 1, null, operation, null, null,
                createTime, startTime,stopTime,currentTime,timeLimit,currentIteration,expectedIterations,
                okPath,rejectPath,null,null), expected);
    }

    static Stream<Arguments> provideTimeStatusTestCases() {
        return Stream.of(
                example("SCHEDULED: 190ns; uuid", null,10, 0,0, 200, 0),
                example("SCHEDULED: op 190ns; uuid", "op",10, 0,0, 200, 0),

                example("STARTED: uuid", null,10, 20,0, 200, 0),
                example("STARTED: op uuid", "op",10, 20,0, 200, 0),

                example("STARTED: 3,0us; uuid", null,10, 20,0, 3000, 0),
                example("STARTED: op 3,0us; uuid", "op",10, 20,0, 3000, 0),

                example("PROGRESS: 1/10; uuid", null,10, 20,0, 200, 0, 1,10, null, null),
                example("PROGRESS: op 1/10; uuid", "op",10, 20,0, 200, 0,1,10, null, null),
                example("PROGRESS: 1; uuid", null,10, 20,0, 200, 0, 1,0, null, null),
                example("PROGRESS: op 1; uuid", "op",10, 20,0, 200, 0,1,0, null, null),
                example("PROGRESS: 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,0, 3000, 0, 1,10, null, null),
                example("PROGRESS: op 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,0, 3000, 0,1,10, null, null),
                example("PROGRESS: 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,0, 3000, 0, 2,10, null, null),
                example("PROGRESS: op 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,0, 3000, 0,2,10, null, null),

                example("PROGRESS (Slow): 1/10; uuid", null,10, 20,0, 600, 500, 1,10, null, null),
                example("PROGRESS (Slow): op 1/10; uuid", "op",10, 20,0, 600, 500,1,10, null, null),
                example("PROGRESS (Slow): 1; uuid", null,10, 20,0, 600, 500, 1,0, null, null),
                example("PROGRESS (Slow): op 1; uuid", "op",10, 20,0, 600, 500,1,0, null, null),
                example("PROGRESS (Slow): 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,0, 3000, 500, 1,10, null, null),
                example("PROGRESS (Slow): op 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,0, 3000, 500,1,10, null, null),
                example("PROGRESS (Slow): 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,0, 3000, 500, 2,10, null, null),
                example("PROGRESS (Slow): op 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,0, 3000, 500,2,10, null, null),

                example("OK: 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, null, null),
                example("OK: op 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, null, null),

                example("OK (Slow): 3,0us; uuid", null,10, 20,3000, 4000, 200, 0,0, null, null),
                example("OK (Slow): op 3,0us; uuid", "op",10, 20,3000, 4000, 200,0,0, null, null),

                example("OK: 1/10; 180ns; 5,6M/s 180,0ns; uuid", null,10, 20,200, 200, 0, 1,10, null, null),
                example("OK: op 1/10; 180ns; 5,6M/s 180,0ns; uuid", "op",10, 20,200, 200, 0,1,10, null, null),
                example("OK: 1; 180ns; 5,6M/s 180,0ns; uuid", null,10, 20,200, 200, 0, 1,0, null, null),
                example("OK: op 1; 180ns; 5,6M/s 180,0ns; uuid", "op",10, 20,200, 200, 0,1,0, null, null),
                example("OK: 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,3000, 3000, 0, 1,10, null, null),
                example("OK: op 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,3000, 3000, 0,1,10, null, null),
                example("OK: 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,3000, 3000, 0, 2,10, null, null),
                example("OK: op 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,3000, 3000, 0,2,10, null, null),

                example("OK (Slow): 1/10; 580ns; 1,7M/s 580,0ns; uuid", null,10, 20,600, 4000, 500, 1,10, null, null),
                example("OK (Slow): op 1/10; 580ns; 1,7M/s 580,0ns; uuid", "op",10, 20,600, 4000, 500,1,10, null, null),
                example("OK (Slow): 1; 580ns; 1,7M/s 580,0ns; uuid", null,10, 20,600, 4000, 500, 1,0, null, null),
                example("OK (Slow): op 1; 580ns; 1,7M/s 580,0ns; uuid", "op",10, 20,600, 4000, 500,1,0, null, null),
                example("OK (Slow): 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,3000, 4000, 500, 1,10, null, null),
                example("OK (Slow): op 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,3000, 4000, 500,1,10, null, null),
                example("OK (Slow): 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,3000, 4000, 500, 2,10, null, null),
                example("OK (Slow): op 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,3000, 4000, 500,2,10, null, null),

                example("OK: [abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, "abc", null),
                example("OK: op[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, "abc", null),

                example("OK (Slow): [abc] 3,0us; uuid", null,10, 20,3000, 4000, 200, 0,0, "abc", null),
                example("OK (Slow): op[abc] 3,0us; uuid", "op",10, 20,3000, 4000, 200,0,0, "abc", null),

                example("OK: [abc] 1/10; 180ns; 5,6M/s 180,0ns; uuid", null,10, 20,200, 200, 0, 1,10, "abc", null),
                example("OK: op[abc] 1/10; 180ns; 5,6M/s 180,0ns; uuid", "op",10, 20,200, 200, 0,1,10, "abc", null),
                example("OK: [abc] 1; 180ns; 5,6M/s 180,0ns; uuid", null,10, 20,200, 200, 0, 1,0, "abc", null),
                example("OK: op[abc] 1; 180ns; 5,6M/s 180,0ns; uuid", "op",10, 20,200, 200, 0,1,0, "abc", null),
                example("OK: [abc] 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,3000, 3000, 0, 1,10, "abc", null),
                example("OK: op[abc] 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,3000, 3000, 0,1,10, "abc", null),
                example("OK: [abc] 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,3000, 3000, 0, 2,10, "abc", null),
                example("OK: op[abc] 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,3000, 3000, 0,2,10, "abc", null),

                example("OK (Slow): [abc] 1/10; 580ns; 1,7M/s 580,0ns; uuid", null,10, 20,600, 4000, 500, 1,10, "abc", null),
                example("OK (Slow): op[abc] 1/10; 580ns; 1,7M/s 580,0ns; uuid", "op",10, 20,600, 4000, 500,1,10, "abc", null),
                example("OK (Slow): [abc] 1; 580ns; 1,7M/s 580,0ns; uuid", null,10, 20,600, 4000, 500, 1,0, "abc", null),
                example("OK (Slow): op[abc] 1; 580ns; 1,7M/s 580,0ns; uuid", "op",10, 20,600, 4000, 500,1,0, "abc", null),
                example("OK (Slow): [abc] 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,3000, 4000, 500, 1,10, "abc", null),
                example("OK (Slow): op[abc] 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,3000, 4000, 500,1,10, "abc", null),
                example("OK (Slow): [abc] 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,3000, 4000, 500, 2,10, "abc", null),
                example("OK (Slow): op[abc] 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,3000, 4000, 500,2,10, "abc", null),

                example("REJECT: [abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, "abc"),
                example("REJECT: op[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, "abc"),

                example("REJECT: [abc] 1/10; 180ns; 5,6M/s 180,0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, "abc"),
                example("REJECT: op[abc] 1/10; 180ns; 5,6M/s 180,0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, "abc"),
                example("REJECT: [abc] 1; 180ns; 5,6M/s 180,0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, "abc"),
                example("REJECT: op[abc] 1; 180ns; 5,6M/s 180,0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, "abc"),
                example("REJECT: [abc] 1/10; 3,0us; 335,6k/s 3,0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, "abc"),
                example("REJECT: op[abc] 1/10; 3,0us; 335,6k/s 3,0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, "abc"),
                example("REJECT: [abc] 2/10; 3,0us; 671,1k/s 1,5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, "abc"),
                example("REJECT: op[abc] 2/10; 3,0us; 671,1k/s 1,5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, "abc")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTimeStatusTestCases")
    void testReadableMessageTimeStatus(final MockMeterData value, final String expected) {
        assertEquals(expected, value.readableMessage());
    }

    private static class MockMeterData extends MeterData {

        public MockMeterData(final String sessionUuid, final long position,
                             final String category, final String operation, final String parent, final String description,
                             final long createTime, final long startTime, final long stopTime, final int currentTime,
                             final long timeLimit, final long currentIteration, final long expectedIterations,
                             final String okPath, final String rejectPath, final String failPath, final String failMessage) {
            super(sessionUuid, position, currentTime,
                    0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                    category, operation, parent, description, createTime, startTime, stopTime, timeLimit, currentIteration, expectedIterations, okPath, rejectPath, failPath, failMessage, null);
        }
    }
}

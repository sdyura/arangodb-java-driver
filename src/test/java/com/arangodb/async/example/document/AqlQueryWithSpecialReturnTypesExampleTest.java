/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.async.example.document;

import com.arangodb.async.example.ExampleBase;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Mark Vollmary
 */
class AqlQueryWithSpecialReturnTypesExampleTest extends ExampleBase {

    @BeforeAll
    static void before() throws InterruptedException, ExecutionException {
        createExamples();
    }

    private static void createExamples() throws InterruptedException, ExecutionException {
        for (int i = 0; i < 100; i++) {
            final BaseDocument value = new BaseDocument(UUID.randomUUID().toString());
            value.addAttribute("name", "TestUser" + i);
            value.addAttribute("gender", (i % 2) == 0 ? Gender.MALE : Gender.FEMALE);
            value.addAttribute("age", i + 10);
            db.collection(COLLECTION_NAME).insertDocument(value).get();
        }
    }

    @Test
    void aqlWithLimitQueryAsJsonObject() throws InterruptedException, ExecutionException {
        final String query = "FOR t IN " + COLLECTION_NAME
                + " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
        final Map<String, Object> bindVars = Collections.singletonMap("gender", Gender.FEMALE);
        db.query(query, bindVars, null, ObjectNode.class)
                .whenComplete((cursor, ex) -> cursor.forEachRemaining(node -> {
                    assertThat(node.get("name").asText()).isIn("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19");
                    assertThat(node.get("gender").asText()).isEqualTo(Gender.FEMALE.name());
                    assertThat(node.get("age").asInt()).isIn(21, 23, 25, 27, 29);
                }))
                .get();
    }

    @Test
    void aqlWithLimitQueryAsArrayNode() throws InterruptedException, ExecutionException {
        final String query = "FOR t IN " + COLLECTION_NAME
                + " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN [t.name, t.gender, t.age]";
        final Map<String, Object> bindVars = Collections.singletonMap("gender", Gender.FEMALE);
        db.query(query, bindVars, null, ArrayNode.class)
                .whenComplete((cursor, ex) -> cursor.forEachRemaining(arrNode -> {
                    assertThat(arrNode.get(0).asText()).isIn("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19");
                    assertThat(arrNode.get(1).asText()).isEqualTo(Gender.FEMALE.name());
                    assertThat(arrNode.get(2).asInt()).isIn(21, 23, 25, 27, 29);
                }))
                .get();
    }

    @Test
    void aqlWithLimitQueryAsMap() throws InterruptedException, ExecutionException {
        final String query = "FOR t IN " + COLLECTION_NAME
                + " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN t";
        final Map<String, Object> bindVars = Collections.singletonMap("gender", Gender.FEMALE);
        db.query(query, bindVars, null, Map.class)
                .whenComplete((cursor, ex) -> cursor.forEachRemaining(map -> {
                    assertThat(map.get("name")).isNotNull();
                    assertThat(String.valueOf(map.get("name"))).isIn("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19");
                    assertThat(map.get("gender")).isNotNull();
                    assertThat(String.valueOf(map.get("gender"))).isEqualTo(Gender.FEMALE.name());
                    assertThat(map.get("age")).isNotNull();
                    assertThat(Long.valueOf(map.get("age").toString())).isIn(21L, 23L, 25L, 27L, 29L);
                }))
                .get();
    }

    @Test
    void aqlWithLimitQueryAsList() throws InterruptedException, ExecutionException {
        final String query = "FOR t IN " + COLLECTION_NAME
                + " FILTER t.age >= 20 && t.age < 30 && t.gender == @gender RETURN [t.name, t.gender, t.age]";
        final Map<String, Object> bindVars = Collections.singletonMap("gender", Gender.FEMALE);
        db.query(query, bindVars, null, List.class)
                .whenComplete((cursor, ex) -> cursor.forEachRemaining(list -> {
                    assertThat(list.get(0)).isNotNull();
                    assertThat(String.valueOf(list.get(0))).isIn("TestUser11", "TestUser13", "TestUser15", "TestUser17", "TestUser19");
                    assertThat(list.get(1)).isNotNull();
                    assertThat(Gender.valueOf(String.valueOf(list.get(1)))).isEqualTo(Gender.FEMALE);
                    assertThat(list.get(2)).isNotNull();
                    assertThat(Long.valueOf(String.valueOf(list.get(2)))).isIn(21L, 23L, 25L, 27L, 29L);
                }))
                .get();
    }

    public enum Gender {
        MALE, FEMALE
    }
}

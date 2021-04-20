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

package com.arangodb;

import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * AQL tests to read and write documents and bindVars with VPack UTC-date fields (0x1c)
 */
public class AqlVPackTest {

    private static final String DB_NAME = "aqlVPackTest_db";
    protected static final String COLLECTION_NAME = "aqlVPackTest_collection";

    private static ArangoDB arangoDB;
    protected static ArangoDatabase db;
    protected static ArangoCollection collection;

    @BeforeClass
    public static void setUp() {
        arangoDB = new ArangoDB.Builder()
                .serializer(new ArangoJack())
                .build();
        if (arangoDB.db(DB_NAME).exists())
            arangoDB.db(DB_NAME).drop();
        arangoDB.createDatabase(DB_NAME);
        db = arangoDB.db(DB_NAME);
        db.createCollection(COLLECTION_NAME);
        collection = db.collection(COLLECTION_NAME);
    }

    @AfterClass
    public static void tearDown() {
        db.drop();
        arangoDB.shutdown();
    }

    @Test
    public void returnBindVarObjectWithDateFieldFromAQL() {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);
        builder.add("_key", "key-" + UUID.randomUUID());
        builder.add("date", new Date());
        builder.close();
        VPackSlice input = builder.slice();
        checkFieldType(input, "date", ValueType.UTC_DATE);

        ArangoCursor<VPackSlice> res = db.query(
                "RETURN @doc",
                Collections.singletonMap("doc", input),
                new AqlQueryOptions(),
                VPackSlice.class);
        VPackSlice doc = res.next();
        checkFieldType(doc, "date", ValueType.UTC_DATE);
    }

    @Test
    public void getDocumentWithDateFieldFromAQL() {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);
        builder.add("_key", "key-" + UUID.randomUUID());
        builder.add("date", new Date());
        builder.close();
        VPackSlice input = builder.slice();
        checkFieldType(input, "date", ValueType.UTC_DATE);

        DocumentCreateEntity<VPackSlice> inserted = collection.insertDocument(input, new DocumentCreateOptions().returnNew(true));
        checkFieldType(inserted.getNew(), "date", ValueType.UTC_DATE);

        ArangoCursor<VPackSlice> res = db.query(
                "RETURN DOCUMENT(@id)",
                Collections.singletonMap("id", inserted.getId()),
                new AqlQueryOptions(),
                VPackSlice.class);
        VPackSlice doc = res.next();
        checkFieldType(doc, "date", ValueType.UTC_DATE);

        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", COLLECTION_NAME);
        bindVars.put("key", inserted.getKey());

        ArangoCursor<VPackSlice> res2 = db.query(
                "FOR d in @@collection FILTER d._key == @key RETURN d",
                bindVars,
                new AqlQueryOptions(),
                VPackSlice.class);
        VPackSlice doc2 = res2.next();
        checkFieldType(doc2, "date", ValueType.UTC_DATE);
    }

    @Test
    public void insertDocumentWithDateFieldFromAQL() {
        final String key = "key-" + UUID.randomUUID();
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);
        builder.add("_key", key);
        builder.add("date", new Date());
        builder.close();
        VPackSlice input = builder.slice();
        checkFieldType(input, "date", ValueType.UTC_DATE);

        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", COLLECTION_NAME);
        bindVars.put("input", input);

        ArangoCursor<VPackSlice> res = db.query(
                "INSERT @input INTO @@collection RETURN NEW",
                bindVars,
                new AqlQueryOptions(),
                VPackSlice.class);
        VPackSlice doc = res.next();
        checkFieldType(doc, "date", ValueType.UTC_DATE);

        VPackSlice readDoc = collection.getDocument(key, VPackSlice.class);
        checkFieldType(readDoc, "date", ValueType.UTC_DATE);
    }

    private static void checkFieldType(VPackSlice slice, String fieldName, ValueType expectedType) {
        Iterator<Map.Entry<String, VPackSlice>> oi = slice.objectIterator();
        while (oi.hasNext()) {
            Map.Entry<String, VPackSlice> field = oi.next();
            if (field.getKey().equals(fieldName)) {
                VPackSlice date = field.getValue();
                assertThat(date.getType(), Matchers.is(expectedType));
            }
        }
    }

}

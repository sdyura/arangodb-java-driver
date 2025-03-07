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

import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.ReplicationFactor;
import com.arangodb.model.EdgeCollectionDropOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.ReplaceEdgeDefinitionOptions;
import com.arangodb.model.VertexCollectionCreateOptions;
import com.arangodb.util.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoGraphTest extends BaseJunit5 {

    private static final String GRAPH_NAME = "ArangoGraphTest_graph";

    private static final String VERTEX_COL_1 = rndName();
    private static final String VERTEX_COL_2 = rndName();
    private static final String VERTEX_COL_3 = rndName();
    private static final String VERTEX_COL_4 = rndName();
    private static final String VERTEX_COL_5 = rndName();

    private static final String EDGE_COL_1 = rndName();
    private static final String EDGE_COL_2 = rndName();
    private static final String EDGE_COL_3 = rndName();

    private static final Integer REPLICATION_FACTOR = 2;
    private static final Integer NUMBER_OF_SHARDS = 2;

    private static final EdgeDefinition ed1 =
            new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_1).to(VERTEX_COL_5);
    private static final EdgeDefinition ed2 =
            new EdgeDefinition().collection(EDGE_COL_2).from(VERTEX_COL_2).to(VERTEX_COL_1, VERTEX_COL_3);

    private static Stream<Arguments> graphs() {
        return dbsStream()
                .map(db -> db.graph(GRAPH_NAME))
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        final Collection<EdgeDefinition> edgeDefinitions = Arrays.asList(ed1, ed2);

        final GraphCreateOptions options = new GraphCreateOptions()
                .replicationFactor(REPLICATION_FACTOR)
                .numberOfShards(NUMBER_OF_SHARDS);

        initGraph(GRAPH_NAME, edgeDefinitions, options);
    }


    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void exists(ArangoGraph graph) {
        assertThat(graph.exists()).isTrue();
        assertThat(graph.db().graph(GRAPH_NAME + "no").exists()).isFalse();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createWithReplicationAndWriteConcern(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isCluster());

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        final GraphEntity graph = db.createGraph(GRAPH_NAME + "_1", edgeDefinitions,
                new GraphCreateOptions().isSmart(true).replicationFactor(2).writeConcern(2));
        assertThat(graph).isNotNull();
        assertThat(graph.getName()).isEqualTo(GRAPH_NAME + "_1");
        assertThat(graph.getWriteConcern()).isEqualTo(2);
        assertThat(graph.getReplicationFactor().get()).isEqualTo(2);
        db.graph(GRAPH_NAME + "_1").drop();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getGraphs(ArangoDatabase db) {
        final Collection<GraphEntity> graphs = db.getGraphs();
        assertThat(graphs.stream().anyMatch(it -> it.getName().equals(GRAPH_NAME))).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void getInfo(ArangoGraph graph) {
        final GraphEntity info = graph.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getName()).isEqualTo(GRAPH_NAME);
        assertThat(info.getEdgeDefinitions()).hasSize(2);

        assertThat(info.getEdgeDefinitions())
                .anySatisfy(e1 -> {
                    assertThat(e1.getCollection()).isEqualTo(EDGE_COL_1);
                    assertThat(e1.getFrom()).contains(VERTEX_COL_1);
                    assertThat(e1.getTo()).contains(VERTEX_COL_5);
                })
                .anySatisfy(e2 -> {
                    assertThat(e2.getCollection()).isEqualTo(EDGE_COL_2);
                    assertThat(e2.getFrom()).contains(VERTEX_COL_2);
                    assertThat(e2.getTo()).contains(VERTEX_COL_1, VERTEX_COL_3);
                });

        assertThat(info.getOrphanCollections()).isEmpty();

        if (isCluster()) {
            for (final String collection : new String[]{EDGE_COL_1, EDGE_COL_2, VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_5}) {
                final CollectionPropertiesEntity properties = graph.db().collection(collection).getProperties();
                assertThat(properties.getReplicationFactor().get()).isEqualTo(REPLICATION_FACTOR);
                assertThat(properties.getNumberOfShards()).isEqualTo(NUMBER_OF_SHARDS);
            }
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void getVertexCollections(ArangoGraph graph) {
        final Collection<String> vertexCollections = graph.getVertexCollections();
        assertThat(vertexCollections)
                .hasSize(4)
                .contains(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_5);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void addVertexCollection(ArangoGraph graph) {
        final GraphEntity g = graph.addVertexCollection(VERTEX_COL_4);
        assertThat(g).isNotNull();
        final Collection<String> vertexCollections = graph.getVertexCollections();
        assertThat(vertexCollections).contains(VERTEX_COL_1, VERTEX_COL_2, VERTEX_COL_3, VERTEX_COL_4, VERTEX_COL_5);

        // revert
        graph.vertexCollection(VERTEX_COL_4).drop();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void addSatelliteVertexCollection(ArangoDatabase db) {
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 9));

        String v1Name = "vertex-" + rnd();

        ArangoGraph g = db.graph(GRAPH_NAME + rnd());
        g.create(null, new GraphCreateOptions().isSmart(true).smartGraphAttribute("test"));
        g.addVertexCollection(v1Name, new VertexCollectionCreateOptions().satellites(v1Name));

        Collection<String> vertexCollections = g.getVertexCollections();
        assertThat(vertexCollections).contains(v1Name);
        assertThat(db.collection(v1Name).getProperties().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        // revert
        g.drop();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void getEdgeCollections(ArangoGraph graph) {
        final Collection<String> edgeCollections = graph.getEdgeDefinitions();
        assertThat(edgeCollections)
                .hasSize(2)
                .contains(EDGE_COL_1, EDGE_COL_2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void addEdgeDefinition(ArangoGraph graph) {
        EdgeDefinition ed = new EdgeDefinition().collection(EDGE_COL_3).from(VERTEX_COL_1).to(VERTEX_COL_2);
        final GraphEntity g = graph.addEdgeDefinition(ed);
        assertThat(g).isNotNull();
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(3);
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_3)) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_3)) {
                assertThat(e.getFrom()).contains(VERTEX_COL_1);
                assertThat(e.getTo()).contains(VERTEX_COL_2);
            }
        }
        if (isCluster()) {
            final CollectionPropertiesEntity properties = graph.db().collection(EDGE_COL_3).getProperties();
            assertThat(properties.getReplicationFactor().get()).isEqualTo(REPLICATION_FACTOR);
            assertThat(properties.getNumberOfShards()).isEqualTo(NUMBER_OF_SHARDS);
        }

        // revert
        graph.edgeCollection(EDGE_COL_3).drop();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void addSatelliteEdgeDefinition(ArangoDatabase db) {
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));
        assumeTrue(isEnterprise());
        assumeTrue(isAtLeastVersion(3, 9));

        String eName = "edge-" + rnd();
        String v1Name = "vertex-" + rnd();
        String v2Name = "vertex-" + rnd();
        EdgeDefinition ed = new EdgeDefinition().collection(eName).from(v1Name).to(v2Name).satellites(v1Name);

        ArangoGraph g = db.graph(GRAPH_NAME + rnd());
        g.create(null, new GraphCreateOptions().isSmart(true).smartGraphAttribute("test"));
        g.addEdgeDefinition(ed);
        final GraphEntity ge = g.getInfo();
        assertThat(ge).isNotNull();
        final Collection<EdgeDefinition> edgeDefinitions = ge.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(1);
        EdgeDefinition e = edgeDefinitions.iterator().next();
        assertThat(e.getCollection()).isEqualTo(eName);
        assertThat(e.getFrom()).contains(v1Name);
        assertThat(e.getTo()).contains(v2Name);

        assertThat(db.collection(v1Name).getProperties().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());

        // revert
        g.drop();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void replaceEdgeDefinition(ArangoGraph graph) {
        final GraphEntity g = graph
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4));
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(2);
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                assertThat(e.getFrom()).contains(VERTEX_COL_3);
                assertThat(e.getTo()).contains(VERTEX_COL_4);
            }
        }
        assertThat(graph.db().collection(VERTEX_COL_1).exists()).isTrue();

        // revert
        graph.edgeCollection(EDGE_COL_1).drop();
        graph.vertexCollection(VERTEX_COL_4).drop();
        graph.addEdgeDefinition(ed1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    @Disabled
    // FIXME: with dropCollections=true the vertex collections remain in the graph as orphan and not dropped
    void replaceEdgeDefinitionDropCollections(ArangoGraph graph) {
        final GraphEntity g = graph
                .replaceEdgeDefinition(new EdgeDefinition().collection(EDGE_COL_1).from(VERTEX_COL_3).to(VERTEX_COL_4),
                        new ReplaceEdgeDefinitionOptions().waitForSync(true).dropCollections(true));
        final Collection<EdgeDefinition> edgeDefinitions = g.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(2);
        int count = 0;
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                count++;
            }
        }
        assertThat(count).isEqualTo(1);
        for (final EdgeDefinition e : edgeDefinitions) {
            if (e.getCollection().equals(EDGE_COL_1)) {
                assertThat(e.getFrom()).contains(VERTEX_COL_3);
                assertThat(e.getTo()).contains(VERTEX_COL_4);
            }
        }
        assertThat(graph.db().collection(VERTEX_COL_5).exists()).isFalse();

        // revert
        graph.edgeCollection(EDGE_COL_1).drop();
        graph.vertexCollection(VERTEX_COL_4).drop();
        graph.addEdgeDefinition(ed1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void removeEdgeDefinition(ArangoGraph graph) {
        graph.edgeCollection(EDGE_COL_1).drop();
        Collection<String> edgeDefinitions = graph.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(1);
        assertThat(edgeDefinitions.iterator().next()).isEqualTo(EDGE_COL_2);
        assertThat(graph.db().collection(EDGE_COL_1).exists()).isTrue();

        //revert
        graph.addEdgeDefinition(ed1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("graphs")
    void removeEdgeDefinitionDropCollections(ArangoGraph graph) {
        graph.edgeCollection(EDGE_COL_1).drop(new EdgeCollectionDropOptions()
                .dropCollections(true)
                .waitForSync(true));
        Collection<String> edgeDefinitions = graph.getEdgeDefinitions();
        assertThat(edgeDefinitions).hasSize(1);
        assertThat(edgeDefinitions.iterator().next()).isEqualTo(EDGE_COL_2);
        assertThat(graph.db().collection(EDGE_COL_1).exists()).isFalse();

        //revert
        graph.addEdgeDefinition(ed1);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void smartGraph(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("smartGraph-edge-" + rnd()).from("smartGraph-vertex-" + rnd()).to("smartGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions,
                new GraphCreateOptions().isSmart(true).smartGraphAttribute("test").numberOfShards(2));

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void hybridSmartGraph(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));
        assumeTrue((isAtLeastVersion(3, 9)));

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        String eName = "hybridSmartGraph-edge-" + rnd();
        String v1Name = "hybridSmartGraph-vertex-" + rnd();
        String v2Name = "hybridSmartGraph-vertex-" + rnd();
        edgeDefinitions.add(new EdgeDefinition().collection(eName).from(v1Name).to(v2Name));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .satellites(eName, v1Name)
                .isSmart(true).smartGraphAttribute("test").replicationFactor(2).numberOfShards(2));

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);

        assertThat(db.collection(eName).getProperties().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(db.collection(v1Name).getProperties().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(db.collection(v2Name).getProperties().getReplicationFactor().get()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void disjointSmartGraph(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));
        assumeTrue((isAtLeastVersion(3, 7)));

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("smartGraph-edge-" + rnd()).from("smartGraph-vertex-" + rnd()).to("smartGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .isSmart(true).isDisjoint(true).smartGraphAttribute("test").numberOfShards(2));

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getIsDisjoint()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void hybridDisjointSmartGraph(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));
        assumeTrue((isAtLeastVersion(3, 9)));

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        String eName = "hybridDisjointSmartGraph-edge-" + rnd();
        String v1Name = "hybridDisjointSmartGraph-vertex-" + rnd();
        String v2Name = "hybridDisjointSmartGraph-vertex-" + rnd();
        edgeDefinitions.add(new EdgeDefinition().collection(eName).from(v1Name).to(v2Name));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions()
                .satellites(v1Name)
                .isSmart(true).isDisjoint(true).smartGraphAttribute("test").replicationFactor(2).numberOfShards(2));

        assertThat(g).isNotNull();
        assertThat(g.getIsSmart()).isTrue();
        assertThat(g.getIsDisjoint()).isTrue();
        assertThat(g.getSmartGraphAttribute()).isEqualTo("test");
        assertThat(g.getNumberOfShards()).isEqualTo(2);

        assertThat(db.collection(v1Name).getProperties().getReplicationFactor()).isEqualTo(ReplicationFactor.ofSatellite());
        assertThat(db.collection(v2Name).getProperties().getReplicationFactor().get()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void enterpriseGraph(ArangoDatabase db) {
        assumeTrue(isEnterprise());
        assumeTrue(isCluster() || isAtLeastVersion(3, 10));

        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        edgeDefinitions.add(new EdgeDefinition().collection("enterpriseGraph-edge-" + rnd()).from("enterpriseGraph-vertex-" + rnd()).to("enterpriseGraph-vertex-" + rnd()));

        String graphId = GRAPH_NAME + rnd();
        final GraphEntity g = db.createGraph(graphId, edgeDefinitions, new GraphCreateOptions().isSmart(true).numberOfShards(2));

        assertThat(g).isNotNull();
        assertThat(g.getSmartGraphAttribute()).isNull();
        assertThat(g.getNumberOfShards()).isEqualTo(2);
        if (isAtLeastVersion(3, 10)) {
            assertThat(g.getIsSmart()).isTrue();
        } else {
            assertThat(g.getIsSmart()).isFalse();
        }
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void drop(ArangoDatabase db) {
        final String edgeCollection = "edge_" + rnd();
        final String vertexCollection = "vertex_" + rnd();
        final String graphId = GRAPH_NAME + rnd();
        final GraphEntity result = db.graph(graphId).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)));
        assertThat(result).isNotNull();
        db.graph(graphId).drop();
        assertThat(db.collection(edgeCollection).exists()).isTrue();
        assertThat(db.collection(vertexCollection).exists()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void dropPlusDropCollections(ArangoDatabase db) {
        final String edgeCollection = "edge_dropC" + rnd();
        final String vertexCollection = "vertex_dropC" + rnd();
        final String graphId = GRAPH_NAME + "_dropC" + rnd();
        final GraphEntity result = db.graph(graphId).create(Collections
                .singleton(new EdgeDefinition().collection(edgeCollection).from(vertexCollection).to(vertexCollection)));
        assertThat(result).isNotNull();
        db.graph(graphId).drop(true);
        assertThat(db.collection(edgeCollection).exists()).isFalse();
        assertThat(db.collection(vertexCollection).exists()).isFalse();
    }

}

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

package com.arangodb.entity.arangosearch.analyzer;


import com.arangodb.entity.arangosearch.AnalyzerFeature;
import com.arangodb.entity.arangosearch.AnalyzerType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Michele Rastelli
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "identity", value = IdentityAnalyzer.class),
        @JsonSubTypes.Type(name = "delimiter", value = DelimiterAnalyzer.class),
        @JsonSubTypes.Type(name = "stem", value = StemAnalyzer.class),
        @JsonSubTypes.Type(name = "norm", value = NormAnalyzer.class),
        @JsonSubTypes.Type(name = "ngram", value = NGramAnalyzer.class),
        @JsonSubTypes.Type(name = "text", value = TextAnalyzer.class),
        @JsonSubTypes.Type(name = "pipeline", value = PipelineAnalyzer.class),
        @JsonSubTypes.Type(name = "stopwords", value = StopwordsAnalyzer.class),
        @JsonSubTypes.Type(name = "aql", value = AQLAnalyzer.class),
        @JsonSubTypes.Type(name = "geojson", value = GeoJSONAnalyzer.class),
        @JsonSubTypes.Type(name = "geopoint", value = GeoPointAnalyzer.class),
        @JsonSubTypes.Type(name = "geo_s2", value = GeoS2Analyzer.class),
        @JsonSubTypes.Type(name = "segmentation", value = SegmentationAnalyzer.class),
        @JsonSubTypes.Type(name = "collation", value = CollationAnalyzer.class),
        @JsonSubTypes.Type(name = "classification", value = ClassificationAnalyzer.class),
        @JsonSubTypes.Type(name = "nearest_neighbors", value = NearestNeighborsAnalyzer.class),
        @JsonSubTypes.Type(name = "minhash", value = MinHashAnalyzer.class)
})
public abstract class SearchAnalyzer {
    private String name;
    private AnalyzerType type;
    private Collection<AnalyzerFeature> features;

    /**
     * @return The Analyzer name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The Analyzer type.
     */
    @JsonIgnore
    public AnalyzerType getType() {
        return type;
    }

    public void setType(AnalyzerType type) {
        this.type = type;
    }

    /**
     * @return The set of features to set on the Analyzer generated fields.
     */
    public Set<AnalyzerFeature> getFeatures() {
        return features != null ? new HashSet<>(features) : null;
    }

    public void setFeatures(Set<AnalyzerFeature> features) {
        this.features = features;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchAnalyzer that = (SearchAnalyzer) o;
        return Objects.equals(getName(), that.getName())
                && getType() == that.getType()
                && Objects.equals(getFeatures(), that.getFeatures());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, features);
    }
}

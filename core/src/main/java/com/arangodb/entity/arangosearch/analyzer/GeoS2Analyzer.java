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

import com.arangodb.entity.arangosearch.AnalyzerType;

import java.util.Objects;

/**
 * An Analyzer capable of breaking up a GeoJSON object or coordinate array in [longitude, latitude] order into a set of
 * indexable tokens for further usage with ArangoSearch Geo functions.
 * <p>
 * The Analyzer is similar to {@link GeoJSONAnalyzer}, but it internally uses a format for storing the geo-spatial data
 * that is more efficient. You can choose between different formats to make a tradeoff between the size on disk, the
 * precision, and query performance.
 *
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/analyzers.html#geo_s2">API Documentation</a>
 * @since ArangoDB 3.10.5
 */
public final class GeoS2Analyzer extends SearchAnalyzer {
    private GeoS2AnalyzerProperties properties;

    public GeoS2Analyzer() {
        setType(AnalyzerType.geo_s2);
    }

    public GeoS2AnalyzerProperties getProperties() {
        return properties;
    }

    public void setProperties(GeoS2AnalyzerProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GeoS2Analyzer that = (GeoS2Analyzer) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties);
    }
}

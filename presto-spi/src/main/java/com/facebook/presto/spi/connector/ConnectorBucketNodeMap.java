/*
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
package com.facebook.presto.spi.connector;

import com.facebook.presto.spi.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

public final class ConnectorBucketNodeMap
{
    private final int bucketCount;
    private final Optional<Map<Integer, Node>> bucketToNode;

    public static ConnectorBucketNodeMap createBucketNodeMap(int bucketCount)
    {
        return new ConnectorBucketNodeMap(bucketCount, Optional.empty());
    }

    public static ConnectorBucketNodeMap createBucketNodeMap(Map<Integer, Node> bucketToNode)
    {
        requireNonNull(bucketToNode, "bucketToNode is null");
        int maxBucket = bucketToNode.keySet().stream()
                .mapToInt(Integer::intValue)
                .peek(bucket -> {
                    if (bucket < 0) {
                        throw new IllegalArgumentException("Bucket number must be positive: " + bucket);
                    }
                })
                .max()
                .orElseThrow(() -> new IllegalArgumentException("bucketToNode is empty"));
        return new ConnectorBucketNodeMap(maxBucket + 1, Optional.of(bucketToNode));
    }

    private ConnectorBucketNodeMap(int bucketCount, Optional<Map<Integer, Node>> bucketToNode)
    {
        if (bucketCount <= 0) {
            throw new IllegalArgumentException("bucketCount must be positive");
        }
        if (bucketToNode.isPresent() && bucketToNode.get().size() != bucketCount) {
            throw new IllegalArgumentException(format("Mismatched bucket count in bucketToNode (%s) and bucketCount (%s)", bucketToNode.get().size(), bucketCount));
        }
        this.bucketCount = bucketCount;
        this.bucketToNode = requireNonNull(bucketToNode, "bucketToNode is null")
                .map(mapping -> unmodifiableMap(new HashMap<>(mapping)));
    }

    public int getBucketCount()
    {
        return bucketCount;
    }

    public boolean hasFixedMapping()
    {
        return bucketToNode.isPresent();
    }

    public Map<Integer, Node> getFixedMapping()
    {
        return bucketToNode.orElseThrow(() -> new IllegalArgumentException("No fixed bucket to node mapping"));
    }
}

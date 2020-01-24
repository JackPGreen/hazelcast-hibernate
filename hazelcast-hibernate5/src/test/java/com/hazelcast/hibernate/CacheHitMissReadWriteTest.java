/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.hibernate;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Environment;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Read and write access (strict) cache concurrency strategy of Hibernate.
 * Data may be added, removed and mutated.
 * Strict means data integrity is preserved strictly (by locks)
 * Write through cache
 */
@RunWith(HazelcastSerialClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class CacheHitMissReadWriteTest extends HibernateStatisticsTestSupport {

    @Override
    protected AccessType getCacheStrategy() {
        return AccessType.READ_WRITE;
    }

    @Override
    protected Properties getCacheProperties() {
        Properties props = new Properties();
        props.setProperty(Environment.CACHE_REGION_FACTORY, HazelcastCacheRegionFactory.class.getName());
        return props;
    }

    @Test
    public void testGetUpdateRemoveGet()
            throws Exception {
        insertDummyEntities(10, 4);
        //all 10 entities and 40 properties are cached
        SecondLevelCacheStatistics dummyEntityCacheStats = sf.getStatistics().getSecondLevelCacheStatistics(CACHE_ENTITY);
        SecondLevelCacheStatistics dummyPropertyCacheStats = sf.getStatistics().getSecondLevelCacheStatistics(CACHE_PROPERTY);

        sf.getCache().evictEntityRegions();
        sf.getCache().evictCollectionRegions();

        //miss 10 entities
        getDummyEntities(10);

        //hit 1 entity and 4 properties
        updateDummyEntityName(2, "updated");

        //hit 1 entity, hit 4 properties
        getPropertiesOfEntity(2);
        //hit 1 entity and 4 properties
        deleteDummyEntity(1);

        assertEquals(12, dummyPropertyCacheStats.getHitCount());
        assertEquals(0, dummyPropertyCacheStats.getMissCount());
        assertEquals(3, dummyEntityCacheStats.getHitCount());
        assertEquals(10, dummyEntityCacheStats.getMissCount());

    }

    @Test
    public void testUpdateShouldNotInvalidateEntryInCache() {
        insertDummyEntities(10, 4);
        //all 10 entities and 40 properties are cached
        SecondLevelCacheStatistics dummyEntityCacheStats = sf.getStatistics().getSecondLevelCacheStatistics(CACHE_ENTITY);

        sf.getCache().evictEntityRegions();
        sf.getCache().evictCollectionRegions();

        //miss 10 entities, 10 entities are cached
        getDummyEntities(10);

        //updates cache entity
        updateDummyEntityName(2, "updated");

        assertEquals(10, dummyEntityCacheStats.getElementCountInMemory());
    }
}

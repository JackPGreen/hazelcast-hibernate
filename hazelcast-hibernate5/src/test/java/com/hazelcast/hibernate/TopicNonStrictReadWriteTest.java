/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.hibernate;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.SlowTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(SlowTest.class)
public class TopicNonStrictReadWriteTest extends TopicNonStrictReadWriteTestSupport {

    @Test
    public void testUpdateQueryByNaturalId() {
        insertAnnotatedEntities(sf, 2);

        executeUpdateQuery(sf, "update AnnotatedEntity set title = 'updated-name' where title = 'dummy:1'");

        assertTopicNotifications(1, CACHE_ANNOTATED_ENTITY + "##NaturalId");
        assertTopicNotifications(4, CACHE_TIMESTAMPS_REGION);
    }
}

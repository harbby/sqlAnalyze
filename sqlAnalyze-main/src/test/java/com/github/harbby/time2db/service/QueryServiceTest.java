/*
 * Copyright (C) 2019 The Time2DB Authors
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
package com.github.harbby.time2db.service;

import com.github.harbby.gadtry.aop.mock.InjectMock;
import com.github.harbby.gadtry.aop.mock.Mock;
import com.github.harbby.gadtry.aop.mock.MockGo;
import com.github.harbby.time2db.MetricDBDao;
import com.github.harbby.time2db.rest.QueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.harbby.gadtry.aop.mock.MockGo.when;
import static com.github.harbby.gadtry.aop.mock.MockGoArgument.anyString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryServiceTest
{
    @Mock
    private MetaTableManager metaTable;
    @Mock
    private MetricDBDao metricDBDao;
    @InjectMock
    private QueryService queryService;

    @BeforeEach
    void init()
    {
        MockGo.initMocks(this);
    }

    @Test
    void getQueryJoin()
    {
        when(metaTable.getTableNameFirst(anyString())).thenReturn("table1");
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setMetrics("metric1,metric2,metric3");
        queryRequest.setGroup("tag1,tag2,tag3");
        queryRequest.setFilter("day>=20201001 and day<=20201007");

        Map<String, String> stages = queryService.getQueryList(queryRequest);
        String queryDsl = queryService.getQueryJoin(stages, queryRequest);
        assertNotNull(queryDsl);
    }
}
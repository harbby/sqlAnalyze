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
package com.github.harbby.time2db;

import com.github.harbby.gadtry.ioc.Autowired;
import com.github.harbby.time2db.rest.QueryRequest;
import com.github.harbby.time2db.rest.JsonResult;
import com.github.harbby.time2db.service.QueryService;

import static java.util.Objects.requireNonNull;

public class SqlAnalyzeContextImpl
        implements SqlAnalyzeContext
{
    private final QueryService queryService;

    @Autowired
    public SqlAnalyzeContextImpl(QueryService queryService)
    {
        this.queryService = requireNonNull(queryService, "queryService is null");
    }

    @Override
    public JsonResult query(QueryRequest query)
    {
        return queryService.query(query);
    }
}

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
package com.github.harbby.time2db.bean;

import com.github.harbby.time2db.MetricDBDao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcMetricDBDao
        implements MetricDBDao
{
    public static final int MAX_RESULT_SIZE = 5000;
    private Connection connection;

    public Connection getConnection()
    {
        return connection;
    }

    @Override
    public List<Map<String, Object>> exec(String query)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(query)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int count = 0;
                    while (rs.next() && count < MAX_RESULT_SIZE) {
                        Map<String, Object> map = new HashMap<>(metaData.getColumnCount());
                        for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                            map.put(metaData.getColumnName(i).toLowerCase(), rs.getObject(i));
                        }
                        list.add(map);
                        count += 1;
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}

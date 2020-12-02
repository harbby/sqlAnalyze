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

import com.github.harbby.gadtry.ioc.Autowired;
import com.github.harbby.time2db.MetricDBDao;
import com.github.harbby.time2db.rest.QueryRequest;
import com.github.harbby.time2db.rest.JsonResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryService
{
    private final MetricDBDao metricDbDao;
    private final MetaTableManager metaTable;

    @Autowired
    public QueryService(MetricDBDao metricDbDao, MetaTableManager metaTable)
    {
        this.metricDbDao = metricDbDao;
        this.metaTable = metaTable;
    }

    public JsonResult query(QueryRequest query)
    {
        Map<String, String> stages = getQueryList(query);
        String queryDsl = getQueryJoin(stages, query);
        //1 find cache
        //2 dao exec...
        List<Map<String, Object>> data = metricDbDao.exec(queryDsl);
        JsonResult jsonResult = new JsonResult();
        jsonResult.setData(data);
        jsonResult.setPageNum(query.getPageNum());
        jsonResult.setPageSize(query.getPageSize());
        jsonResult.setStartTime(query.getStartTime());
        jsonResult.setEndTime(query.getStartTime());
        return jsonResult;
    }

    /**
     * 将单个指标查询 拼接起来
     */
    public String getQueryJoin(Map<String, String> stages, QueryRequest query)
    {
        StringBuilder sql = new StringBuilder("");
        int j = 0;    //
        String orderColumn = getOrderColumn(query);
        StringBuilder zd = new StringBuilder(); //sql的头部信息
        for (Map.Entry<String, String> entry : stages.entrySet()) {
            String metric = entry.getKey();
            String tmpSql = entry.getValue();
            if (j == 0) {
                sql.append(" from (").append(tmpSql).append(" order by ").append(metric)
                        .append(" desc,").append(orderColumn).append(" desc ")
                        .append(" limit ").append(query.getPageNum() * query.getPageSize())
                        .append(") as a").append(j).append(" ");
                zd.append("select a0.*");
            }
            else {
                zd.append(",a").append(j).append(".").append(metric);
                sql.append(" left join (").append(tmpSql).append(") as a").append(j)
                        .append(" on true").append(joinOn("a", j, query.getGroup()));
            }
            j++;
        }
        // ---sql 构建完毕
        return zd.append(sql).toString();
    }

    private static String getOrderColumn(QueryRequest query)
    {
        return query.getGroup().split(",")[0];
    }

    public static String joinOn(String tb, int j, String leftjoin)
    {
        StringBuilder out = new StringBuilder();
        for (String i : leftjoin.split(",")) {
            if (i.trim().equals("")) {
                break;
            }
            String on1 = tb + "0." + i;
            String on2 = tb + j + "." + i;
            out.append(" and ("+on1+" is null and "+on2+" is null or "+on1+"="+on2+")");
        }
        return out.toString();
    }

    public String getOneQuery(String metricName, QueryRequest query)
    {
        String templates = "select " + query.getGroup() + ",%s from %s %s group by " + query.getGroup();
        String sumMetric = String.format(" sum(%s) as %s ", metricName, metricName);
        String tableName = metaTable.getTableNameFirst(metricName);
        String filter = " where 1=1 and "+ query.getFilter();
        return String.format(templates, sumMetric, tableName, filter);
    }

    public Map<String, String> getQueryList(QueryRequest query)
    {
        Map<String, String> sqls = new LinkedHashMap<>();
        for (String metricOneId : query.getMetrics().split(",")) {        //---构建sql
            String tmpSql = getOneQuery(metricOneId.toUpperCase(), query);    //获取具体的指标sql
            sqls.put(metricOneId.toUpperCase(), tmpSql);
        }
        return sqls;
    }
}

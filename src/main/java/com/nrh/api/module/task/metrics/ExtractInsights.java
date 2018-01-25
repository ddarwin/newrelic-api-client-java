package com.nrh.api.module.task.metrics;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.Trace;
import com.nrh.api.module.nr.client.InsightsAPI;
import com.nrh.api.module.nr.config.MetricConfig;
import com.nrh.api.module.nr.model.MetricNameModel;

public class ExtractInsights {
  
  private static final Logger log = LoggerFactory.getLogger(ExtractInsights.class);
  
  private static final String NRQL = "SELECT latest(timestamp) FROM {eventType} FACET appId, instanceId, metricFull LIMIT 1000";
  
  private CopierConfig copierConfig;
  private InsightsAPI destInsights;

  public ExtractInsights(CopierConfig copierConfig) {
    this.copierConfig = copierConfig;
    destInsights = new InsightsAPI(copierConfig.getDestKeys());
  }
  
  @Trace
  public Map<String, Date> queryInsights() throws IOException {
    
    // Use the correct eventType
    log.info("About to query Insights");
    String nrqlLive = NRQL.replace("{eventType}", copierConfig.getEventType());
    JSONArray jFacets = runQuery(nrqlLive);
    
    // Loop over each of the facets received
    Map<String, Date> latestMap = new HashMap<>();
    for (int i=0; i < jFacets.length(); i++) {
      JSONObject jFacet = jFacets.getJSONObject(i);
      processFacet(jFacet, latestMap);
    }
    
    log.info("Finished querying Insights, latest maps has " + latestMap.size() + " dates.");
    return latestMap;
  }

  private JSONArray runQuery(String nrqlLive) throws IOException {
    
    // Query Insights
    // log.info(nrqlLive);
    String sResponse = destInsights.querySync(nrqlLive);
    JSONObject jResponse = new JSONObject(sResponse);
    JSONArray jFacets = jResponse.getJSONArray("facets");
    log.info("Received " + jFacets.length() + " facets from Insights");
    return jFacets;
  }

  private void processFacet(JSONObject jFacet, Map<String, Date> latestMap) {
    
    // Get the app and metric names
    MetricNameModel metricNameModel = getMetricFromFacet(jFacet);
    long latest = getLatestFromFacet(jFacet);
    
    // Store in the latestMap
    Date date = new Date(latest);
    if (metricNameModel != null) {
      log.debug("* Insights: " + metricNameModel + " latest is " + date);
      String uniqueId = metricNameModel.getUniqueId();
      latestMap.put(uniqueId, date);
    }
  }

  private MetricNameModel getMetricFromFacet(JSONObject jFacet) {

    // Lookup the app
    JSONArray jName = jFacet.getJSONArray("name");

    // 0 is appId
    // 1 is instanceId
    // 2 is metricName
    Integer appId = 0;
    Integer instanceId = 0;
    String sMetricName = "";

    if (!jName.isNull(0)) {
      appId = jName.getInt(0);
    }
    if (!jName.isNull(1)) {
      instanceId = jName.getInt(1);
    }
    if (!jName.isNull(2)) {
      sMetricName = jName.getString(2);
    }
    
    MetricConfig cfg = new MetricConfig(appId, instanceId, sMetricName);
    MetricNameModel metricNameModel = new MetricNameModel(cfg, sMetricName);
    return metricNameModel;
  }

  private long getLatestFromFacet(JSONObject jFacet) {
    // Get the latest timestamp
    JSONArray jResults = jFacet.getJSONArray("results");
    JSONObject jLatest = jResults.getJSONObject(0);
    long latest = jLatest.getLong("latest");
    return latest;
  }
}

package com.nrh.api.module.nr.dao;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metric {
  private static final Logger log = LoggerFactory.getLogger(Metric.class);

  private String shortName;
  private String fullName;
  private String metricValue;
  private TreeMap<Date, Timeslice> tsMap = new TreeMap<Date, Timeslice>();

  // public Metric(String fullName) {
  //   this.fullName = fullName;
  // }

  public Metric(String fullName, String shortName) {
    this.fullName = fullName;
    this.shortName = shortName;
  }

  public void parseString(String sMetricData) {
    JSONObject jResponse = new JSONObject(sMetricData);
    JSONObject jMetricData = jResponse.getJSONObject("metric_data");
    parseJSON(jMetricData);
  }

  public void parseJSON(JSONObject jMetricData) {
    JSONArray jMetricsFound = jMetricData.getJSONArray("metrics_found");
    if (metricFound(jMetricsFound)) {
      JSONArray jMetricArr = jMetricData.getJSONArray("metrics");
      parseMetricArr(jMetricArr);
    }
  }

  private void parseMetricArr(JSONArray jMetricArr) {
    for (int i=0; i < jMetricArr.length(); i++) {
      JSONObject jMetric = jMetricArr.getJSONObject(i);
      String sName = jMetric.getString("name");
      if (fullName.equals(sName)) {
        JSONArray jTimeslices = jMetric.getJSONArray("timeslices");
        parseTimeslices(jTimeslices);
      }
    }
  }

  private void parseTimeslices(JSONArray jTimeslices) {
    for (int i=0; i < jTimeslices.length(); i++) {
      JSONObject jTimeslice = jTimeslices.getJSONObject(i);
      Timeslice ts = new Timeslice();
      ts.parseJSON(jTimeslice);
      
      // Store this timeslice in the map
      Date to = ts.getTo();
      tsMap.put(to, ts);
    }
  }

  private boolean metricFound(JSONArray jMetricsFound) {
    for (int i=0 ; i < jMetricsFound.length(); i++) {
      if (fullName.equals(jMetricsFound.getString(i))) {
        return true;
      }
    }
    log.debug(fullName + " metric not found.");
    return false;
  }

  /**
   * @return the timeslices
   */
  public Map<Date, Timeslice> getTsMap() {
    return tsMap;
  }

  /**
   * @return the timeslices since that date
   */
  public SortedMap<Date, Timeslice> getTimeslicesSince(Date latest) {
    if (latest == null) {
      latest = new Date(0);
    }
    SortedMap<Date, Timeslice> tailMap = tsMap.tailMap(latest, false);
    log.debug("* " + fullName + " has " + tailMap.size() + " slices since " + latest);
    return tailMap;
  }

  public int getTimesliceSize() {
    return tsMap.size();
  }

  public Timeslice getLastTimeslice() {
    return tsMap.lastEntry().getValue();
  }

  /**
   * @return the shortName if set, fullName otherwise
   */
  // public String getName() {
  //   if (shortName == null) {
  //     return fullName;
  //   }
  //   return shortName;
  // }
  /**
   * @return the fullName
   */
  public String getFullName() {
    return fullName;
  }
  /**
   * @param fullName the fullName to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
  /**
   * @return the shortName
   */
  public String getShortName() {
    return shortName;
  }
  /**
   * @param shortName the shortName to set
   */
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }
  /**
   * @return the metricValue
   */
  public String getMetricValue() {
    return metricValue;
  }
  /**
   * @param metricValue the metricValue to set
   */
  public void setMetricValue(String metricValue) {
    this.metricValue = metricValue;
  }

  public String toString() {
    return shortName + " (" + fullName + ")";
  }
}
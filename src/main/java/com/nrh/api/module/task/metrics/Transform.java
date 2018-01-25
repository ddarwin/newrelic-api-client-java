package com.nrh.api.module.task.metrics;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.nrh.api.module.nr.config.MetricConfig;
import com.nrh.api.module.nr.model.Event;
import com.nrh.api.module.nr.model.MetricDataModel;
import com.nrh.api.module.nr.model.TimesliceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

public class Transform {

  private static final Logger log = LoggerFactory.getLogger(Transform.class);
  
  private CopierConfig config;

  public Transform(CopierConfig config) {
    this.config = config;
  }

  /**
   * Convert the metric data into event data
   */
  @Trace
  public ArrayList<Event> toEvents(Map<String, Date> latestMap, ArrayList<MetricDataModel> metricData) {
    // this.latestMap = latestMap;
    ArrayList<Event> eventList = new ArrayList<Event>();
    for (MetricDataModel model : metricData) {
      String uniqueId = model.getUniqueId();
      Date latest = latestMap.get(uniqueId);
      eventList.addAll(processMetric(latest, model));
    }

    NewRelic.addCustomParameter("metricCopierEvents", eventList.size());
    log.info("Transform complete, we have " + eventList.size() + " events to post");

    return eventList;
  }

  private ArrayList<Event> processMetric(Date latest, MetricDataModel model) {
    // Only process timeslices more recent than the latest date
    SortedMap<Date, TimesliceModel> tsMap = model.getTimeslicesSince(latest);
    
    // Make an event per timeslice
    ArrayList<Event> eventList = new ArrayList<>();
    for (Date date : tsMap.keySet()) {
      TimesliceModel ts = tsMap.get(date);
      Event e = processTimeslice(date, model, ts);
      eventList.add(e);
    }
    return eventList;
  }

  private Event processTimeslice(Date date, MetricDataModel model, TimesliceModel ts) {
    Event e = new Event(config.getEventType());
    e.setTimestamp(date);
    
    // Integer attributes
    MetricConfig cfg = model.getMetricConfig();
    e.addIntAttribute("appId", cfg.getAppId());
    e.addIntAttribute("instanceId", cfg.getInstanceId());

    // String attributes
    e.addStringAttribute("appName", cfg.getAppName());
    e.addStringAttribute("metricShort", model.getShortName());
    e.addStringAttribute("metricFull", model.getName());
    
    // Double precision attributes
    Map<String, Double> valueMap = ts.getValueMap();
    for (String key : valueMap.keySet()) {
      e.addNumericAttribute(key, valueMap.get(key));
    }
    return e;
  }
}
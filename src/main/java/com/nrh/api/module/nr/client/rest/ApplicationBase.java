package com.nrh.api.module.nr.client.rest;

import com.newrelic.api.agent.Trace;
import com.nrh.api.module.nr.client.Util;
import com.nrh.api.module.nr.config.*;
import com.nrh.api.module.nr.model.*;

import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl.Builder;
import okhttp3.*;

public abstract class ApplicationBase {

	private static final Logger log = LoggerFactory.getLogger(ApplicationBase.class);
			
	public static final String URL_HOST = "api.newrelic.com";
	
	private APIKeyset keys;
	private OkHttpClient client;

	public ApplicationBase(APIKeyset keys) {
		this.keys = keys;
		client = new OkHttpClient();
	}
	
	public ArrayList<AppModel> list(ApplicationConfig appConfig, String segment) throws IOException {
		
		// Create the URL with the filters
		Builder urlBuilder = Util.startBuilder(URL_HOST, segment);
		Util.addFilters(urlBuilder, appConfig.getFilterMap());

		// Call the API for this URL
		String sResponse = callAPI(urlBuilder, keys.getRestKey());

		// Parse the response correctly
		return ParserToApp.strToAppList(sResponse, appConfig);
	}

	public AppModel show(ApplicationConfig appConfig, String segment) throws IOException {
		// Create the URL (no parameters)
		Builder urlBuilder = Util.startBuilder(URL_HOST, segment);
		
		// Call the API for this URL
		String sResponse = callAPI(urlBuilder, keys.getRestKey());

		// Parse the response correctly
		return ParserToApp.strToAppModel(sResponse, appConfig);
	}

	public ArrayList<MetricNameModel> metricNames(MetricConfig metricConfig, String segment) throws IOException {
		
		// Create the URL
		Builder urlBuilder = Util.startBuilder(URL_HOST, segment);
		
		// Add the paramter called name if it's defined
		if (metricConfig.getFilterName() != null) {
			urlBuilder.addEncodedQueryParameter("name", metricConfig.getFilterName());
		}

		// Call the API for this URL
		String sResponse = callAPI(urlBuilder, keys.getRestKey());

		// Parse the response correctly
		metricConfig.setMetricType(MetricConfig.TYPE_METRIC_NAME);
		return ParserToMetric.strToMetricNames(sResponse, metricConfig);
	}

	public ArrayList<MetricDataModel> metricData(MetricConfig metricConfig, String segment) throws IOException {
		
		// Create the URL
		Builder urlBuilder = Util.startBuilder(URL_HOST, segment);

		// Add the optional parameters if they are provided
		ArrayList<String> metricNameList = metricConfig.getMetricNameList();
		if (metricNameList != null) {
			for (String metricName : metricNameList ) {
				urlBuilder.addEncodedQueryParameter("names[]", metricName);
			}
		}

		// Call the API for this URL
		String sResponse = callAPI(urlBuilder, keys.getRestKey());

		// Parse the response correctly
		metricConfig.setMetricType(MetricConfig.TYPE_METRIC_DATA);
		return ParserToMetric.strToMetricData(sResponse, metricConfig);
	}

	@Trace
	private String callAPI(Builder urlBuilder, String apiKey) throws IOException {
		// Create the request object and call the API
		Request req = Util.createRequest(urlBuilder, apiKey);
		Response rsp = Util.callSync(client, req);

		// Convert the response into a String
		String sResponse = rsp.body().string();
		log.debug(sResponse);
		return sResponse;
	}
}

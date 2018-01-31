package com.nrh.api.module.nr.client.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import com.nrh.api.APIApplication;
import com.nrh.api.module.nr.config.*;
import com.nrh.api.module.nr.model.*;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRestAPI {

	private static final Logger log = LoggerFactory.getLogger(TestRestAPI.class);
	
	private APIKeyset keys;
	private AppAPI appClient; 
	private AppHostAPI appHostClient;
	private AppInstanceAPI appInstanceClient;
	
	// These values are used by the ordered test cases so they are static
	private static int appId;
	private static int hostId;
	private static int instanceId;

	private static AppConfig appConfig;
	private static MetricConfig metricConfig;
	private static int metricCount;
	
	@Before
	public void setUp() throws Exception {
		
		// Read in the config files
		APIApplication.readConfig();
		// log.info("Config file used: " + APIApplication.getConfig().origin());

		// Get the name of the unitTestAccount
		String unitTestAccount = APIApplication.getConfString("newrelic-api-client.tests.unitTestAccount");
		keys = APIApplication.getAPIKeyset(unitTestAccount);
		log.info("Application API Test using keyset for account: " + keys.getAccountName());
		
		// Initialize the Applications API
		appClient = new AppAPI(keys);
		appHostClient = new AppHostAPI(keys);
		appInstanceClient = new AppInstanceAPI(keys);

		// Initialize an empty config (the tests will fill it in)
		appConfig = new AppConfig();
	}

	@Test
	public void app1List() throws IOException {
		ArrayList<AppModel> appList = appClient.list(appConfig);
		log.info("Number of applications: " + appList.size());
		
		assertNotEquals(0, appList.size());
		
		// Grab the first app that is "reporting"
		for (AppModel appModel : appList) {
			if (appModel.getReporting()) {
				appId = appModel.getAppId();
				String appName = appModel.getAppName();
				
				// Create the new config
				metricConfig = new MetricConfig();
				metricConfig.setAppId(appId);
				metricConfig.setAppName(appName);
				log.info("Setting appId to this reporting app: " + appName + " (" + appId + ")");
				break;
			}
		}
	}
	
	@Test
	public void app2Show() throws IOException {
		appConfig.setAppId(appId);
		log.info("appShow(" + appConfig.getAppId() + ")");
		AppModel appModel = appClient.show(appConfig);
		assertEquals(appConfig.getAppId(), appModel.getAppId());
	}

	@Test
	public void app3MetricNames() throws IOException {
		
		ArrayList<MetricNameModel> metricNameList = appClient.metricNames(metricConfig);

		// There should be more than 0 metrics
		assertNotEquals(0, metricNameList.size());

		// Grab a group of metrics
		int i = 0;
		for ( ; i < metricNameList.size(); i++) {
			// Don't get more than 5 metrics
			if (i == 5) {
				break;
			}
			
			MetricNameModel metricNameModel = metricNameList.get(i);
			String fullName = metricNameModel.getFullName();

			// Add metrics to the list unless they start with Instance
			// String fullName = jMetrics.getJSONObject(i).getString("name");
			if (!fullName.startsWith("Instance")) {
				metricConfig.addMetricName(fullName, fullName);
				metricCount++;
			}
		}
	}

	@Test
	public void app4MetricData() throws IOException {

		ArrayList<MetricDataModel> metricDataList = appClient.metricData(metricConfig);
		
		// Check that the correct number of metrics were found
		assertEquals(metricCount, metricDataList.size());
	}

	@Test
	public void appHost1List() throws IOException {
		appConfig.setAppId(appId);
		log.info("appHost1List(" + appConfig.getAppId() + ")");
		ArrayList<AppHostModel> hostList = appHostClient.list(appConfig);
		log.info("Number of hosts: " + hostList.size());

		// Get the hostId from the first host
		AppHostModel appHostModel = hostList.get(0);
		hostId = appHostModel.getHostId();
		
		assertNotEquals(0, hostList.size());
	}

	@Test
	public void appHost2ShowOne() throws IOException {
		appConfig.setAppId(appId);
		appConfig.setHostId(hostId);

		log.info("appHost2Show(" + appId + ", " + hostId + ")");
		AppHostModel appModel = appHostClient.show(appConfig);
		assertEquals(appConfig.getHostId(), appModel.getHostId());
	}

	@Test
	public void appHost4MetricData() throws IOException {
		// Start with the list call to get a list of hosts
		appConfig.setAppId(appId);
		log.info("First call appHostClient.list(" + appId + ")");
		ArrayList<AppHostModel> hostList = appHostClient.list(appConfig);

		// Loop over all the hosts
		for(AppHostModel hostModel : hostList) {
			Integer hostId = hostModel.getHostId();
			metricConfig.setHostId(hostId);
			log.info("Then call appHostClient.metricData(" + appId + ", " + hostId + ")");
			ArrayList<MetricDataModel> dataModel = appHostClient.metricData(metricConfig);
			
			// Check that we got all the metrics
			assertEquals(metricCount, dataModel.size());
		}
	}

	@Test
	public void appInstance1List() throws IOException {
		appConfig.setAppId(appId);
		log.info("appInstance1List(" + appConfig.getAppId() + ")");
		ArrayList<AppInstanceModel> instanceList = appInstanceClient.list(appConfig);
		log.info("Number of instances: " + instanceList.size());
		
		// Get the instanceId from the first host
		AppInstanceModel appHostModel = instanceList.get(0);
		instanceId = appHostModel.getInstanceId();

		assertNotEquals(0, instanceList.size());
	}

	@Test
	public void appInstance2ShowOne() throws IOException {
		appConfig.setAppId(appId);
		appConfig.setInstanceId(instanceId);

		log.info("appInstance2Show(" + appId + ", " + hostId + ")");
		AppInstanceModel appModel = appInstanceClient.show(appConfig);
		assertEquals(appConfig.getInstanceId(), appModel.getInstanceId());
	}

	@Test
	public void appInstance4MetricData() throws IOException {
		// Start with the list call to get a list of instances
		appConfig.setAppId(appId);
		log.info("First call appInstanceClient.list(" + appId + ")");
		ArrayList<AppInstanceModel> instanceList = appInstanceClient.list(appConfig);

		// Loop over all the instances
		for(AppInstanceModel instanceModel : instanceList) {
			Integer instanceId = instanceModel.getInstanceId();
			metricConfig.setInstanceId(instanceId);
			log.info("Then call appInstanceClient.metricData(" + appId + ", " + instanceId + ")");
			ArrayList<MetricDataModel> dataModel = appInstanceClient.metricData(metricConfig);
			
			// Check that we got all the metrics
			assertEquals(metricCount, dataModel.size());
		}
	}
}

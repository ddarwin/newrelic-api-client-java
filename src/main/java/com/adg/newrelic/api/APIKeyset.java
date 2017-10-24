package com.adg.newrelic.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

/**
 * The API Keyset class holds a set of keys for a single account
 * 
 * @author kahrens
 *
 */
public class APIKeyset {
	private String accountName;
	private String adminName;
	private String accountId;
	private String restKey;
	private String adminKey;
	private String insightsQueryKey;
	private String insightsInsertKey;
	
	/**
	 * Populate 
	 * 
	 * @param conf the config for the whole API project
	 * @param accountName the name of the account to pull from the config
	 */
	public APIKeyset(Config conf, String accountName) {
		
		// Set all the local values from the config
		String prefix = "newrelic-api-client.accounts." + accountName;
		this.accountName = conf.getString(prefix + ".accountName");
		this.adminName = conf.getString(prefix + ".adminName");
		this.accountId = conf.getString(prefix + ".accountId");
		this.restKey = conf.getString(prefix + ".restKey");
		this.adminKey = conf.getString(prefix + ".adminKey");
		this.insightsQueryKey = conf.getString(prefix + ".insightsQueryKey");
		this.insightsInsertKey = conf.getString(prefix + ".insightsInsertKey");
	}
	
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getRestKey() {
		return restKey;
	}
	public void setRestKey(String restKey) {
		this.restKey = restKey;
	}
	public String getAdminKey() {
		return adminKey;
	}
	public void setAdminKey(String adminKey) {
		this.adminKey = adminKey;
	}
	public String getInsightsQueryKey() {
		return insightsQueryKey;
	}
	public void setInsightsQueryKey(String insightsQueryKey) {
		this.insightsQueryKey = insightsQueryKey;
	}
	public String getInsightsInsertKey() {
		return insightsInsertKey;
	}
	public void setInsightsInsertKey(String insightsInsertKey) {
		this.insightsInsertKey = insightsInsertKey;
	}
}
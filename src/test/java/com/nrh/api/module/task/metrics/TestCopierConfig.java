package com.nrh.api.module.task.metrics;

import static org.junit.Assert.*;

import com.nrh.api.module.nr.config.MetricConfig;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TestCopierConfig {
  
  @Test
  public void testCopierConfig() {
    
    // This test will make sure the config is created and doesn't return a null
    CopierConfig copierConfig = new CopierConfig();
    ArrayList<MetricConfig> cfgList = copierConfig.getCfgList();
    assertNotNull(cfgList);
  }
}
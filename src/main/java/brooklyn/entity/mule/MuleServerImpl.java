/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package brooklyn.entity.mule;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;

import org.osgi.jmx.JmxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.java.JavaAppUtils;
import brooklyn.entity.webapp.JavaWebAppSoftwareProcessImpl;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.feed.jmx.JmxAttributePollConfig;
import brooklyn.event.feed.jmx.JmxFeed;
import brooklyn.event.feed.jmx.JmxValueFunctions;
import brooklyn.util.collections.MutableMap;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;

/**
 * An {@link brooklyn.entity.Entity} that represents a single Mule instance.
 */
public class MuleServerImpl extends JavaWebAppSoftwareProcessImpl implements MuleServer {

    private static final Logger LOG = LoggerFactory.getLogger(MuleServerImpl.class);
    
    private BasicAttributeSensor<Map> mapAttribute = new BasicAttributeSensor<Map>(Map.class, "brooklyn.test.mapAttribute", "Brooklyn testing map attribute");

    public MuleServerImpl() {
        super();
    }

    private volatile JmxFeed jmxHeapMemory;

    @Override
    public void connectSensors() {
        super.connectSensors();
        if (getDriver().isJmxEnabled()) {

        	jmxHeapMemory = JmxFeed.builder()
        			.entity(this)
        			.period(3000, TimeUnit.MILLISECONDS)
        			.pollAttribute(new JmxAttributePollConfig<Map>(mapAttribute)
        					.objectName("java.lang:type=Memory")
        					.attributeName("HeapMemoryUsage")
        					.onSuccess((Function) JmxValueFunctions.compositeDataToMap()))
        					.build();
        } else {
            // if not using JMX
            LOG.warn("Tomcat running without JMX monitoring; limited visibility of service available");
//            connectServiceUpIsRunning();
        }
        connectServiceUpIsRunning();
    }

    @Override
    public void disconnectSensors() {
        super.disconnectSensors();
        if (getDriver() != null && getDriver().isJmxEnabled()) {
           if (jmxHeapMemory != null) jmxHeapMemory.stop();
        }
//        else {
//            disconnectServiceUpIsRunning();
//        }
            disconnectServiceUpIsRunning();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getDriverInterface() {
        return MuleDriver.class;
    }
    
    @Override
    public String getShortName() {
        return "Mule";
    }
}


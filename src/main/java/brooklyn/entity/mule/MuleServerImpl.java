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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.feed.jmx.JmxAttributePollConfig;
import brooklyn.event.feed.jmx.JmxFeed;
import brooklyn.event.feed.jmx.JmxValueFunctions;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * An {@link brooklyn.entity.Entity} that represents a single Mule instance.
 */
public class MuleServerImpl extends SoftwareProcessImpl implements MuleServer {

    private static final Logger LOG = LoggerFactory.getLogger(MuleServerImpl.class);
    
    private BasicAttributeSensor<Map> heapMemoryUsageAttrSensor = new BasicAttributeSensor<Map>(Map.class, "heap.memory.usage.mapAttribute", "Heap memory usage map attribute");

    public MuleServerImpl() {
        super();
    }
    
    public MuleDriver getDriver() {
        return (MuleDriver) super.getDriver();
    }

    private volatile JmxFeed jmxHeapMemoryUsageFeed;

    @Override
    public void connectSensors() {
        super.connectSensors();
        // currently, JMX is always enabled
        if (getDriver().isJmxEnabled()) {

        	jmxHeapMemoryUsageFeed = JmxFeed.builder()
        			.entity(this)
        			.period(3000, TimeUnit.MILLISECONDS)
        			.pollAttribute(new JmxAttributePollConfig<Map>(heapMemoryUsageAttrSensor)
        					.objectName("java.lang:type=Memory")
        					.attributeName("HeapMemoryUsage")
        					.onSuccess((Function) JmxValueFunctions.compositeDataToMap()))
        					.build();
        } else {
            LOG.warn("Mule running without JMX monitoring");
        }
        connectServiceUpIsRunning();
    }

    @Override
    public void disconnectSensors() {
        super.disconnectSensors();
        if (getDriver() != null && getDriver().isJmxEnabled()) {
           if (jmxHeapMemoryUsageFeed != null) jmxHeapMemoryUsageFeed.stop();
        }
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

	@Override
	@Effector(description="Deploys the given packaged Mule app from a source URL. Uses targetName as app name in $MULE_HOME/apps")
	public void deploy(
			@EffectorParam(name="url", description="URL of packaged Mule app") String url, 
			@EffectorParam(name="targetName", description="Name to use for directory in $MULE_HOME/apps") String targetName) {
		try {
			checkNotNull(url, "url");
			checkNotNull(targetName, "url");
			MuleDriver driver = getDriver();
			String deployedName = driver.deploy(url, targetName);
            // Update attribute
            Set<String> deployedApps = getAttribute(DEPLOYED_APPS);
            if (deployedApps == null) {
                deployedApps = Sets.newLinkedHashSet();
            }
            deployedApps.add(deployedName);
            setAttribute(DEPLOYED_APPS, deployedApps);
		} catch (RuntimeException e) {
            // Log and propagate, so that log says which entity had problems...
            LOG.warn("Error deploying '"+url+"' as "+targetName+" on "+toString()+"; rethrowing...", e);
            throw Throwables.propagate(e);
		}
	}

	@Override
    @Effector(description="Undeploys the given Mule app by name in $MULE_HOME/apps")
	public void undeploy(
			@EffectorParam(name="targetName", description="Name which identifies app to undeploy") String targetName) {
		try {
			MuleDriver driver = getDriver();
			driver.undeploy(targetName);
            // Update attribute
            Set<String> deployedApps = getAttribute(DEPLOYED_APPS);
            if (deployedApps == null) {
                deployedApps = Sets.newLinkedHashSet();
            }
            deployedApps.remove(targetName);
            setAttribute(DEPLOYED_APPS, deployedApps);
		} catch (RuntimeException e) {
            LOG.warn("Error undeploying '"+targetName+"' on "+toString()+"; rethrowing...", e);
            throw Throwables.propagate(e);
		}
	}

}


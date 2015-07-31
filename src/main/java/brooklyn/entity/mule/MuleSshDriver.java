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

import static java.lang.String.format;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.basic.Entities;
import brooklyn.entity.java.JavaSoftwareProcessSshDriver;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;

import com.google.common.collect.ImmutableList;

public class MuleSshDriver extends JavaSoftwareProcessSshDriver implements MuleDriver {

    private static final Logger LOG = LoggerFactory.getLogger(MuleSshDriver.class);

    public MuleSshDriver(MuleServerImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void preInstall() {
        resolver = Entities.newDownloader(this);
        String installDir = getInstallDir();
        String unpackedDirName = resolver.getUnpackedDirectoryName("mule-standalone-" + getVersion());
        setExpandedInstallDir(Os.mergePaths(installDir, unpackedDirName));
    }

    @Override
    public void install() {
        List<String> urls = resolver.getTargets();
        String saveAs = resolver.getFilename();

        List<String> commands = new LinkedList<String>();
        commands.addAll(BashCommands.commandsToDownloadUrlsAs(urls, saveAs));
        commands.add(BashCommands.INSTALL_TAR);
        commands.add(format("tar xvzf %s", saveAs));

        newScript(INSTALLING).environmentVariablesReset().body.append(commands).execute();
    }

    @Override
    public void customize() {
        LOG.info("in customize - nothing to do");
    }

    @Override
    public void launch() {
        newScript(MutableMap.of(USE_PID_FILE, false), LAUNCHING).body
                .append(format("nohup ./bin/mule -M-Dcom.sun.management.jmxremote=true -M-Dcom.sun.management.jmxremote.port=1098 -M-Dcom.sun.management.jmxremote.authenticate=false -M-Dcom.sun.management.jmxremote.ssl=false >> console.log 2>&1 </dev/null &"))
                .execute();
    }

    @Override
    public boolean isRunning() {
        int res = newScript(MutableMap.of(USE_PID_FILE, false), CHECK_RUNNING).body.append(format("bin/mule status")).execute();
        return res == 0;
    }

    @Override
    public void stop() {
        newScript(MutableMap.of(USE_PID_FILE, false), STOPPING).body.append(format("bin/mule stop")).execute();
    }

    @Override
    protected String getLogFileLocation() {
        return Os.mergePathsUnix(getRunDir(), "logs/mule.log");
    }

    protected String getDeployDir() {
        return getRunDir() + "/apps";
    }

    @Override
    public String deploy(String url, String targetName) {
        String dest = String.format("%s/%s.zip", getDeployDir(), targetName);
        int result = copyResource(url, dest);
        log.debug("{} deployed {} to {}:{}: result {}", new Object[] { entity, url, getHostname(), dest, result });
        if (result != 0) {
            log.warn("Problem deploying {} to {}:{} for {}: result {}",
                    new Object[] { url, getHostname(), dest, entity, result });
            throw new IllegalStateException(String.format("Failed to copy %s to %s", url, dest));
        } else if (log.isDebugEnabled()) {
            log.debug("{} deployed {}:{}: result {}", new Object[] { entity, getHostname(), dest, result });
        }

        return result == 0 ? targetName : "";
    }

    @Override
    public void undeploy(String targetName) {
        String dest = String.format("%s/%s-anchor.txt", getDeployDir(), targetName);
        log.info("{} undeploying {}:{}", new Object[] { entity, getHostname(), dest });
        int result = getMachine().execCommands("removing anchor file on undeploy",
                ImmutableList.of(String.format("rm %s", dest)));
        if (result != 0) {
            log.warn("Problem undeploying {} from {}:{} for {}: result {}", new Object[] { targetName, getHostname(), dest,
                    entity, result });
            throw new IllegalStateException("Failed to delete " + dest);
        } else if (log.isDebugEnabled()) {
            log.debug("{} undeployed {}:{}: result {}", new Object[] { entity, getHostname(), dest, result });
        }
    }

}

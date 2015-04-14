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
import brooklyn.entity.webapp.JavaWebAppSshDriver;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;

public class MuleSshDriver extends JavaWebAppSshDriver implements MuleDriver {

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

        newScript(INSTALLING)
                .environmentVariablesReset()
                .body.append(commands)
                .execute();
    }

    @Override
    public void customize() {
    	// deploy initial apps?
    	LOG.info("in customize - nothing to do");

    	/* 
        newScript(CUSTOMIZING)
                .body.append("mkdir -p conf logs webapps temp")
                .failOnNonZeroResultCode()
                .execute();

        copyTemplate(entity.getConfig(TomcatServer.SERVER_XML_RESOURCE), Os.mergePaths(getRunDir(), "conf", "server.xml"));
        copyTemplate(entity.getConfig(TomcatServer.WEB_XML_RESOURCE), Os.mergePaths(getRunDir(), "conf", "web.xml"));

        // Deduplicate same code in JBoss
        if (isProtocolEnabled("HTTPS")) {
            String keystoreUrl = Preconditions.checkNotNull(getSslKeystoreUrl(), "keystore URL must be specified if using HTTPS for " + entity);
            String destinationSslKeystoreFile = getHttpsSslKeystoreFile();
            InputStream keystoreStream = resource.getResourceFromUrl(keystoreUrl);
            getMachine().copyTo(keystoreStream, destinationSslKeystoreFile);
        }

        getEntity().deployInitialWars();
        */
    }

    @Override
    public void launch() {
        newScript(MutableMap.of(USE_PID_FILE, false), LAUNCHING)
                .body.append(
                        format("cd mule-standalone-3.6.1 && nohup ./bin/mule -M-Dcom.sun.management.jmxremote=true -M-Dcom.sun.management.jmxremote.port=1098 -M-Dcom.sun.management.jmxremote.authenticate=false -M-Dcom.sun.management.jmxremote.ssl=false >> console.log 2>&1 </dev/null &")
                    )
                .execute();
    }
    
    @Override
    public boolean isRunning() {
    	int res = newScript(MutableMap.of(USE_PID_FILE, false), CHECK_RUNNING)
//    		.body.append(format("cd mule-standalone-3.6.1 && ./bin/mule status")).execute(); // == 0;
    		.body.append(format("mule-standalone-3.6.1/bin/mule status")).execute(); // == 0;
    	System.out.println("isRunning(), result of script is: " + res);
    	return res == 0;
    }

    @Override
    public void stop() {
    	newScript(MutableMap.of(USE_PID_FILE, false), STOPPING)
    		.body.append(format("mule-standalone-3.6.1/bin/mule stop")).execute();
//    		.body.append(format("cd mule-standalone-3.6.1 && ./bin/mule stop")).execute();
    }

    @Override
    protected String getLogFileLocation() {
        return Os.mergePathsUnix(getRunDir(), "mule-standalone-3.6.1/logs/mule.log");
    }

    @Override
    protected String getDeploySubdir() {
       return "apps";
    }

}

package brooklyn.entity.mule;

import java.util.Set;

import brooklyn.catalog.Catalog;
import brooklyn.config.ConfigKey;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.java.UsesJmx;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.entity.trait.HasShortName;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.util.flags.SetFromFlag;

/**
 * An {@link brooklyn.entity.Entity} that represents a single Mule instance.
 */
@Catalog(name = "Mule Server", description = "Mule ESB is a Java-based integration platform that allows developers to connect applications together quickly and easily")
@ImplementedBy(MuleServerImpl.class)
public interface MuleServer extends SoftwareProcess, UsesJmx, HasShortName {

	@SetFromFlag("version")
	ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(
			SoftwareProcess.SUGGESTED_VERSION, "3.6.1");

	@SetFromFlag("downloadUrl")
	BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey<String>(
			SoftwareProcess.DOWNLOAD_URL,
			"https://repository-master.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/${version}/mule-standalone-${version}.tar.gz");

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final AttributeSensor<Set<String>> DEPLOYED_APPS = new BasicAttributeSensor(
            Set.class, "mule.deployedApps", "Names of apps that are currently in $MULE_HOME/apps");
	
    /**
     * Deploys a URL as a Mule app at the Mule standalone server.
     * 
     * targetName is the name of the app copied over to $MULE_HOME/apps
     *
     * If deployment of targetName is successful, targetName is returned.
     * This targetName can be used as an argument to undeploy.
     * If deployment is unsuccessful, an empty String is returned.
     * 
	 * @param url
	 *            where to get the zip file, as a URL, either classpath://xxx or
	 *            file:///home/xxx or http(s)...
	 * @param targetName
	 *            the name to use when moving this zip file to $MULE_HOME/apps
     */
	@Effector(description = "Deploys the given artifact, from a source URL, to a given deployment filename/context")
	public void deploy(
			@EffectorParam(name = "url", description = "URL of WAR file") String url,
			@EffectorParam(name = "targetName", description = "context path where WAR should be deployed (/ for ROOT)") String targetName);

	/**
	 * For the DEPLOYED_WARS to be updated, the input must match the result of
	 * the call to deploy, e.g. the transformed name using
	 */
	@Effector(description = "Undeploys the given context/artifact")
	public void undeploy(@EffectorParam(name = "targetName") String targetName);

}

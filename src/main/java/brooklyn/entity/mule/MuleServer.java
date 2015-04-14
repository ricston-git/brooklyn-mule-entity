package brooklyn.entity.mule;

import brooklyn.catalog.Catalog;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.java.UsesJmx;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.entity.trait.HasShortName;
import brooklyn.entity.webapp.JavaWebAppSoftwareProcess;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.util.flags.SetFromFlag;

/**
 * An {@link brooklyn.entity.Entity} that represents a single Mule instance.
 */
@Catalog(name = "Mule Server", description = "todo")
@ImplementedBy(MuleServerImpl.class)
public interface MuleServer extends JavaWebAppSoftwareProcess, UsesJmx, HasShortName {

	@SetFromFlag("version")
	ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(
			SoftwareProcess.SUGGESTED_VERSION, "3.6.1");

	@SetFromFlag("downloadUrl")
	BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey<String>(
			SoftwareProcess.DOWNLOAD_URL,
			"https://repository-master.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/3.6.1/mule-standalone-3.6.1.tar.gz");
//			"https://repository-master.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/${version}/mule-standalone-${version}.tar.gz");

	AttributeSensor<String> JMX_SERVICE_URL = UsesJmx.JMX_URL;

}

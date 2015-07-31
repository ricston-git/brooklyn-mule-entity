package brooklyn.entity.mule.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.java.UsesJmx;
import brooklyn.entity.mule.MuleServer;
import brooklyn.entity.proxying.EntitySpec;

public class MuleServerApp extends AbstractApplication {

    public static final Logger LOG = LoggerFactory.getLogger(MuleServerApp.class);

    @Override
    public void init() {
        addChild(EntitySpec.create(MuleServer.class).configure(SoftwareProcess.INSTALL_DIR, "~/mule-home")
                .configure(SoftwareProcess.RUN_DIR, "~/mule-home/mule-standalone-3.6.1")
                .configure(UsesJmx.JMX_URL.getName(), "service:jmx:rmi:///jndi/rmi://localhost:1098/jmxrmi")
                .configure(UsesJmx.RMI_REGISTRY_PORT.getName(), 1098));
    }

}

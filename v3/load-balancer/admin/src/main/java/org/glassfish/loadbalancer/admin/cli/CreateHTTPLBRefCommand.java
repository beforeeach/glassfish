/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.loadbalancer.admin.cli;

import java.util.logging.Logger;
import java.util.List;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.*;
import org.glassfish.api.Param;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.internal.api.Target;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.LbConfigs;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.LoadBalancer;
import com.sun.enterprise.config.serverbeans.Applications;

import org.glassfish.api.admin.*;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;

/**
 *
 * This is a remote command that supports the create-http-lb-ref CLI command.
 * It creates a server-ref|cluster-ref, health-checker by using the given
 * parameters.
 * lbname: the name of the load-balancer element that exists
 * config: the name of the lb-config element that exists
 * target: cluster-ref or server-ref parameter of lb-config *
 * healthcheckerurl: url attribute of health-checker
 * healthcheckerinterval: interval-in-seconds parameter of health-checker
 * healthcheckertimeout: timeout-in-seconds parameter of health-checker
 * @author Yamini K B
 */
@Service(name = "create-http-lb-ref")
@Scoped(PerLookup.class)
@I18n("create.http.lb.ref")
@TargetType(value={CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@org.glassfish.api.admin.Cluster(RuntimeType.DAS)
public final class CreateHTTPLBRefCommand extends LBCommandsBase
        implements AdminCommand {

    @Param(optional=true)
    String config;

    @Param(optional=true)
    String lbname;

    @Param(optional=true)
    String lbpolicy;

    @Param(optional=true)
    String lbpolicymodule;

    @Param(optional=true, defaultValue="/")
    String healthcheckerurl;

    @Param(optional=true, defaultValue="30")
    String healthcheckerinterval;

    @Param(optional=true, defaultValue="10")
    String healthcheckertimeout;

    @Param(optional=true)
    String lbenableallapplications;

    @Param(optional=true)
    String lbenableallinstances;

    @Param(optional=true)
    String lbweight;

    @Param(primary=true)
    String target;

    @Inject
    LbConfigs lbconfigs;

    @Inject
    Target tgt;

    @Inject
    Logger logger;

    @Inject
    CommandRunner runner;

    @Inject
    Applications applications;

    private ActionReport report;

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateHTTPLBRefCommand.class);
    
    @Override
    public void execute(AdminCommandContext context) {

        report = context.getActionReport();

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        boolean isCluster = (target!=null) ? tgt.isCluster(target): false;

        if (config!=null && lbname!=null) {
            String msg = localStrings.getLocalString("EitherConfigOrLBName",
                    "Either LB name or LB config name, not both");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (config != null) {
            if (lbconfigs.getLbConfig(config) == null) {
                String msg = localStrings.getLocalString("LbConfigDoesNotExist",
                        "Specified LB config {0} does not exist", config);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        } else if (lbname != null) {
            LoadBalancer lb = domain.getLoadBalancers().getLoadBalancer(lbname);
            config = lb.getLbConfigName();
        }

        if((lbpolicy != null) || (lbpolicymodule != null)) {
            if (!isCluster) {
                String msg = localStrings.getLocalString("NotCluster",
                        "{0} not a cluster", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        }
        
        Cluster c = null;
        Server s = null;
        if (isCluster) {
            c = domain.getClusterNamed(target);
            if (c == null ) {
                String msg = localStrings.getLocalString("ClusterNotDefined",
                        "Cluster {0} cannot be used as target", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        } else {
            s = domain.getServerNamed(target);
            if (s == null ) {
                String msg = localStrings.getLocalString("ServerNotDefined",
                        "Server {0} cannot be used as target", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
        }

        // create lb ref
        createLBRef(target, config);
        if(healthcheckerurl != null ){
            try {
                final CreateHTTPHealthCheckerCommand command =
                        (CreateHTTPHealthCheckerCommand) runner
                        .getCommand("create-http-health-checker", report, context.getLogger());
                command.url = healthcheckerurl;
                command.interval=healthcheckerinterval;
                command.timeout=healthcheckertimeout;
                command.config=config;
                command.target=target;
                command.execute(context);
                checkCommandStatus(context);
            } catch (CommandException e) {
                String msg = e.getLocalizedMessage();
                logger.warning(msg);
//                    report.setActionExitCode(ExitCode.FAILURE);
//                    report.setMessage(msg);
//                    return;
            }
        }
        if(Boolean.getBoolean(lbenableallinstances)) {
            try {
                final EnableHTTPLBServerCommand command = (EnableHTTPLBServerCommand)runner
                        .getCommand("enable-http-lb-server", report, context.getLogger());
                command.target = target;
                command.execute(context);
                checkCommandStatus(context);
            } catch (CommandException e) {
                String msg = e.getLocalizedMessage();
                logger.warning(msg);
//                    report.setActionExitCode(ExitCode.FAILURE);
//                    report.setMessage(msg);
//                    return;
            }

        }
        if(Boolean.getBoolean(lbenableallapplications) || lbpolicy != null ||
                lbpolicymodule != null ) {
            List<ApplicationRef> appRefs = domain.getApplicationRefsInTarget(target);
            LbConfig lbConfig = lbconfigs.getLbConfig(config);

            if ((appRefs.size() > 0) && Boolean.getBoolean(lbenableallapplications)) {
                for(ApplicationRef ref:appRefs) {
                    //enable only user applications
                    if(isUserApp(ref.getRef())) {
                        enableApp(context, ref.getRef());
                    }
                }
            }
            ClusterRef cRef = lbConfig.getRefByRef(ClusterRef.class, target);
            updateClusterRef(cRef);
        }
    }

    public void createLBRef(String target, String configName) {
        logger.fine("[LB-ADMIN] createLBRef called for target " + target);

            // target is a cluster
            if (tgt.isCluster(target)) {
                addClusterToLbConfig(configName, target);
                logger.info(localStrings.getLocalString("http_lb_admin.AddClusterToConfig",
                        "Added cluster {0} to load balancer {1}", target, configName));


            // target is a server
            } else if (domain.isServer(target)) {
                addServerToLBConfig(configName, target);
                logger.info(localStrings.getLocalString("http_lb_admin.AddServerToConfig",
                        "Added server {0} to load balancer {1}", target, configName));

            } else {
                String msg = localStrings.getLocalString("InvalidTarget", "Invalid target", target);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }
    }

    private void addServerToLBConfig(final String configName, final String serverName) {
        LbConfig lbConfig = lbconfigs.getLbConfig(configName);

        ServerRef sRef = lbConfig.getRefByRef(ServerRef.class, serverName);
        if (sRef != null) {
            // already exists
            return;
        }

        Server server = domain.getServerNamed(serverName);
        boolean isStandAlone = server.getCluster() == null && server.isInstance();
        if (!isStandAlone) {
            String msg = localStrings.getLocalString("NotStandAloneInstance",
                    "[{0}] is not a stand alone instance. Only stand alone instance can be added to a load balancer.",
                    serverName);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<LbConfig>() {
                    @Override
                    public Object run(LbConfig param) throws PropertyVetoException, TransactionFailure {
                        ServerRef ref = param.createChild(ServerRef.class);
                        ref.setRef(serverName);
                        param.getClusterRefOrServerRef().add(ref);
                        return Boolean.TRUE;
                    }
            }, lbConfig);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToAddServerRef",
                    "Failed to add server-ref");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }

    }

    private void addClusterToLbConfig(final String configName, final String clusterName) {
        LbConfig lbConfig = lbconfigs.getLbConfig(configName);

        ClusterRef cRef = lbConfig.getRefByRef(ClusterRef.class, clusterName);
        if (cRef != null) {
            // already exists
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<LbConfig>() {
                    @Override
                    public Object run(LbConfig param) throws PropertyVetoException, TransactionFailure {
                        ClusterRef ref = param.createChild(ClusterRef.class);
                        ref.setRef(clusterName);
                        param.getClusterRefOrServerRef().add(ref);
                        return Boolean.TRUE;
                    }
            }, lbConfig);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToAddClusterRef",
                    "Failed to add cluster-ref");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }
    }

    private void enableApp(AdminCommandContext context, String appName) {
        try {
            final EnableHTTPLBApplicationCommand command =
                    (EnableHTTPLBApplicationCommand) runner
                    .getCommand("enable-http-lb-application", report, context.getLogger());
            command.target = target;
            command.name=appName;
            command.execute(context);
            checkCommandStatus(context);
        } catch (CommandException e) {
            String msg = e.getLocalizedMessage();
            logger.warning(msg);
//            report.setActionExitCode(ExitCode.FAILURE);
//            report.setMessage(msg);
//            return;
        }
    }

    public void updateClusterRef(final ClusterRef ref) {
        try {
            ConfigSupport.apply(new SingleConfigCode<ClusterRef>() {
                    @Override
                    public Object run(ClusterRef param) throws PropertyVetoException, TransactionFailure {
                        if(lbpolicy != null) {
                            param.setLbPolicy(lbpolicy);
                        }
                        if(lbpolicymodule != null) {
                            param.setLbPolicyModule(lbpolicymodule);
                        }

                        return Boolean.TRUE;
                    }
            }, ref);
        } catch (TransactionFailure ex) {
            String msg = localStrings.getLocalString("FailedToUpdateClusterRef",
                    "Failed to update cluster-ref");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setFailureCause(ex);
            return;
        }
    }    

    private boolean isUserApp(String id) {
        if(applications.getApplication(id).getObjectType().equals("user")) {
            return true;
        }
        return false;
    }
}

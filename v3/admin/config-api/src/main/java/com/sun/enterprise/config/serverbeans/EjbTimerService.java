/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;

import org.glassfish.quality.ToDo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.EJBTimerServiceConfig", singleton=true)
@Configured
public interface EjbTimerService extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the minimumDeliveryIntervalInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="7000")
    @Min(value=1)
    String getMinimumDeliveryIntervalInMillis();

    /**
     * Sets the value of the minimumDeliveryIntervalInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMinimumDeliveryIntervalInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxRedeliveries property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1")
    @Min(value=1)
    String getMaxRedeliveries();

    /**
     * Sets the value of the maxRedeliveries property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMaxRedeliveries(String value) throws PropertyVetoException;

    /**
     * Gets the value of the timerDatasource property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getTimerDatasource();

    /**
     * Sets the value of the timerDatasource property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setTimerDatasource(String value) throws PropertyVetoException;

    /**
     * Gets the value of the redeliveryIntervalInternalInMillis property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="5000")
    @Min(value=1)
    String getRedeliveryIntervalInternalInMillis();

    /**
     * Sets the value of the redeliveryIntervalInternalInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRedeliveryIntervalInternalInMillis(String value) throws PropertyVetoException;
    
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}

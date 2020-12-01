package org.apache.maven.model.converter.plugins;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.model.ReportSet;
import org.apache.maven.model.converter.ProjectConverterException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A <code>PluginConfigurationConverter</code> for the maven-changes-plugin.
 *
 * @plexus.component role="org.apache.maven.model.converter.plugins.PluginConfigurationConverter" role-hint="changes"
 *
 * @author Dennis Lundberg
 * @version $Id$
 */
public class PCCChanges
    extends AbstractPluginConfigurationConverter
{
    /**
     * @see org.apache.maven.model.converter.plugins.AbstractPluginConfigurationConverter#getArtifactId()
     */
    public String getArtifactId()
    {
        return "maven-changes-plugin";
    }

    public String getType()
    {
        return TYPE_REPORT_PLUGIN;
    }

    protected void buildConfiguration( Xpp3Dom configuration, org.apache.maven.model.v3_0_0.Model v3Model,
                                       Properties projectProperties )
        throws ProjectConverterException
    {
        addConfigurationChild( configuration, projectProperties, "maven.changes.issue.template", "issueLinkTemplate" );
        addConfigurationChild( configuration, "xmlPath", "${basedir}/xdocs/changes.xml" );
    }

    protected List getReportSets()
    {
        List reportSets = new ArrayList();

        ReportSet reportSet = new ReportSet();
        reportSet.addReport( "changes-report" );
        reportSets.add( reportSet );

        return reportSets;
    }
}

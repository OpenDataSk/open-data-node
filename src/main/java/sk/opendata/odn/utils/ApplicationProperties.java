/* Copyright (C) 2011 Peter Hanecak <hanecak@opendata.sk>
 *
 * This file is part of Open Data Node.
 *
 * Open Data Node is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open Data Node is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open Data Node.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.opendata.odn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.harvester.datanest.organizations.AbstractDatanestHarvester;

public class ApplicationProperties extends Properties {
	
	private static final long serialVersionUID = 4684911806029642651L;

	public final String ODN_PROPERTY_SUBDIR = ".odn";
	
	private static Logger logger = LoggerFactory.getLogger(AbstractDatanestHarvester.class);
	private static Hashtable<String, ApplicationProperties> instances = new Hashtable<String, ApplicationProperties>();
	
	private ApplicationProperties(String propertiesFileName) throws IOException {
		super();
		
		// load the default properties from the bundle
		this.load(getClass().getResourceAsStream(propertiesFileName));
		logger.info("loaded default properties from '" + propertiesFileName + "' from the bundle");
		
		// load the properties from the user home directory and - if found and
		// properly loaded - use them to override properties from the bundle
		File homeSubdirFn = new File(System.getProperty("user.home"),
				ODN_PROPERTY_SUBDIR);
		File overrideFn = new File(homeSubdirFn, propertiesFileName);
		if (overrideFn.exists() && overrideFn.isFile() && overrideFn.canRead()) {
			FileInputStream in = new FileInputStream(overrideFn);
			Properties overrides = new Properties();
			overrides.load(in);
			
			this.putAll(overrides);
			logger.info("user properties loaded from '" + overrideFn.getAbsolutePath() + "'");
		}
	}
	
	/**
	 * Load properties from given file name. Properties are loaded only once from one unique file name.
	 * 
	 * Properties load procedure:
	 * 
	 * <ol>
	 * <li>{@code propertiesFileName} is loaded from resources bundled with
	 * application</li>
	 * <li>if file {@code propertiesFileName} exists in subdirectory
	 * {@code .odn} in user's home directory (as specified by {@code user.home}
	 * system property), properties are loaded from that file too and they are
	 * then added to the properties loaded from the bundled resources</li>
	 * <li>the resulting set of properties are then present in the instance of
	 * this class</li>
	 * </ol>
	 * 
	 * This allows for:
	 * 
	 * <ul>
	 * <li>all the necessary properties be specified in bundled property file</li>
	 * <li>user/administrator of the application can override some or all
	 * properties in the file
	 * {@code &lt;user.home&gt;/.odn/&lt;propertiesFileName&gt;}
	 * </ul>
	 * 
	 * TODO: As of now, we're using this as 'new
	 * ApplicationProperties("/file.properties")' => clarify the usage if
	 * staring '/'!
	 * 
	 * @param propertiesFileName
	 *            properties file name
	 * @throws IOException
	 *             when loading of the properties from bundle or file fails
	 */
	public static ApplicationProperties getInstance(String propertiesFileName) throws IOException {
		ApplicationProperties ap = instances.get(propertiesFileName);
		
		if (ap == null) {
			ap = new ApplicationProperties(propertiesFileName);
			instances.put(propertiesFileName, ap);
		}
		
		return ap;
	}

}

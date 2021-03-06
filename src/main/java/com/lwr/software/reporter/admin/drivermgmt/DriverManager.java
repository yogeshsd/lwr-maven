/* 
	Query2Report Copyright (C) 2018  Yogesh Deshpande
	
	This file is part of Query2Report.
	
	Query2Report is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	Query2Report is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with Query2Report.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.lwr.software.reporter.admin.drivermgmt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;

import com.lwr.software.reporter.DashboardConstants;

public class DriverManager {

	private static volatile DriverManager manager;
	
	private Set<DriverParams> driverParams = new HashSet<DriverParams>();
	
	private static Logger logger = LogManager.getLogger(DriverManager.class);
	
	private String fileName = DashboardConstants.CONFIG_PATH+"drivers.json";
	
	public static DriverManager getDriverManager(){
		if(manager == null){
			synchronized (DriverManager.class) {
				if(manager == null){
					manager = new DriverManager();
				}
			}
		}
		return manager;
	}
	
	private DriverManager(){
		init();
	}
	
	private void init(){
		logger.info("Initializing driver manager from "+new File(fileName).getAbsolutePath());
	    try {
	    	ObjectMapper objectMapper = new ObjectMapper();
	        TypeFactory typeFactory = objectMapper.getTypeFactory();
	        CollectionType collectionType = typeFactory.constructCollectionType(Set.class, DriverParams.class);
	        driverParams =  objectMapper.readValue(new File(fileName), collectionType);
	    } catch (IOException e) {
	    	logger.error("Unable to initialize driver manager",e);
	    }
	}
	
	public Set<DriverParams> getDrivers(){
		return this.driverParams;
	}
	
	public DriverParams getDriver(String alias){
		logger.info("Getting driver for alias "+alias);
		for (DriverParams driver : driverParams) {
			if(driver.getAlias().equalsIgnoreCase(alias)){
				logger.info("Returning driver "+driver+" against alias "+alias);
				return driver;
			}
		}
		logger.error("Unable to find driver for alias "+alias);
		return null;
	}

	public boolean saveDriver(DriverParams params){
		logger.info("Saving driver "+params);
		try{
			boolean found = false;
			Iterator<DriverParams> iterator = driverParams.iterator();
			while(iterator.hasNext()){
				DriverParams param = iterator.next();
				if(param.getAlias().equals(params.getAlias())){
					found = true;
					param.setClassName(params.getClassName());
					if(params.getJarFile() != null)
						param.setJarFile(params.getJarFile());
				}
			}
			if(!found)
				driverParams.add(params);
			serializeDriverParams();
			return true;
		}catch(Exception e){
			logger.error("Unable to save driver "+params.getAlias(),e);
			return false;
		}
		
	}
	
	public boolean removeDriver(String alias) {
		logger.info("Deleting driver "+alias);
		DriverParams paramToDelete = null;
		for (DriverParams param : driverParams) {
			if(param.getAlias().equalsIgnoreCase(alias)){
				paramToDelete = param;
				break;
			}
		}
		if(paramToDelete == null){
			logger.error("Unable to find driver for alias "+alias);
			return false;
		}
		logger.info("Deleting driver "+paramToDelete+" against alias "+alias);
		driverParams.remove(paramToDelete);
		serializeDriverParams();
		return true;
	}
	
	private void serializeDriverParams(){
		try{
			logger.info("Seralizing drivers to file "+new File(fileName).getAbsolutePath());
	    	ObjectMapper objectMapper = new ObjectMapper();
	        String dataToRight = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(driverParams);
	        FileWriter writer = new FileWriter(fileName);
	        writer.write(dataToRight);
	        writer.flush();
	        writer.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Unable to seralize driver manager ",e);
		}
	}
}

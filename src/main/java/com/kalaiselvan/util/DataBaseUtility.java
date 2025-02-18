package com.kalaiselvan.util;
import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonObject;

public class DataBaseUtility {

	private static BasicDataSource staticDataSource;

	public static BasicDataSource getDataSource() {
		if (staticDataSource == null) {
			try {
				Class.forName("org.postgresql.Driver");
				BasicDataSource basicDataSource = new BasicDataSource();
				JsonObject databaseConfig = new JsonObject(); //JSON
				basicDataSource.setUrl(String.format("jdbc:postgresql://%s:%s/%s", databaseConfig.get("ip").getAsString(), databaseConfig.get("port").getAsString(), databaseConfig.get("database").getAsString()));
				basicDataSource.setDefaultSchema(databaseConfig.get("schema").getAsString());
				basicDataSource.setUsername(databaseConfig.get("userName").getAsString());
				basicDataSource.setPassword(databaseConfig.get("password").getAsString());
				basicDataSource.setMinIdle(databaseConfig.get("minIdealConnection").getAsInt());
				basicDataSource.setMaxIdle(databaseConfig.get("maxIdealConnection").getAsInt());
				basicDataSource.setMaxOpenPreparedStatements(100);
				staticDataSource = basicDataSource;
				return staticDataSource;
			} catch (Exception ex) {
				 System.err.println("Exception : " + StackTrace.getMessage(ex));
				return null;
			}
		} else {
			return staticDataSource;
		}
	}

	public BasicDataSource getConnection() {
		return getDataSource();
	}
}
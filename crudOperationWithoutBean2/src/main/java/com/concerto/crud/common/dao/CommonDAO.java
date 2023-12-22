package com.concerto.crud.common.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.concerto.crud.common.bean.Entity;
import com.concerto.crud.common.bean.Module;

/**
 * Copyright (C) Concerto Software and Systems (P) LTD | All Rights Reserved
 * 
 * @File : com.concerto.crud.common.dao.CommonDAO.java
 * @Author : Suyog Kedar
 * @AddedDate : October 03, 2023 12:30:40 PM
 * @Purpose : Defines methods for executing create, update, read, delete, and
 *          other operations on a specified module in the database. This
 *          interface serves as a contract for interacting with the underlying
 *          data store.
 * @Version : 1.0
 */
public interface CommonDAO {

	Map<String, Object> executeGetData(String fieldName, Object value, Module module);

	List<Map<String, Object>> executeGetAllData(Module module);

	boolean addToMaster(Map<String, Object> input, Module module);

	boolean deleteData(Map<String, Object> input, Module module, String action, String tableSuffix);

	boolean addToHist(Map<String, Object> input, Module module, String request, String action, Connection connection);

	List<Map<String, Object>> getById(String tableName, Map<String, Object> input, boolean isSubBean);

	boolean performUpdate(Map<String, Object> data, Module module);

	boolean doCUDOperations(Map<String, Object> input, Entity entity, String request);

	boolean doCUDOperationForBean(Map<String, Object> parentBody, List<Map<String, Object>> beanData, Module module,
			String request);

}

package com.concerto.crud.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.concerto.crud.common.bean.Bean;
import com.concerto.crud.common.bean.Entity;
import com.concerto.crud.common.bean.Module;
import com.concerto.crud.common.constant.AppConstant;
import com.concerto.crud.common.dao.CommonDAO;
import com.concerto.crud.common.util.JsonToJavaConverter;
import com.concerto.crud.common.validationservice.ValidationService;

/**
 * Copyright (C) Concerto Software and Systems (P) LTD | All Rights Reserved
 * 
 * @File : com.concerto.crud.common.service.CommonServiceImpl.java
 * @Author : Suyog Kedar
 * @AddedDate : October 03, 2023 12:30:40 PM
 * @Purpose : Service class providing methods for common operations on a
 *          specified module. Uses data validation, JSON conversion, and
 *          database interactions to perform create, update, read, delete, and
 *          search operations, as well as approval and rejection controls.
 *          Utilizes the CommonModuleDAO interface for database access.
 * @Version : 1.0
 */

@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	private CommonDAO commonDAO;

	@Autowired
	private ValidationService validationService;

	@Override
	public Map<String, Object> getData(String fieldName, Object value, String moduleName) {
		Module module = JsonToJavaConverter.moduleData(moduleName);
		Map<String, Object> response = new HashMap<>();
		List<String> primaryFields = JsonToJavaConverter.getPrimaryfields(moduleName);

		for (String field : primaryFields) {
			if (field.equals(fieldName)) {
				Map<String, Object> dataRetrieve = commonDAO.executeGetData(fieldName, value, module);
				if (!dataRetrieve.isEmpty()) {
					return dataRetrieve;
				} else {
					response.put(AppConstant.COMMON_MODULE_MESSAGE, AppConstant.DATA_NOT_AVAILABLE);
					return response;
				}
			} else {
				response.put(AppConstant.COMMON_MODULE_MESSAGE, AppConstant.NOT_PRIMARY_KEY);
			}
		}
		return response;
	}

	@Override
	public List<Map<String, Object>> getAllData(String moduleName) {
		Module module = JsonToJavaConverter.moduleData(moduleName);
		List<Map<String, Object>> dataRetrieve = commonDAO.executeGetAllData(module);
		if (!dataRetrieve.isEmpty()) {
			return dataRetrieve;
		} else {
			Map<String, List<Map<String, Object>>> response = new HashMap<>();
			List<Map<String, Object>> emptyDataList = new ArrayList<>();
			response.put(AppConstant.COMMON_MODULE_MESSAGE, emptyDataList);

			Map<String, Object> message = new HashMap<>();
			message.put(AppConstant.COMMON_MODULE_MESSAGE, AppConstant.DATA_NOT_AVAILABLE_IN_MODULE + moduleName);
			emptyDataList.add(message);

			return emptyDataList;
		}
	}

	@Override
	public Map<String, Object> doCUDprocess(Map<String, Object> requestBody, String moduleName, String request) {
		Map<String, Object> result = new HashMap<>();
		String response = null;
		Module module = JsonToJavaConverter.moduleData(moduleName);
		Map<String, String> validationResult = validationService.checkValidation(module, requestBody);

		if (!validationResult.isEmpty()) {
			return new HashMap<>(validationResult);
		}

		List<Bean> beanList = module.getBeans();

		if (beanList != null) {
			response = handleWithBeans(requestBody, module, request);
			if (AppConstant.SUCCESS.equals(response)) {
				result.put(AppConstant.COMMON_MODULE_MESSAGE, AppConstant.ADDED_FOR_APPROVAL + request);
			} else {
				result.put(AppConstant.COMMON_MODULE_MESSAGE, response);
			}
		} else {
			response = doCUDprocess(requestBody, module, request);
			result.put(AppConstant.COMMON_MODULE_MESSAGE, response);
		}

		return result;
	}

	private String handleWithBeans(Map<String, Object> requestBody, Module module, String request) {
		Map<String, Object> parentBody = extractParentFields(requestBody);
		List<Map<String, Object>> beanData = extractBeanList(requestBody);

		String response = doCUDprocessForBean(parentBody, module, request);
		if (AppConstant.SUCCESS.equals(response)) {
			List<Bean> beanList = module.getBeans();
			for (int i = 0; i < beanList.size(); i++) {
				Bean bean = beanList.get(i);
				for (Map<String, Object> childBody : beanData) {
					response = doCUDprocessForBean(childBody, bean, request);
				}
				if (AppConstant.SUCCESS.equals(response)) {
					boolean result = commonDAO.doCUDOperationForBean(parentBody, beanData, module, request);
					if (result) {
						response = AppConstant.SUCCESS;
					} else {
						response = AppConstant.DATA_INSERTION_FAILED;
					}
				}
			}
		}
		return response;

	}

	private String doCUDprocessForBean(Map<String, Object> requestBody, Entity entity, String request) {
		String entityName = entity.getEntityName();
		boolean isSubBean = entity.isSubBean();
		String response = null;
		List<Map<String, Object>> tempResult = commonDAO.getById(entityName + AppConstant.TEMP_TABLE_SUFFIX,
				requestBody, isSubBean);
		List<Map<String, Object>> masterResult = commonDAO.getById(entityName + AppConstant.MASTER_TABLE_SUFFIX,
				requestBody, isSubBean);
		if (!tempResult.isEmpty()) {
			response = AppConstant.APPROVAL_PENDING;
		} else if (!masterResult.isEmpty() && AppConstant.ADD.equalsIgnoreCase(request)) {
			response = AppConstant.DATA_PRESENT;
		} else if (masterResult.isEmpty()
				&& (AppConstant.UPDATE.equalsIgnoreCase(request) || AppConstant.DELETE.equalsIgnoreCase(request))) {
			response = AppConstant.DATA_NOT_PRESENT;
		} else {
			response = "success";
		}
		return response;

	}

	private String doCUDprocess(Map<String, Object> requestBody, Entity entity, String request) {
		String entityName = entity.getEntityName();
		boolean isSubBean = entity.isSubBean();
		String response = null;
		List<Map<String, Object>> tempResult = commonDAO.getById(entityName + AppConstant.TEMP_TABLE_SUFFIX,
				requestBody, isSubBean);
		List<Map<String, Object>> masterResult = commonDAO.getById(entityName + AppConstant.MASTER_TABLE_SUFFIX,
				requestBody, isSubBean);
		if (!tempResult.isEmpty()) {
			response = AppConstant.APPROVAL_PENDING;
		} else if (!masterResult.isEmpty() && AppConstant.ADD.equalsIgnoreCase(request)) {
			response = AppConstant.DATA_PRESENT;
		} else if (masterResult.isEmpty()
				&& (AppConstant.UPDATE.equalsIgnoreCase(request) || AppConstant.DELETE.equalsIgnoreCase(request))) {
			response = AppConstant.DATA_NOT_PRESENT;
		} else {
			boolean result = commonDAO.doCUDOperations(requestBody, entity, request);
			response = result ? AppConstant.ADDED_FOR_APPROVAL + request : AppConstant.DATA_INSERTION_FAILED;
		}
		return response;

	}

	@Override
	public Map<String, Object> doApproveOrReject(Map<String, Object> requestBody, String entityName, String action) {
		Map<String, Object> response = new HashMap<>();
		Module module = JsonToJavaConverter.moduleData(entityName);
		boolean result = false;
		List<Bean> beans = module.getBeans();
		List<Map<String, Object>> dataList = commonDAO.getById(entityName + AppConstant.TEMP_TABLE_SUFFIX, requestBody,
				false);
		Map<String, Object> data = dataList.isEmpty() ? Collections.emptyMap() : dataList.get(0);
		if (beans != null) {
			for (Bean bean : beans) {
				String beanName = bean.getEntityName();
				List<Map<String, Object>> subBeanData = commonDAO.getById(beanName + AppConstant.TEMP_TABLE_SUFFIX,
						requestBody, true);
				if (data.isEmpty() && subBeanData.isEmpty()) {

					response.put(AppConstant.COMMON_MODULE_MESSAGE, AppConstant.NO_REQUEST_PENDING);
					return response;
				} else {
					Map<String, Object> combinedData = new HashMap<>();
					combinedData.put(AppConstant.PARENT_DATA, data);
					combinedData.put(AppConstant.BEAN_DATA, subBeanData);
					result = executeApproveOrRejectAction(module, action, combinedData);

				}
			}
		}

		else if (data.isEmpty()) {
			response.put(AppConstant.COMMON_MODULE_MESSAGE, AppConstant.NO_REQUEST_PENDING);
			return response;
		} else {
			result = executeApproveOrRejectAction(module, action, data);
		}

		if (result) {
			response.put(AppConstant.COMMON_MODULE_MESSAGE, action + AppConstant.ACTION_SUCCESSFUL);
		} else {
			response.put(AppConstant.COMMON_MODULE_MESSAGE, action + AppConstant.ACTION_FAILED);
		}

		return response;
	}

	private boolean executeApproveOrRejectAction(Module module, String action, Map<String, Object> data) {
		boolean result = false;

		if (AppConstant.APPROVE.equalsIgnoreCase(action)) {
			return handleApproveAction(data, module, action);
		} else if (AppConstant.REJECT.equalsIgnoreCase(action)) {
			return commonDAO.deleteData(data, module, action, AppConstant.TEMP_TABLE_SUFFIX);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private boolean handleApproveAction(Map<String, Object> data, Module module, String action) {
		boolean tempDelete = false;
		Map<String, Object> dataMap = (Map<String, Object>) data.get(AppConstant.PARENT_DATA);
		String request = dataMap.get(AppConstant.REQUEST).toString();
		if (AppConstant.NEW_DATA_ACTION.equalsIgnoreCase(request)) {
			boolean addToMaster = commonDAO.addToMaster(data, module);
			if (addToMaster) {
				tempDelete = commonDAO.deleteData(data, module, action, AppConstant.TEMP_TABLE_SUFFIX);
			}
			return addToMaster && tempDelete;
		} else if (AppConstant.DELETE_ACTION.equalsIgnoreCase(request)) {
			boolean masterDelete = commonDAO.deleteData(data, module, action, AppConstant.MASTER_TABLE_SUFFIX);
			if (masterDelete) {
				tempDelete = commonDAO.deleteData(data, module, action, AppConstant.TEMP_TABLE_SUFFIX);
			}
			return masterDelete && tempDelete;
		} else if (AppConstant.UPDATE_ACTION.equalsIgnoreCase(request)) {
			boolean update = commonDAO.performUpdate(data, module);
			if (update) {
				tempDelete = commonDAO.deleteData(data, module, action, AppConstant.TEMP_TABLE_SUFFIX);
			}
			return update && tempDelete;
		}
		return false;

	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> extractBeanList(Map<String, Object> requestBody) {
		List<Map<String, Object>> beanList = new ArrayList<>();

		for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (AppConstant.BEANS.equals(key) && value instanceof List) {
				beanList.addAll((List<Map<String, Object>>) value);
			}
		}

		return beanList;
	}

	public Map<String, Object> extractParentFields(Map<String, Object> requestBody) {
		Map<String, Object> parentFields = new HashMap<>();

		for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (!"Beans".equals(key)) {
				parentFields.put(key, value);
			}
		}

		return parentFields;
	}

}

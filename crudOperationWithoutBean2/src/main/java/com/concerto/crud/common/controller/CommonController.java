package com.concerto.crud.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.concerto.crud.common.constant.AppConstant;
import com.concerto.crud.common.service.CommonService;
import com.concerto.crud.common.util.Logging;

/**
 * Copyright (C) Concerto Software and Systems (P) LTD | All Rights Reserved
 * 
 * @File : com.concerto.crud.common.controller.CommonController.java
 * @Author : Suyog Kedar
 * @AddedDate : October 03, 2023 12:30:40 PM
 * @Purpose : Handles HTTP requests for creating, updating, reading, and
 *          deleting data for a specified module.
 * @Version : 1.0
 */

@RestController
@RequestMapping("/common_module")
public class CommonController {

	@Autowired
	private CommonService commonService;


	@GetMapping("/readData/{moduleName}")
	public ResponseEntity<Map<String, Object>> getData(@RequestParam String fieldName, @RequestParam Object value,
			@PathVariable String moduleName) {
		Map<String, Object> response = new HashMap<>();
		try {
			response = commonService.getData(fieldName, value, moduleName);
		} catch (Exception e) {
			Logging.error(AppConstant.DATA_READING_ERROR, e);
			response.put(AppConstant.COMMON_MODULE_ERROR, AppConstant.DATA_READING_ERROR);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	@GetMapping("/readAllData/{moduleName}")
	public ResponseEntity<List<Map<String, Object>>> getAllData(@PathVariable String moduleName) {
		List<Map<String, Object>> response = new ArrayList<>();
		try {
			response = commonService.getAllData(moduleName);
		} catch (Exception e) {
			Logging.error(AppConstant.DATA_READING_ERROR, e);
			List<Map<String, Object>> errorDetails = new ArrayList<>();
			Map<String, Object> errorMap = new HashMap<>();
			errorMap.put(AppConstant.COMMON_MODULE_ERROR, AppConstant.DATA_READING_ERROR);
			errorDetails.add(errorMap);
			return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	@PostMapping("{action}/{moduleName}")
	public ResponseEntity<Map<String, Object>> approveOrRejectControl(@RequestBody Map<String, Object> input,
			@PathVariable String moduleName, @PathVariable String action) {
		Map<String, Object> response = new HashMap<>();
		try {
			if (AppConstant.APPROVE.equalsIgnoreCase(action) || AppConstant.REJECT.equalsIgnoreCase(action)) {
				response = commonService.doApproveOrReject(input, moduleName, action);
			} else {
				response.put(AppConstant.COMMON_MODULE_ERROR, AppConstant.ACTION_INCORRECT);
			}
		} catch (Exception e) {
			Logging.error(AppConstant.ACTION_CONTROL_ERROR, e);
			response.put(AppConstant.COMMON_MODULE_ERROR, AppConstant.ACTION_CONTROL_ERROR);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);

	}


	@PostMapping("process/{moduleName}")
	public ResponseEntity<Map<String, Object>> doCUDprocess(@RequestBody Map<String, Object> requestBody,
			@PathVariable String moduleName, @RequestParam String request) {
		Map<String, Object> response = new HashMap<>();
		try {
			response = commonService.doCUDprocess(requestBody, moduleName, request);
		} catch (Exception e) {
			Logging.error(AppConstant.CUD_PROCESS_ERROR + request, e);
			response.put(AppConstant.COMMON_MODULE_ERROR, AppConstant.CUD_PROCESS_ERROR + request);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}

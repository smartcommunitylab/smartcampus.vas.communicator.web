/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.vas.communicator.controller;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.presentation.data.SyncData;
import eu.trentorise.smartcampus.presentation.data.SyncDataRequest;
import eu.trentorise.smartcampus.profileservice.ProfileServiceException;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.vas.communicator.manager.CommunicatorManager;

@Controller
public class ObjectController extends RestController {

	@Autowired
	CommunicatorManager communicatorManager;

	@RequestMapping(method = RequestMethod.POST, value = "/sync")
	public @ResponseBody
	ResponseEntity<SyncData> syncData(HttpServletRequest request, HttpServletResponse response, @RequestParam long since, @RequestBody Map<String, Object> obj) throws DataException, IOException, NotFoundException, ClassNotFoundException, SecurityException, ProfileServiceException {
		BasicProfile user = getUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		SyncDataRequest syncReq = Util.convertRequest(obj, since);
		SyncData out = communicatorManager.synchronize(user, syncReq.getSyncData());
		return new ResponseEntity<SyncData>(out, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/synctype")
	public @ResponseBody
	ResponseEntity<SyncData> syncData(HttpServletRequest request, HttpServletResponse response, @RequestParam long since, @RequestParam(value = "type", defaultValue = "") String type) throws DataException, IOException, NotFoundException, ClassNotFoundException, SecurityException, ProfileServiceException {
		SyncDataRequest syncReq = convertTypeRequest(type, since);
		SyncData out = communicatorManager.simpleSynchronize(syncReq.getSyncData());
		return new ResponseEntity<SyncData>(out, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/synctypefromdate")
	public @ResponseBody
	ResponseEntity<SyncData> syncDataFromDate(HttpServletRequest request, HttpServletResponse response, @RequestParam long from, @RequestParam(value = "type", defaultValue = "") String type) throws DataException, IOException, NotFoundException, ClassNotFoundException, SecurityException, ProfileServiceException {
		SyncDataRequest syncReq = convertTypeRequest(type, null);
		SyncData out = communicatorManager.simpleSynchronizeFromTime(from, syncReq.getSyncData());
		return new ResponseEntity<SyncData>(out, HttpStatus.OK);
	}

	public static SyncDataRequest convertTypeRequest(String type, Long since) throws ClassNotFoundException {
		SyncData data = new SyncData();
		if (since != null) {
			try {
				data.setVersion(since);
			} catch (Exception e1) {
				data.setVersion(0);
			}
		}
		Map<String, Object> include = new TreeMap<String, Object>();
		if (!type.isEmpty()) {
			include.put("type", type);
		}

		data.setInclude(include);

		return new SyncDataRequest(data, since != null? since: 0);
	}

}

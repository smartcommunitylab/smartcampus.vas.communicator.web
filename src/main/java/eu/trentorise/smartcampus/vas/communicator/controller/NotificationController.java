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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.profileservice.ProfileServiceException;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;
import eu.trentorise.smartcampus.vas.communicator.manager.NotificationManager;

@Controller
public class NotificationController extends RestController {

	@Autowired
	NotificationManager notificationManager;

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.communicator.model.Notification")
	public @ResponseBody
	List<Notification> getNotifications(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("since") Long since,
			@RequestParam("position") Integer position,
			@RequestParam("count") Integer count) throws DataException,
			IOException, SecurityException, ProfileServiceException {

		BasicProfile user = getUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.get(user, since, position, count, null);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.communicator.model.Notification/{id}")
	public @ResponseBody
	Notification getNotification(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id) throws DataException, IOException,
			NotFoundException, SecurityException, ProfileServiceException {

		BasicProfile user = getUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.getById(id);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.communicator.model.Notification/{id}")
	public @ResponseBody
	boolean delete(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id)
			throws DataException, IOException, NotFoundException, SecurityException, ProfileServiceException {

		BasicProfile user = getUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.delete(id);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.communicator.model.Notification/{id}")
	public @ResponseBody
	void update(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SecurityException, ProfileServiceException {

		BasicProfile user = getUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		notificationManager.updateLabels(id, notification.getLabelIds());
		notificationManager.starred(id, notification.isStarred());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/objects")
	public @ResponseBody
	Map<String, List<Notification>> filterNotifications(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestParam("filter") String jsonFilter,
			@RequestParam Long since, @RequestParam Integer position, 
			@RequestParam Integer count)
			throws IOException, DataException, SecurityException, ProfileServiceException {

		NotificationFilter filter = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			filter = mapper.readValue(jsonFilter, NotificationFilter.class);
		} catch (JsonMappingException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

		BasicProfile user = getUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		Map<String, List<Notification>> result = new HashMap<String, List<Notification>>();
		List<Notification> notList = notificationManager.get(user, since, position, count, filter);

		result.put(
				Notification.class.getName(),
				(notList == null) ? Collections.<Notification> emptyList() : notList);
		return result;
	}
}

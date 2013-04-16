/**
 *    Copyright 2012-2013 Trento RISE
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
 */

package eu.trentorise.smartcampus.vas.communicator.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sun.istack.logging.Logger;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
import eu.trentorise.smartcampus.communicator.model.Configuration;
import eu.trentorise.smartcampus.communicator.model.UserAccount;
import eu.trentorise.smartcampus.controllers.SCController;
import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.NotFoundException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.vas.communicator.manager.AppAccountManager;
import eu.trentorise.smartcampus.vas.communicator.manager.UserAccountManager;

@Controller
public class PushController extends SCController {

	private static final String REGISTRATIONID_HEADER = "REGISTRATIONID";

	@Autowired
	UserAccountManager userAccountManager;

	@Autowired
	AppAccountManager appAccountManager;

	@Autowired
	@Value("${gcm.sender.id.default.key}")
	private String gcm_sender_id_default_key;

	@Autowired
	@Value("${gcm.sender.id.default.value}")
	private String gcm_sender_id_default_value;

	@Autowired
	@Value("${gcm.registration.id.default.key}")
	private String gcm_registration_id_default_key;

	@Autowired
	@Value("${gcm.registration.id.default.value}")
	private String gcm_registration_id_default_value;

	@RequestMapping(method = RequestMethod.POST, value = "/register/app/{appName}/{senderId}")
	public @ResponseBody
	boolean registerAppToPush(HttpServletRequest request,
			@PathVariable String appName,
			@PathVariable("senderId") String senderId, HttpSession session)
			throws DataException, IOException, NotFoundException,
			SmartCampusException, AlreadyExistException {

		List<Configuration> listConf = new ArrayList<Configuration>();

		// set value of sender/serverside app registration code
		if (senderId == null)
			senderId = gcm_sender_id_default_value;
		// if app is not registered?use ours?

		Configuration e = new Configuration(gcm_sender_id_default_key,
				CloudToPushType.GOOGLE, senderId);
		listConf.add(e);

		AppAccount appAccount;
		List<AppAccount> listApp = appAccountManager.getAppAccounts(appName);
		if (listApp.isEmpty()) {
			appAccount = new AppAccount();
			appAccount.setAppName(appName);
			appAccount.setConfigurations(listConf);
			appAccountManager.save(appAccount);
		} else {
			appAccount = listApp.get(0);
			appAccount.setConfigurations(listConf);
			appAccountManager.update(appAccount);
		}

		return true;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/register/user/{appName}")
	public @ResponseBody
	boolean registerUserToPush(HttpServletRequest request,
			@PathVariable String appName, HttpSession session)
			throws DataException, IOException, NotFoundException,
			SmartCampusException, AlreadyExistException {

		User user = retrieveUser(request);
		UserAccount userAccount;
		String registrationId = request.getHeader(REGISTRATIONID_HEADER);

		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
				user.getId(), appName);

		if (listUser.isEmpty()) {
			userAccount = new UserAccount();
			userAccount.setAppName(appName);
			userAccount.setUserId(user.getId());
			try {
				userAccountManager.save(userAccount);
			} catch (AlreadyExistException e1) {
				throw new AlreadyExistException(e1);
			}
		} else {

			userAccount = listUser.get(0);
		}

		List<Configuration> listConf = new ArrayList<Configuration>();

		// set value of sender/serverside user registration code
		if (registrationId == null)
			registrationId = gcm_registration_id_default_value;
		// if user is not registered?use ours?

		// ask type of device
		Configuration e = new Configuration(gcm_registration_id_default_key,
				CloudToPushType.GOOGLE, registrationId);
		listConf.add(e);

		userAccount.setConfigurations(listConf);
		userAccountManager.update(userAccount);

		return true;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/unregister/user/{appName}")
	public @ResponseBody
	boolean unregisterUserToPush(HttpServletRequest request,
			@PathVariable String appName, HttpSession session)
			throws DataException, IOException, NotFoundException,
			SmartCampusException, AlreadyExistException {

		User user = retrieveUser(request);
		UserAccount userAccount;
	

		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
				user.getId(), appName);

		if (!listUser.isEmpty()) {
			userAccount = listUser.get(0);

			userAccount.setConfigurations(null);
			userAccountManager.update(userAccount);
		
		}

		return true;

	}
}

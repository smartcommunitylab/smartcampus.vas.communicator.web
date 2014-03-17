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
package eu.trentorise.smartcampus.vas.communicator.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;
import eu.trentorise.smartcampus.vas.communicator.storage.CommunicatorStorage;

@Component
public class NotificationManager {

	@Autowired
	CommunicatorStorage storage;

	public void create(Notification notification) throws DataException {
		storage.storeObject(notification);
	}

	public boolean delete(String id) throws NotFoundException, DataException {
		storage.deleteObject(storage.getObjectById(id, Notification.class));
		return true;
	}

	public List<Notification> get(BasicProfile user, Long since, Integer position, Integer count, NotificationFilter filter) throws DataException {
		return storage.searchNotifications(user.getUserId(), since, position, count, filter);
	}

	public Notification getById(String id) throws NotFoundException, DataException {
		return storage.getObjectById(id, Notification.class);
	}

	/**
	 * set starred value to given value
	 * 
	 * @param id
	 * @param starredStatus
	 * @throws NotFoundException
	 * @throws DataException
	 */
	public void starred(String id, boolean starredStatus) throws NotFoundException, DataException {
		changeStarredStatus(id, starredStatus);
	}

	/**
	 * set starred value to true
	 * 
	 * @param id
	 *            notification id
	 * @throws NotFoundException
	 * @throws DataException
	 */
	public void starred(String id) throws NotFoundException, DataException {
		changeStarredStatus(id, true);
	}

	private void changeStarredStatus(String id, boolean starred)
			throws NotFoundException, DataException {
		Notification notification = storage.getObjectById(id, Notification.class);
		notification.setStarred(starred);
		storage.storeObject(notification);
	}

	public void updateLabels(String id, List<String> labelIds)
			throws NotFoundException, DataException {
		Notification notification = storage.getObjectById(id, Notification.class);
		notification.setLabelIds(labelIds);
		storage.storeObject(notification);
	}
	
	public void deleteUserMessages(BasicProfile user) throws DataException {
		storage.deleteObjectsPermanently(Notification.class, user.getUserId());
	}
}

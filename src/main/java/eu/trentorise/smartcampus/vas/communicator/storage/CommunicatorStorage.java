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
package eu.trentorise.smartcampus.vas.communicator.storage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.data.BasicObject;
import eu.trentorise.smartcampus.presentation.storage.sync.mongo.BasicObjectSyncMongoStorage;
import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;

public class CommunicatorStorage extends BasicObjectSyncMongoStorage {

	public CommunicatorStorage(MongoOperations mongoTemplate) {
		super(mongoTemplate);
	}

	public <T extends BasicObject> void deleteObjectPermanently(T object) throws DataException {
		mongoTemplate.remove(Query.query(Criteria.where("id").is(object.getId())), getObjectClass());
	}

	public <T extends BasicObject> void deleteObjectsPermanently(Class<T> cls, String user) throws DataException {
		mongoTemplate.remove(Query.query(Criteria.where("user").is(user).and("type").is(cls.getCanonicalName())), getObjectClass());
	}
	
	public List<Notification> searchNotifications(String user, Long since, Integer position, Integer count, NotificationFilter infilter) {
		NotificationFilter filter = infilter == null ? new NotificationFilter() : infilter;  
		List<Notification> list = find(Query.query(createNotificationSearchWithTypeCriteria(user, since, filter)), Notification.class);
		if (filter.getOrdering() != null) {
			switch (filter.getOrdering()) {
			case ORDER_BY_ARRIVAL:
				Collections.sort(list,arrivalDateComparator);
				break;
			case ORDER_BY_REL_PLACE:
			case ORDER_BY_REL_TIME:
			case ORDER_BY_PRIORITY:
			default:
				break;
			}
		}
		else {
			Collections.sort(list,arrivalDateComparator);
		}
		if (position != null && count != null && position > 0 && count > 0 && list.size() > position) {
			return list.subList(position, Math.min(list.size(), position+count));
		}
		return list;
	}
	
	private Criteria createNotificationSearchWithTypeCriteria(String user, Long since, NotificationFilter filter) {
		Criteria criteria = new Criteria();
		// user is obligatory
		criteria.and("user").is(user);
		// only non-deleted
		criteria.and("deleted").is(false);
	
		if (since != null) {
			criteria.and("content.timestamp").gte(since);
		}
		if (filter.isReaded() != null) {
			criteria.and("content.readed").is(filter.isReaded());
		}
		if (filter.isStarred() != null) {
			criteria.and("content.starred").is(filter.isStarred());
		}
		if (filter.getSourceType() != null) {
			criteria.and("content.type").is(filter.getSourceType());
		}
		if (filter.getLabelId() != null) {
			criteria.and("content.labelIds").is(filter.getLabelId());
		}
		if (filter.getSearchText() != null) {
			criteria.orOperator(new Criteria().and("content.title").regex(filter.getSearchText(), "i"),new Criteria().and("content.description").regex(filter.getSearchText(), "i"));
		}
		return criteria;
	}

	private Comparator<Notification> arrivalDateComparator = new Comparator<Notification>() {
		@Override
		public int compare(Notification o1, Notification o2) {
			return (int)(o1.getTimestamp() - o2.getTimestamp()); 
		}
	};
	
	
}

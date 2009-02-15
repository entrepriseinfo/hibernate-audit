/**
 * Copyright (C) 2009 Krasimir Chobantonov <kchobantonov@yahoo.com>
 * This file is part of GNU Hibernate Audit.

 * GNU Hibernate Audit is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GNU Hibernate Audit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with GNU Hibernate Audit.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public abstract class AbstractCollectionAuditWorkUnit extends
		AbstractAuditWorkUnit {
	
	protected void processElement(Session session,
			AuditConfiguration auditConfiguration, Object entityOwner, Object element,
			Type elementType, String propertyName, long index,
			EntityAuditObject auditObject, AuditEvent auditEvent) {

		if (elementType.isEntityType()) {
			Serializable id = null;
			
			if (element != null) {
				id = session.getSessionFactory().getClassMetadata(
						((EntityType) elementType).getAssociatedEntityName())
						.getIdentifier(element, session.getEntityMode());
			}

			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entityOwner.getClass().getName(), propertyName);

			EntityObjectProperty property = new EntityObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			property.setTargetEntityId(auditConfiguration.getExtensionManager()
					.getPropertyValueConverter().toString(id));
			auditObject.getAuditObjectProperties().add(property);
		} else if (elementType.isComponentType()) {
			AbstractComponentType componentType = (AbstractComponentType) elementType;

			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entityOwner.getClass().getName(), propertyName);

			ComponentObjectProperty property = new ComponentObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			ComponentAuditObject targetComponentAuditObject = null;

			if (element != null) {
				targetComponentAuditObject = new ComponentAuditObject();
				targetComponentAuditObject.setAuditEvent(auditEvent);
				targetComponentAuditObject.setParentAuditObject(auditObject);
				AuditType auditComponentType = HibernateAudit.getAuditType(
						session, element.getClass().getName());
				targetComponentAuditObject.setAuditType(auditComponentType);

				for (int j = 0; j < componentType.getPropertyNames().length; j++) {
					String componentPropertyName = componentType
							.getPropertyNames()[j];

					Type componentPropertyType = componentType.getSubtypes()[j];
					Object componentPropertyValue = componentType
							.getPropertyValue(element, j,
									(SessionImplementor) session);

					processProperty(session, auditConfiguration, auditEvent,
							element, componentPropertyName,
							componentPropertyValue, componentPropertyType,
							targetComponentAuditObject);
				}
			}
			property.setTargetComponentAuditObject(targetComponentAuditObject);
			auditObject.getAuditObjectProperties().add(property);
		} else if (elementType.isCollectionType()) {
			// collection of collections
		} else {
			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entityOwner.getClass().getName(), propertyName);

			SimpleObjectProperty property = new SimpleObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			property.setValue(auditConfiguration.getExtensionManager()
					.getPropertyValueConverter().toString(element));
			auditObject.getAuditObjectProperties().add(property);
		}
	}

}
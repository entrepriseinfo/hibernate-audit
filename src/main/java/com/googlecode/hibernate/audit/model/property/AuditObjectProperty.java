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
package com.googlecode.hibernate.audit.model.property;

import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.AuditObject;

public abstract class AuditObjectProperty {
	protected Long id;
	protected AuditObject auditObject;
	protected AuditTypeField auditField;
	protected Long index;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditObject getAuditObject() {
		return auditObject;
	}

	public void setAuditObject(AuditObject auditObject) {
		this.auditObject = auditObject;
	}

	public AuditTypeField getAuditField() {
		return auditField;
	}

	public void setAuditField(AuditTypeField auditField) {
		this.auditField = auditField;
	}

	public Long getIndex() {
		return index;
	}

	public void setIndex(Long index) {
		this.index = index;
	}
}
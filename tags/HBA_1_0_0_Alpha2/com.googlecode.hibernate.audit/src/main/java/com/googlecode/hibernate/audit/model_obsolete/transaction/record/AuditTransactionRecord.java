package com.googlecode.hibernate.audit.model_obsolete.transaction.record;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.googlecode.hibernate.audit.model_obsolete.AuditOperation;
import com.googlecode.hibernate.audit.model_obsolete.transaction.AuditTransactionObsolete;
import com.googlecode.hibernate.audit.model_obsolete.clazz.AuditClass;

@Entity
@Table(name = "AUDIT_TRANSACTION_RECORD")
@DiscriminatorColumn(name = "RECORD_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRAN_RECORD_ID_SEQ")
public abstract class AuditTransactionRecord {

	@Id
	@Column(name = "AUDIT_TRANSACTION_RECORD_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
	private Long id;

	/** the transaction id. */
	@ManyToOne
	@JoinColumn(name = "AUDIT_TRANSACTION_ID")
	private AuditTransactionObsolete auditTransaction;

	@Column(name = "OPERATION")
	@Enumerated(value = EnumType.STRING)
	private AuditOperation operation;

	/** the property class name. */
	@ManyToOne
	@JoinColumn(name = "AUDIT_CLASS_ID")
	private AuditClass auditClass;

	@Column(name = "AUDITED_ENTITY_ID")
	private String auditedEntityId;

/*	@OneToMany(mappedBy = "auditTransactionRecord")
	private Set<AuditTransactionRecordField> auditTransactionRecordFields = new HashSet<AuditTransactionRecordField>(); 
*/
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public String getAuditedEntityId() {
		return auditedEntityId;
	}

	public void setAuditedEntityId(String auditedEntityId) {
		this.auditedEntityId = auditedEntityId;
	}

/*	public void addAuditTransactionRecordField(AuditTransactionRecordField property) {
		property.setAuditTransactionRecord(this);
		auditTransactionRecordFields.add(property);
	}
	
	public Set<AuditTransactionRecordField> getAuditTransactionRecordFields() {
		return Collections.unmodifiableSet(auditTransactionRecordFields);
	}
*/
	/**
	 * @return the auditTransaction
	 */
	public AuditTransactionObsolete getAuditTransaction() {
		return auditTransaction;
	}

	/**
	 * @param auditTransaction the auditTransaction to set
	 */
	public void setAuditTransaction(AuditTransactionObsolete auditTransaction) {
		this.auditTransaction = auditTransaction;
	}

	/**
	 * @return the operation
	 */
	public AuditOperation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(AuditOperation operation) {
		this.operation = operation;
	}

	/**
	 * @return the auditClass
	 */
	public AuditClass getAuditClass() {
		return auditClass;
	}

	/**
	 * @param auditClass the auditClass to set
	 */
	public void setAuditClass(AuditClass auditClass) {
		this.auditClass = auditClass;
	}
}

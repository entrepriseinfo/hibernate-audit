package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.Session;
import org.hibernate.Query;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;


/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "AUDIT_CLASS_FIELD")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_CLASS_FIELD_ID_SEQUENCE")
public class AuditTypeField
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    protected static void checkTransaction(Session s) throws IllegalStateException
    {
        if (s.getTransaction() == null || !s.getTransaction().isActive())
        {
            throw new IllegalStateException("No active transaction found, bailing out");
        }
    }

    /**
     * DO NOT access from outside package and DO NOT relax the access restrictions!
     *
     * Returns a persistent instance of given field from the database. If "create" is set to false
     * and the field does not exist in the database, the method returns null. If "create" is set to
     * true and the field does not exist in the database, it is persisted, and then returned.
     *
     * @param session - the hibernate session to be used to interact with the database.
     *        It is assumed that a transaction is already started, and it will be committed outside
     *        the scope of this method.
     *
     * @return the persisted type (or null)
     */
    static AuditTypeField getInstanceFromDatabase(String name, AuditType type,
                                                  boolean create, Session session)
    {
        checkTransaction(session);

        String qs = "from AuditTypeField as f where f.name  = :name and f.type = :type";
        Query q = session.createQuery(qs);
        q.setString("name", name);
        q.setParameter("type", type);

        AuditTypeField persistedType = (AuditTypeField)q.uniqueResult();

        if (persistedType != null || !create)
        {
            return persistedType;
        }

        persistedType = new AuditTypeField();
        persistedType.setName(name);
        persistedType.setType(type);
        session.save(persistedType);
        return persistedType;
    }


    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_CLASS_FIELD_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "AUDIT_CLASS_ID")
    private AuditType type;

    @Column(name = "FIELD_NAME", nullable = false)
    private String name;

    @Column(name = "LABEL")
    private String label;

    // Constructors --------------------------------------------------------------------------------

    public AuditTypeField()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public AuditType getType()
    {
        return type;
    }

    public void setType(AuditType type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Convertor from string to native type.
     */
    public Object stringToValue(String s)
    {
        return type.stringToValue(s);
    }

    /**
     * @exception NullPointerException no type was previously set on this field.
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public String valueToString(Object o)
    {
        return type.valueToString(o);
    }

    /**
     * Falls back to database identity.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof AuditTypeField))
        {
            return false;
        }

        AuditTypeField that = (AuditTypeField)o;

        return id != null && id.equals(that.id);
    }

    /**
     * Falls back to database identity.
     */
    @Override
    public int hashCode()
    {
        if (id == null)
        {
            return 0;
        }

        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return "[" + type + "]." + name;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}

package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import java.lang.reflect.Method;
import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.Serializable;

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
@Table(name = "AUDIT_CLASS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_CLASS_SEQ")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("P")
public class AuditType
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditType.class);

    public static final Format oracleDateFormat =
        new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");

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
     * Returns a persistent instance of given type from the database. If "create" is set to false
     * and the type does not exist in the database, the method returns null. If "create" is set to
     * true and the type does not exist in the database, it is persisted, and then returned.
     *
     * @param session - the hibernate session to be used to interact with the database.
     *        It is assumed that a transaction is already started, and it will be committed outside
     *        the scope of this method.
     *
     * @return the persisted type (or null)
     */
    static AuditType getInstanceFromDatabase(Class c, boolean create, Session session)
    {
        checkTransaction(session);

        String qs = "from AuditType as a where a.className  = :className";
        Query q = session.createQuery(qs);
        q.setString("className", c.getName());

        AuditType persistedType = (AuditType)q.uniqueResult();

        if (persistedType != null || !create)
        {
            return persistedType;
        }

        persistedType = new AuditType(c);
        session.save(persistedType);
        return persistedType;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_CLASS_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "CLASS_NAME", nullable = false)
    private String className;

    @Column(name = "LABEL")
    private String label;

    @Transient
    protected Class classInstance;

    // Constructors --------------------------------------------------------------------------------

    public AuditType()
    {
    }

    /**
     * Only for use by classes of this package, do not expose publicly.
     */
    AuditType(Class c)
    {
        this.classInstance = c;

        if (classInstance != null)
        {
            this.className = classInstance.getName();
        }
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

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Class getClassInstance()
    {
        if (classInstance != null)
        {
            return classInstance;
        }

        if (className == null)
        {
            return null;
        }
        try
        {
            classInstance = Class.forName(this.className);
        }
        catch(ClassNotFoundException e)
        {
            throw new IllegalArgumentException("cannot resolve class " + className, e);
        }

        return classInstance;
    }

    public boolean isPrimitiveType()
    {
        return true;
    }

    /**
     * Runtime information that helps with the conversion of a "value" to its string representation;
     * for entities, the string representation is the string representation of their ids. This
     * information is useful at runtime, but it is not persisted explicitly, because we already
     * have the fully qualified Java class name in the table, and that is enough to figure out
     * that this type is an entity.
     */
    public boolean isEntityType()
    {
        return false;
    }

    public boolean isCollectionType()
    {
        return false;
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public String valueToString(Object o)
    {
        getClassInstance();

        if (!classInstance.isInstance(o))
        {
            throw new IllegalArgumentException(
                "the argument is not a " + classInstance.getName() + " instance");
        }

        try
        {
            Method m = classInstance.getMethod("toString");
            return (String)m.invoke(o);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException(
                "failed to invoke " + classInstance.getName() + "'s toString()", e);
        }
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public Serializable stringToValue(String s)
    {
        getClassInstance();

        // avoid reflection for often-used types

        if (String.class == classInstance)
        {
            return s;
        }
        else if (Integer.class == classInstance)
        {
            return Integer.parseInt(s);
        }
        else if (Long.class == classInstance)
        {
            return Long.parseLong(s);
        }
        else if (Boolean.class == classInstance)
        {
            return Boolean.parseBoolean(s);
        }
        else if (Date.class == classInstance)
        {
            try
            {
                return (Date)oracleDateFormat.parseObject(s);
            }
            catch(ParseException e)
            {
                throw new IllegalArgumentException(
                    "conversion of '" + s + "' to a Date value failed", e);
            }
        }

        String parseMethodName = classInstance.getName();
        parseMethodName = parseMethodName.substring(parseMethodName.lastIndexOf('.') + 1);
        parseMethodName = "parse" + parseMethodName;

        try
        {
            Method m = classInstance.getMethod(parseMethodName, String.class);
            return (Serializable)m.invoke(null, s);
        }
        catch(Exception e)
        {
            log.debug("failed to obtain value from string via reflection", e);
        }

        throw new RuntimeException("don't know to convert string to " + classInstance.getName());
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

        if (!(o instanceof AuditType))
        {
            return false;
        }

        AuditType that = (AuditType)o;

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
        return "PrimitiveType[" + (id == null ? "TRANSIENT" : id) + "][" + className + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}

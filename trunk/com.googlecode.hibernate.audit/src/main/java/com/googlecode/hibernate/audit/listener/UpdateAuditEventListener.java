package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.HibernateException;
import com.googlecode.hibernate.audit.model.Manager;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class UpdateAuditEventListener
    extends AbstractAuditEventListener implements SaveOrUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public UpdateAuditEventListener(Manager m)
    {
        super(m);
    }

    // SaveOrUpdateEventListener implementation ----------------------------------------------------

    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
    {
        // this will create an audit transaction and properly register the synchronizations
        createAuditTransaction(event.getSession());
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "UpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
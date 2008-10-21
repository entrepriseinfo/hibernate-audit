package com.googlecode.hibernate.audit.test.post_update;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.event.EventSource;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.HibernateAuditException;
import com.googlecode.hibernate.audit.LogicalGroupIdProvider;

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
@Test(sequential = true)
public class PostUpdateListenerRuntimeFailureTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testRuntimeFailureOnRealListener() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImpl sf = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());

            // we use a BreakableLogicalGroupIdProvider that throws an  ExoticRuntimeException smack
            // in the middle of event processing
            BreakableLogicalGroupIdProvider blgip = new BreakableLogicalGroupIdProvider();
            assert !blgip.isBroken();

            HibernateAudit.register(sf, blgip);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a.setName("alice");

            blgip.breakIt();

            s.update(a);

            try
            {
                s.getTransaction().commit();
                throw new Error("should've failed");
            }
            catch(HibernateAuditException e)
            {
                assert e.getCause() instanceof ExoticRuntimeException;
                Transaction t = s.getTransaction();

                // my mock TM dissasociates a rolled back transaction from the thread, so I cannot
                // test t.wasRolledBack() here
                assert !t.isActive();
                assert !t.wasCommitted();
            }
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    class BreakableLogicalGroupIdProvider implements LogicalGroupIdProvider
    {
        private boolean broken;

        BreakableLogicalGroupIdProvider()
        {
            broken = false;
        }

        public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
        {
            if (!broken)
            {
                return null;
            }

            throw new PostUpdateListenerRuntimeFailureTest.ExoticRuntimeException();
        }

        public boolean isBroken()
        {
            return broken;
        }

        public void breakIt()
        {
            broken = true;
        }
    }

    class ExoticRuntimeException extends RuntimeException
    {
    }
}

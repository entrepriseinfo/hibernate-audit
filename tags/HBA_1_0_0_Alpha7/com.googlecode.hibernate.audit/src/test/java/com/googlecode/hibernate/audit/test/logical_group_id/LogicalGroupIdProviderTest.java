package com.googlecode.hibernate.audit.test.logical_group_id;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.event.EventSource;
import org.hibernate.cfg.AnnotationConfiguration;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.logical_group_id.data.A;
import com.googlecode.hibernate.audit.test.logical_group_id.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.LogicalGroupIdProvider;

import java.util.List;
import java.util.Random;
import java.io.Serializable;

/**
 * Tests the runtime API
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class LogicalGroupIdProviderTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(LogicalGroupIdProviderTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testNullLogicalProviderId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);

        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();

            List<AuditTransaction> atxs = HibernateAudit.getTransactions(a.getId());

            assert atxs.size() == 1;

            AuditTransaction at = atxs.get(0);

            log.debug(at);

            assert at.getLogicalGroupId() == null;
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testConstantLogicalProviderId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);

        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            final long random = new Random().nextLong();

            LogicalGroupIdProvider lgip = new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es,
                                                      Serializable id,
                                                      Object entity)
                {
                    return new Long(random);
                }
            };

            HibernateAudit.enable(sf, lgip);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a = new A();
            a.setName("anna");

            s.save(a);

            s.getTransaction().commit();

            List<AuditTransaction> atxs = HibernateAudit.getTransactions(null);

            assert atxs.size() == 2;

            for(AuditTransaction atx: atxs)
            {
                assert new Long(random).equals(atx.getLogicalGroupId());
            }
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // explicitely disabled, because it breaks other tests (FailedCommitTest) TODO
    @Test(enabled = false) // TEST_OFF
    public void testInconsistentLogicalGroup() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);

        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            LogicalGroupIdProvider lgip = new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es,
                                                      Serializable id,
                                                      Object entity)
                {
                    if (entity instanceof A)
                    {
                        return new Long(10);
                    }
                    else if (entity instanceof B)
                    {
                        return new Long(20);
                    }

                    throw new IllegalStateException();
                }
            };

            HibernateAudit.enable(sf, lgip);

            A a = new A();
            a.setName("alice");

            B b = new B();
            b.setName("bob");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);
            s.save(b);

            try
            {
                s.getTransaction().commit();
                throw new Error("should've failed");
            }
            catch(IllegalStateException e)
            {
                log.debug(">>> " + e.getMessage());
                s.getTransaction().rollback();
            }
        }
        finally
        {
            HibernateAudit.disableAll();

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
}

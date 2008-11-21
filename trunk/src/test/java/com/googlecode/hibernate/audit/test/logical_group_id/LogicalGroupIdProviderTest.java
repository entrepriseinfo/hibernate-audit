package com.googlecode.hibernate.audit.test.logical_group_id;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.cfg.AnnotationConfiguration;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.logical_group_id.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.LogicalGroupIdProvider;
import com.googlecode.hibernate.audit.RootIdProvider;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;

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

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

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

            List<AuditEvent> events = at.getEvents();

            for(AuditEvent e: events)
            {
                assert e.getLogicalGroupId() == null;
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

    @Test(enabled = true)
    public void testConstantLogicalProviderId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

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

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf, lgip);

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
                for(AuditEvent e: atx.getEvents())
                {
                    assert new Long(random).equals(e.getLogicalGroupId());
                }
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

    @Test(enabled = true)
    public void testLatestTransactionByLogicalGroup_HBANotStarted() throws Exception
    {
        try
        {
            HibernateAudit.getLatestTransactionForLogicalGroup("doesn't matter");
            throw new Error("should have failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(">>> " + e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testLatestTransactionByLogicalGroup_EmptyAuditTables() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());

            assert null == HibernateAudit.getLatestTransactionForLogicalGroup("doesn't matter");
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

    @Test(enabled = true)
    public void testLatestTransactionByLogicalGroup_NoSuchLogicalGroup() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf); // null logical group id

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            assert null == HibernateAudit.getLatestTransactionForLogicalGroup("doesn't matter");
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

    @Test(enabled = true)
    public void testLatestTransactionByLogicalGroup_OneRecord() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            RootIdProvider rip = new RootIdProvider(A.class);
            HibernateAudit.register(sf, rip);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            rip.setRoot(a);
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            AuditTransaction tx = HibernateAudit.getLatestTransactionForLogicalGroup(a.getId());

            for(AuditEvent e: tx.getEvents())
            {
                assert a.getId().equals(e.getLogicalGroupId());
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

    @Test(enabled = true)
    public void testLatestTransactionByLogicalGroup_TwoRecords() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            RootIdProvider rip = new RootIdProvider(A.class);
            HibernateAudit.register(sf, rip);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            rip.setRoot(a);
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx1 = txs.get(0);

            AuditTransaction tx = HibernateAudit.getLatestTransactionForLogicalGroup(a.getId());

            for(AuditEvent e: tx.getEvents())
            {
                assert a.getId().equals(e.getLogicalGroupId());
            }

            s = sf.openSession();
            s.beginTransaction();

            a = (A)s.get(A.class, a.getId());
            rip.setRoot(a);

            a.setName("blah");
            s.update(a);

            s.getTransaction().commit();
            s.close();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;
            assert tx1.getId().equals(txs.get(0).getId());
            AuditTransaction tx2 = txs.get(1);

            tx = HibernateAudit.getLatestTransactionForLogicalGroup(a.getId());
            assert tx2.getId().equals(tx.getId());

            for(AuditEvent e: tx2.getEvents())
            {
                assert a.getId().equals(e.getLogicalGroupId());
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
}

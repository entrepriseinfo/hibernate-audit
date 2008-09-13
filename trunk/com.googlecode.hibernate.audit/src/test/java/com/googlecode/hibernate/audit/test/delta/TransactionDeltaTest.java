package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.TransactionDeltaImpl;
import com.googlecode.hibernate.audit.delta.EntityDeltaImpl;
import com.googlecode.hibernate.audit.delta.EntityDelta;

import java.util.Set;

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
public class TransactionDeltaTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testAddEntityDelta() throws Exception
    {
        TransactionDeltaImpl td = new TransactionDeltaImpl(new Long(1), null, null, null);

        assert td.getEntityDeltas().isEmpty();

        EntityDeltaImpl ed = new EntityDeltaImpl(new Long(2));

        assert td.addEntityDelta(ed);

        Set<EntityDelta> eds = td.getEntityDeltas();

        assert eds.size() == 1;
        assert eds.contains(ed);

        assert !td.addEntityDelta(ed);
        assert eds.size() == 1;

        EntityDeltaImpl ed2 = new EntityDeltaImpl(new Long(2));

        assert !td.addEntityDelta(ed2);
        assert eds.size() == 1;

        EntityDeltaImpl ed3 = new EntityDeltaImpl(new Long(3));
        assert td.addEntityDelta(ed3);

        assert eds.size() == 2;
        assert eds.contains(ed);
        assert eds.contains(ed3);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
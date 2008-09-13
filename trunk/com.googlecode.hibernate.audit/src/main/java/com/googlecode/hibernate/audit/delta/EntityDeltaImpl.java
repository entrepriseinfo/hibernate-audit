package com.googlecode.hibernate.audit.delta;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

/**
 * Encapsulates the set of changes applied to entity state during a transaction.
 *
 * An EntityDelta instance contains complete information allowing to recreate the state of the
 * entity at the end of the transaction, given that we have access to the state of the entity as
 * persisted just before the transaction started.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class EntityDeltaImpl implements EntityDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // the entity id
    private Serializable id;

    // TODO can be optimized by maintaining a map, keyed on variable name
    private Set<PrimitiveDelta> primitiveDeltas;
    private Map<String, CollectionDelta> collectionDeltas;

    // Constructors --------------------------------------------------------------------------------

    public EntityDeltaImpl(Serializable id)
    {
        this.id = id;
        primitiveDeltas = new HashSet<PrimitiveDelta>();
        collectionDeltas = new HashMap<String, CollectionDelta>();
    }

    // EntityDelta implementation ------------------------------------------------------------------

    public Serializable getId()
    {
        return id;
    }

    public Set<PrimitiveDelta> getPrimitiveDeltas()
    {
        return primitiveDeltas;
    }

    public Set<CollectionDelta> getCollectionDeltas()
    {
        return new HashSet<CollectionDelta>(collectionDeltas.values());
    }

    public PrimitiveDelta getPrimitiveDelta(String name)
    {
        for(PrimitiveDelta p: primitiveDeltas)
        {
            if (p.getName().equals(name))
            {
                return p;
            }
        }

        return null;
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * Not exposed in PrimitiveDelta as its usage makes sense only when creating the delta.
     *
     * @return true if delta is successfully added, false if there's already a delta corresponding
     *         to the same primitive.
     */
    public boolean addPrimitiveDelta(PrimitiveDelta d)
    {
        return primitiveDeltas.add(d);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (id == null)
        {
            return false;
        }

        if (!(o instanceof EntityDelta))
        {
            return false;
        }

        EntityDelta that = (EntityDelta)o;
        return id.equals(that.getId());
    }

    @Override
    public int hashCode()
    {
        return id == null ? 0 : id.hashCode();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
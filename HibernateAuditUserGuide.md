# Deployment #

Add `hibernate-audit.jar` in your Hibernate application classpath. You can get the jar file from the hibernate-audit maven repository located here: http://hibernate-audit.googlecode.com/svn/repository/releases

# Activation #
## hibernate-audit version 2.x ##

Including the hibernate audit jar file will activate the hibernate audit listener. If you do not want to activate the listener but to include the jar file then add the following property in hibernate.properties

`com.googlecode.hibernate.audit.listener.autoRegister=false`

## hibernate-audit verion 1.x ##
Register `com.googlecode.hibernate.audit.listener.AuditListener` as a listerner to your session factory for the following events:

  * post-insert
  * post-update
  * post-delete
  * pre-collection-update
  * pre-collection-remove
  * post-collection-recreate

**Hibernate configuration
```
...
<hibernate-configuration>
    <session-factory>
....

        <listener class="com.googlecode.hibernate.audit.listener.AuditListener"  type="post-insert"/>
        <listener class="com.googlecode.hibernate.audit.listener.AuditListener"  type="post-update"/>
        <listener class="com.googlecode.hibernate.audit.listener.AuditListener"  type="post-delete"/>
        <listener class="com.googlecode.hibernate.audit.listener.AuditListener"  type="pre-collection-update"/>
        <listener class="com.googlecode.hibernate.audit.listener.AuditListener"  type="pre-collection-remove"/>
        <listener class="com.googlecode.hibernate.audit.listener.AuditListener"  type="post-collection-recreate"/>
    </session-factory>
</hibernate-configuration>
```**

**Spring LocalSessionFactoryBean
```
     ....

    <bean id="com.googlecode.hibernate.audit.listener.AuditListener" class="com.googlecode.hibernate.audit.listener.AuditListener" scope="singleton"/>

     <bean id="mySessionFactory" class="org.springframework.orm.hibernate3.LocalSessionFactoryBean">

       ... application specific session factory properties......

       <property name="eventListeners">
            <map>
                <entry key="post-insert">
                    <list>
                        <ref local="com.googlecode.hibernate.audit.listener.AuditListener" />
                    </list>
                </entry>
                <entry key="post-update">
                    <list>
                        <ref local="com.googlecode.hibernate.audit.listener.AuditListener" />
                    </list>
                </entry>
                <entry key="post-delete">
                    <list>
                        <ref local="com.googlecode.hibernate.audit.listener.AuditListener" />
                    </list>
                </entry>
                <entry key="pre-collection-update">
                    <list>
                        <ref local="com.googlecode.hibernate.audit.listener.AuditListener" />
                    </list>
                </entry>
                <entry key="pre-collection-remove">
                    <list>
                        <ref local="com.googlecode.hibernate.audit.listener.AuditListener" />
                    </list>
                </entry>
                <entry key="post-collection-recreate">
                    <list>
                        <ref local="com.googlecode.hibernate.audit.listener.AuditListener" />
                    </list>
                </entry>
              </map>
        </property>
    </bean>
```**

# HBA-Specific Properties #

### `hba.configuration.observer.clazz` ###

`hba.configuration.observer.clazz` specifies application specific implementation of `com.googlecode.hibernate.audit.configuration.AuditConfigurationObserver` interface.

```
hba.configuration.observer.clazz=com.myapp.MyAppAuditSessionFactoryObserver
```

Using a custom `AuditConfigurationObserver` the application can specify application specific implementation that controls all extensions.
The `AuditConfigurationObserver.auditConfigurationCreated` method will be invoked at the end of the `SessionFactory` initialization process - see [Initializable.initialize(org.hibernate.cfg.Configuration)](http://www.hibernate.org/hib_docs/v3/api/org/hibernate/event/Initializable.html#initialize(org.hibernate.cfg.Configuration))

### `hba.record.empty.collections.on.insert` ###

Controls if we are going to record empty collections on insert - the default is true - e.g. we will record empty collection - it is useful if you do know that all collections in your model can't be null and if you want to save some space in the database to set this property to false. Once this is set to true then the empty collections will be treated as a null collections - e.g. no record in the audit tables for this collection property. This is only useful for INSERTs and not for UPDATEs - updates will still record empty collections when those collections are changed.

### `hba.audited-model.dynamic-update` ###

`hba.audited-model.dynamic-update` controll whether the audit framework will set the dynamic-update for every audited entity to true - note that this property have effect only when its value is true.
Using the property set to true will guarantee that the audit will capture all updates that happend for the audited domain (this statement does not include explicitly not audited entities/properties which are always excluded from the audit) - enabling this will cause some performance penalties because Hibernate will not use a single update statement for a particular entity type but it will use dynamic update statement that will be generated on the fly based on which entity properties were modified.
For more information check the [Hibernate site](http://www.hibernate.org)

```
hba.audited-model.dynamic-update=true
```

### `hba.mappingfile` ###

`hba.mappingfile` if set this specified the HBM location for the hibernate entities configuration.
This is useful only when the application want to specify a different mapping from the one that is provided with the audit framework. For example the application can change table names, column names and etc.
You can use the HBM file provided in the JAR file as a template and modify it according to your needs. Hibernate audit does not use hibernate annotations so you can control all mapping information from
the HBM file only.

If the hba.mappingfile property is not specified the the following mapping resolution is going to be applied.

1) Get the Hibernate dialect and try to search for a mapping file located in classpath resource com/googlecode/hibernate/audit/model/ with the name - 'dialect'-audit.hbm.xml - where the 'dialect' is computed using the following method

```
	protected String getDialectName(Dialect d) {
		if (d instanceof Oracle8iDialect || d instanceof Oracle9Dialect) {
			return "oracle";
		} else if (d instanceof SQLServerDialect) {
			return "sqlserver";
		} else if (d instanceof MySQLDialect) {
			return "mysql";
		} else if (d instanceof Sybase11Dialect || d instanceof SybaseAnywhereDialect || d instanceof SybaseASE15Dialect || d instanceof SybaseDialect) {
			return "sybase";
		} else if (d instanceof Cache71Dialect) {
			return "cache71";
		} else if (d instanceof DB2Dialect) {
			return "db2";
		} else if (d instanceof FrontBaseDialect) {
			return "frontbase";
		} else if (d instanceof H2Dialect) {
			return "h2";
		} else if (d instanceof HSQLDialect) {
			return "hsql";
		} else if (d instanceof InformixDialect) {
			return "informix";
		} else if (d instanceof IngresDialect) {
			return "ingres";
		} else if (d instanceof InterbaseDialect) {
			return "interbase";
		} else if (d instanceof JDataStoreDialect) {
			return "jdatastore";
		} else if (d instanceof MckoiDialect) {
			return "mckoi";
		} else if (d instanceof MimerSQLDialect) {
			return "mimersql";
		} else if (d instanceof PointbaseDialect) {
			return "pointbase";
		} else if (d instanceof PostgreSQLDialect) {
			return "postgresql";
		} else if (d instanceof PostgreSQLDialect || d instanceof ProgressDialect) {
			return "postgresql";
		} else if (d instanceof RDMSOS2200Dialect) {
			return "rdmsos2200";
		} else if (d instanceof SAPDBDialect) {
			return "sapdb";
		} else if (d instanceof TeradataDialect) {
			return "teradata";
		} else if (d instanceof TimesTenDialect) {
			return "timesten";
		}
		
		return d.getClass().getSimpleName();
	}

```
If we are unable to find this resource then the default mapping (which is part of the framework jar) is going to be used - the location is

com/googlecode/hibernate/audit/model/audit.hbm.xml

> 2) if we can't find the dialect then the default mapping

com/googlecode/hibernate/audit/model/audit.hbm.xml

will be used.

The framework provides also oracle dialect specific  default mapping - located
com/googlecode/hibernate/audit/model/oracle-audit.hbm.xml

So if you are using Oracle then this mapping will be used ( of course if the hba.mappingfile is not specified)

Note that TARGET\_ENTITY\_ID column is String if you do know that all your entities PK are surrogate keys that are numbers (e.g. Java type can be mapped to NUMBER) then it is a good thing to provide your own mapping that the sql-type is specified as NUMBER instead of VARCHAR or whatever the DB supports for String type. This way you can save some space.

# NOTE #
Currently the provided HBM configuration works with Oracle database or any other compatible database ( see the SQL types used in the audit.hbm.xml) - If you want to use the project with different type of database then you need to supply a modified version of the audit.hbm.xml file with the correct databases column types.
# Application Extensions #

### `AuditableInformationProvider` ###

Hibernate Audit framework will use this provider to determine if a particular entity or entity/property needs to be audited.
By default all entities and entity/properties are audited except for the hibernate audit entities - also you can't override this behavior because it prevents infinite loop.

### `ConcurrentModificationProvider` ###

If you want to use the Hibernate Audit optimistic concurrency solution then you have to provide you own implementation of the `com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationProvider`. The implemenation needs to return three important objects
  * `com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationLevelCheck` - which can be OBJECT or PROPERTY - the object means that if we detect simultaneously changes to the same object then the second transaction will get the `com.googlecode.hibernate.audit.exception.ConcurrentModificationException` and if it is property then this means that only if two transactions are trying to modify the same attribute (including collection attributes) on the same entity then this exception will be raise as well. Make sure that you read the Error Handling section for important information regarding throwing exceptions from Hibernate Audit library.
  * `com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationBehavior` THROW\_EXCEPTION and LOG - The THROW\_EXCEPTION means that `ConcurrentModificationException` will be thrown to the client and LOG means that this is going only to be logged and the save/update/delete will proceed.
  * Long loadAuditTransactionId - that represent from which transactionId we should start to look for concurrency modifications - usially this id is load before you load your entities and then it is keep until the save/update is usued.

**Note** For reliable concurrency check you have to provide at least one AuditLogicalGroup (entity type class and id) for all entities so that each Audit Object has associated AuditLogicalGroup - the reason for that is because during the concurrency check (just before the JTA/JDBC transaction ends) we will perform a pessimistic lock against all AuditLogicalGroup entities that participate in the current transaction. During that time if there is another transaction that has to change an entity that is part of the first transacation then it have also to lock the its corresponding AuditLogicalGroup which is already locked by the first transaction, so the second transaction will wait until the first transaction is done - This guarantees that we are not performing the concurrency check simultaneously for the same entity in different JTA/JDBC transactions.

### `PropertyValueConverter` ###

Whenever a value that is not entity or component needs to be stored in the audit tables then the audit will invoke this converter to convert object value into String.
If you do have property values that can't be converted properly to its String representation then you may consider to override the default implementation.

### `AuditLogicalGroupProvider` ###
If you want to have logical entity groups in your entity model where each logical group
has only one root entity then you can provide a custom AuditLogicalGroupProvider implementation that will determine for each Hibernate AuditEvent what is
the corresponding AuditLogicalGroup - the AuditLogicalGroup will define the type of the root entity class as well as its primary key id.
This is useful if you want to support fast lookups for related audit entities and reliable HibernateAudit optimistic concurrency (if enabled) -
for example if you have User entity domain then the root entity may be the User entity then you can quickly find all User related entities that were modified during a specified timeframe.
If AuditLogicalGroups are used then the HibernateAudit optimistic concurrency will use that to speed up the process of checkin for any concurrency issues.

### `SecurityInformationProvider` ###
If you want to capture who modified particular auditted entity then you have to provide custom application specific implementation of `com.googlecode.hibernate.audit.extension.security.SecurityInformationProvider`. You need also the register that so that Hibernate Audit will use the new implementation.

### `TransactionSyncronization` ###

Hibernate audit will delegate the transaction synchronization registration to this provider. The default implementation uses

```
eventSource.getTransaction().registerSynchronization(synchronization);
```

<b>NOTE:</b> If you are using automatic Hibernate session management (e.g. you do not explicitly have session.close() method calls in your code - for example when you are using software like Spring) that closes the session before all transaction sychronization hooks receive the beforeCompletion notification then you MUST provide a custom hibernate-audit TransactionSynchronization that will make sure that the audit synchronziation hook is called before the session is closed.

For example if you are using Spring then in this implementation you should use
```
org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(springSyncronization)
```

Where the springSynchronization should be a wrapper of the synchrozation parameter but will also provide order (e.g. the wrapper class should also implement `{org.springframework.core.Ordered` Spring interface)

### `AuditTransactionAttributeProvider` ###

Provides collection of entities that can be stored and associated to the current audit transaction. The hibernate audit will call the provider to get the list of entites that need to be persisted as part of the audit transaction. Default provider returns {{{null}} so no audit transaction attributes will be stored in the audit tables.

# Loading Data from Hibernate Audit tables #
There are two ways that you can use to load the data from the Audit Hibernate tables:
  * Directly querying the Hibernate Audit tables by using Hibernate API and the Hibernate Audit entities - you do that the same way as you are loading your custom entities. All Hibernate Audit entities will be automatically published in the same SessionFactory/Session as your application. All Hibernate Audit entity names are using fully qualified names (the name of the class of the entity) - this way we can guarantee that we do not have entity name collisions.
  * Also you can use Hibernate Audit `HibernateAuditInstantiator` class to intantiate an entity from a specific audit revision. Here is an example code : assuming that the implementation class of your entity that you need to load is com.example.entity.model.DemoEntity and it has primary key with id value 101. For the example will use audit transaction id of 23001
```
      Session session = .... ; // obtain your application Hibernate Session 
      com.example.entity.model.DemoEntity demoEntityAtRevision23001 = null;

      AuditType demoEntityAuditType = HibernateAudit.getAuditType(session, com.example.entity.model.DemoEntity.class.getName());
      demoEntityAtRevision23001 = (DemoEntity)HibernateAuditInstantiator.getEntity(session, demoEntityAuditType, "101", Long.valueOf(23001));
      
      // here you can access you entity as usual - note that any modifications to 
      // the entity won't be synhronized to the database. Exception to that rule are all entities that "reference" entities - Reference entity means that there won't be any representations for this entity in the Hibernate Audit tables so we are going to load the entity directly from the original table for that entity - then such entity will be actually managed by Hibernate and any modification will be synchronized to the database.
```

**Note:** Currently the `HibernateAuditInstantiator` only supports eager load - this means that any references to other entities will be loaded as well. Depend on the connected graph of objects at particular audit transaction a single load of an entity might involve loading of multiple other entities (from the same class or different classes). In future Hibernate Audit implementations this may change and instead of loading all connected entities to provide smart proxies that will load the necessary data only when it is needed.

# Error Handling #

All exceptions are logged and if they are not recoverable then they are passed to the Hibernate and then to the application. You need to setup correctly the Log4j library before you can see those log messages. All Hibernate Audit log categories starts with
`com.googlecode.hibernate.audit`

**Note:** [Hibernate](http://www.hibernate.org) and [Spring Framework](http://www.springframework.org) will notify all `javax.transaction.Synchronization` objects for before and after completion in `try/catch` block that swallows `java.lang.Throwable` and only logging them in ERROR log level. This means that if the Hibernate Audit synchronization throws an exception then this exception won't be propagated to the client - the only thing that the Hibernate Audit framework can do when such exception occurs is to try to mark the current transaction for rollback. If you are using Log4j as your logging framework then you can fix this problem using the following workaround: Create a new class that you can invoke after the Log4j is initialized and add Log4j special appenders for both Hibernate and Spring loggers (assuming that you do have both frameworks)

```
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.hibernate.transaction.JDBCTransaction;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

/**
 * @author <a href="mailto:kchobantonov@yahoo.com">Krasimir Chobantonov</a>
 */
public final class AuditExceptionPropagation {

    private static final Logger HIBERNATE_TRANSACTION_LOG = Logger.getLogger(JDBCTransaction.class);
    private static final Logger SPRING_TRANSACTION_LOG = Logger.getLogger(TransactionSynchronizationUtils.class);

    public void init() {
        interceptLog(HIBERNATE_TRANSACTION_LOG);
        interceptLog(SPRING_TRANSACTION_LOG);
    }

    private void interceptLog(Logger logger) {
        if (Level.OFF.equals(logger.getLevel())) {
            logger.setLevel(Level.ERROR);
        }

        logger.addAppender(new AppenderSkeleton() {

            @Override
            public boolean requiresLayout() {
                return false;
            }

            @Override
            public void close() {
            }

            @Override
            protected void append(LoggingEvent event) {
                if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException) event.getThrowableInformation().getThrowable();
                }
            }
        });
    }

}
```


This implementation will re-throw any RuntimeException when the ThrowableInformation is provided (make sure that you do not have custom Log4j exception handling that will remove this information from the `LoggginEvent`.

For more information about Hibernate and this problem check [Hibernate JIRA issue HHH-3543](http://opensource.atlassian.com/projects/hibernate/browse/HHH-3543)

# Known Limitations and Workarounds #

  * Hibernate Audit framework currently does not capture `java.util.Map` entity persistence attributes.
    1. Currently there is no workaround solution without modifying the Hibernate Audit framework
  * Hibernate Audit framework currently does not support entity/component attribute values (that are not references to entity or component or collection of such) for which the `java.lang.String` representation is greater than 4000 chars (all entities and componenets are supported). All such properties must not be marked as auditted because of potential RuntimeExceptions that may be raise during the save to the database.
    1. There is a workaround for this without modifying the Hibernate Audit library. You need override the Hibernate Audit entity mapping document and to change the type of the `ATTRIBUTE_VALUE_TXT` column from `VARCHAR(4000)` to CLOB and also to use custom `UserType` - `org.hibernate.type.StringClobType`, then you need to make sure that you are using this new mapping file by using the `hba.mappingfile` property. You also need to provide the a custom implementation of `com.googlecode.hibernate.audit.extension.event.AuditLogicalGroupProvider` and register that approperiately with the Hiberate Audit framework if your entity property type can't be converted properly to java.lang.String - for example `java.io.InputStream` and etc. **Note:** This workaround has disadvantages - the performance will not be very good compared to the original implementation.


# FAQ #

  * It seems that Hibernate Audit framework does not capture changes that are made in some other Hibernate listeners, what may be wrong ?
    1. Make sure that the Hibernate Audit listener is the last registered listener and it is the last in the transaction synchronization list so that the beforeComplete for the Hibernate Audit listener will be invoked last. Also make sure that in all other Hibernate listeners that changes the database that those changes happens only in beforeComplete and that the session flush is called at the end.
    1. Check if there is a code in the execution path that swallows(ignore) any exceptions that prevent the Hibernate Audit listener to add all audit entities into the session and invoke the flush to send those to the database, make sure that in such cases the transaction is rolled back if you want to guarantee that you will not end up having missing audit entries in the database - check also the section that described how to setup a custom Log4J appender that will throw any RuntimeException that can happen during the transaction synchronizations calls from Hibernate (note that Hibernate will swallows Throwable and it will just log it)
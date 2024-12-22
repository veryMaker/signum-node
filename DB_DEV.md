# Working with the database

## Migration

To incrementally upgrade the data base model, the migration tool "flyway" is being used. There are two ways migration is 
done in "Signum Node".

1. Using declarative SQL
2. Using imperative JDBC Logic in Java


The migrations are being applied _automagically_ each time the node software starts.  

### Declarative SQL

In the folder `./resources/db` all incremental SQL updates are stored. This is the most common way to do migrations.
If you need programmatical logic, i.e. for new calculations, you have to use the JDBC Logic.
Flyway uses a file name based version (semantic version) mechanism. So, the correct order can be always guaranteed.


`V<MajorVersion>_<MinorVersion>__<CustomName>.sql`

Example:

`V7_2__account_balance.sql`
`V7_4__account_clean.sql`
`V10_1__initial_transaction_block_id_index.sql`

Mind the double `_` between minor version and name.

> Never change the files once a migration was rolled out. Always add changes in new files

The migrations are split per database, as different databases might need different adjustments or fixes.
For sake of clarity it is always good to keep the version numbers more or less identical for all databases,
such it is easier to follow the changes. 


### Imperative JDBC Logic

If you need to run some calculation for a migration, then it might be necessary to write some 
Java code. The logic has to be put into `./src/db/sql/migration` and follows the same file naming rules as described in [Declarative SQL](#declarative-sql)   


## Generate Java Classes from Database Model

The current implementation uses the [Jooq Gradle Plugin](https://github.com/etiennestuder/gradle-jooq-plugin?tab=readme-ov-file#compatibility) for JDK 11 to generate
the Java classes from the database.

In case, some (structural)[1] database model updates are done it is necessary to re-generate the Java classes.

Run `./gradlew generateJooq`

The resulting classes are written to `./temp/generated/jooq`. Copy the relevant code over to `./src/brs/schema`

> At this time, no incremental build is being considered. So, all classes are always re-generated.


[1] new columns, tables, renamings... not really relevant for indexes, unless you use them in further code

# ktorm-ksp-ext-batch


<p align="center">
    <img src="https://raw.githubusercontent.com/kotlin-orm/ktorm-docs/master/source/images/logo-full.png" alt="Ktorm" width="300" />
</p>
<p align="center">
    <a href="https://github.com/kotlin-orm/ktorm-ksp-ext-batch/actions/workflows/build.yml">
        <img src="https://github.com/kotlin-orm/ktorm-ksp-ext-batch/actions/workflows/build.yml/badge.svg" alt="Build Status" />
    </a>
    <a href="https://search.maven.org/search?q=g:%22org.ktorm%22">
        <img src="https://img.shields.io/maven-central/v/org.ktorm/ktorm-ksp-ext-batch-mysql.svg?label=Maven%20Central" alt="Maven Central" />
    </a>
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/license-Apache%202-blue.svg?maxAge=2592000" alt="Apache License 2" />
    </a>
</p>

# ktorm-ksp-ext-batch

:us: English | :cn: [简体中文](README_cn.md)

```ktorm-ksp``` plugin that generates batch operation functions (```addAll``` & ```updateAll```) for entity classes.
```addAll``` is implemented using ```bulkInsert```, which has better performance than ```database.batchInsert```

Supported databases:

- MySQL
- PostgreSQL
- SQLite

# Quick Start

1. This plugin depends on ```ktorm-ksp```, You need to add the relevant dependency configuration of ```ktorm-ksp``` to the project
2. Add one of the following plugin dependencies in ```build.gradle``` or ```pom.xml``` depending on the database used
```groovy
// Groovy DSL
dependencies {
  ksp 'org.ktorm.ktorm-ksp-ext-batch-mysql:$ktorm_version' // MySQL
  ksp 'org.ktorm.ktorm-ksp-ext-batch-sqlite:$ktorm_version'  // SQLite
  ksp 'org.ktorm.ktorm-ksp-ext-batch-postgresql:$ktorm_version' // PostgreSQL
}
```
```kotlin
// Kotlin DSL
ksp("org.ktorm.ktorm-ksp-ext-batch-mysql:$ktorm_version") // MySQL
ksp("org.ktorm.ktorm-ksp-ext-batch-sqlite:$ktorm_version")  // SQLite
ksp("org.ktorm.ktorm-ksp-ext-batch-postgresql:$ktorm_version") // PostgreSQL
```
```xml
<!-- maven -->
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-plugin</artifactId>
        <version>${kotlin.version}</version>
        <configuration>
          <compilerPlugins>
            <compilerPlugin>ksp</compilerPlugin>
          </compilerPlugins>
          <sourceDirs>
            <sourceDir>src/main/kotlin</sourceDir>
            <sourceDir>target/generated-sources/ksp</sourceDir>
          </sourceDirs>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>com.dyescape</groupId>
            <artifactId>kotlin-maven-symbol-processing</artifactId>
            <version>1.3</version>
          </dependency>
          <dependency>
            <groupId>org.ktorm</groupId>
            <artifactId>ktorm-ksp-compiler</artifactId>
            <version>${ktorm_version}</version>
          </dependency>
          <!-- MySQL -->
          <dependency>
            <groupId>org.ktorm</groupId>
            <artifactId>ktorm-ksp-ext-batch-mysql</artifactId>
            <version>${ktorm_version}</version>
          </dependency>
          <!-- SQLite -->
          <dependency>
            <groupId>org.ktorm</groupId>
            <artifactId>ktorm-ksp-ext-batch-sqlite</artifactId>
            <version>${ktorm_version}</version>
          </dependency>
          <!-- PostgreSQL -->
          <dependency>
            <groupId>org.ktorm</groupId>
            <artifactId>ktorm-ksp-ext-batch-postgresql</artifactId>
            <version>${ktorm_version}</version>
          </dependency>
        </dependencies>
        <executions>
          <execution>
            <id>compile</id>
            <phase>compile</phase>
            <goals>
              <goal>compile</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>  
```



# Database Dialects

When the ```addAll``` function generates SQL statements for bulk insert, In order to make the 
```column default value``` and ```column auto increment``` take effect as much as possible, The generated bulk insert SQL 
statement will also be different

## MySQL

MySQL uses the ```default``` keyword to make ```column default value```, ```auto_increment``` effective when bulk inserting

```SQL
create table employee
(
    id   int(11) primary key auto_increment,
    age  int(11) default 18,
    name varchar(32),
    job  varchar(32)
);
```
```kotlin
// used addAll bulk insert
database.employees.addAll(
  listOf(
    Employee(id=null, age=null, name="jack", job=null),
    Employee(id=null, age=null, name="luck", job=null),
  )
)
```
```SQL
-- generated SQL
insert into employee(id, age, name, job)
values (default, default, 'jack', default),
       (default, default, 'luck', default);
```
inserted result:

| id  | age | name | job  |
|-----|-----|------|------|
| 1   | 18  | jack | null |
| 2   | 18  | luck | null |

When to use the ```default``` keyword?

- The property value is ```null``` and the ```nullAsDefaultValue``` parameter value of the ```addAll``` method is ```true```
- Unassigned property. [What is Unassigned property](#what-is-unassigned-property)

## PostgreSQL

Postgre SQL uses the ````default```` keyword to make the ```column default value```, ```serial``` effective when bulk inserting

```SQL
create table employee
(
    id   serial primary key,
    age  varchar(128) default 18,
    name varchar(32),
    job  varchar(32)
);
```
```kotlin
// used addAll bulk insert
database.employees.addAll(
  listOf(
    Employee(id=null, age=null, name="jack", job=null),
    Employee(id=null, age=null, name="luck", job=null),
  )
)
```
```SQL
-- generated SQL
insert into employee(id, age, name, job)
values (default, default, 'jack', default),
       (default, default, 'luck', default);
```
inserted result:

| id  | age | name | job  |
|-----|-----|------|------|
| 1   | 18  | jack | null |
| 2   | 18  | luck | null |

When to use the ```default``` keyword?

- The property value is ```null``` and the ```nullAsDefaultValue``` parameter value of the ```addAll``` method is ```true```
- Unassigned property. [What is Unassigned property](#what-is-unassigned-property)

## SQLite

Because the ```default``` keyword is not supported in SQLite, use ```null``` to insert the
```unassigned property``` in the bulk insert statement

How to make the ``column default value``` and ```autoincrement``` take effect when bulk inserting?

- autoincrement

  SQLite allows inserting a value of ```null```, in which case the value is automatically generated

- column default value

  The ```column default value``` does not take effect when the inserted value is ```null```, There are two workarounds:
1、If the entity class is a ```data class```, then we can set the default value for the property at the kotlin code. 2、
  Use ```on conflict replace``` for the default value column when creating the table, Please refer to the following SQL statement


```SQL
create table employee
(
  id   integer primary key autoincrement,
  age  integer not null on conflict replace default 18,
  name varchar,
  job  varchar   
);
```
```kotlin
// used addAll bulk insert
database.employees.addAll(
  listOf(
    Employee(id=null, age=null, name="jack", job=null),
    Employee(id=null, age=null, name="luck", job=null),
  )
)
```
```SQL
-- generated SQL
insert into employee(id, age, name, job)
values (null, null, 'jack', null),
       (null, null, 'luck', null);
```
inserted result:

| id  | age | name | job  |
|-----|-----|------|------|
| 1   | 18  | jack | null |
| 2   | 18  | luck | null |

Through the ```on conflict replace``` statement, ```null``` will be automatically replaced with the default value of the 
column when inserting, but the disadvantage is that this column must be ```not null```

## What is Unassigned property

In ktorm, properties that are assigned property that are unassigned property have different effects on the generated SQL.
Only the entity class based on the ```Entity``` interface has the concept of unassigned property. 

```kotlin
@Table
public interface Department : Entity<Department> {
    @PrimaryKey
    public val id: Int
    public var name: String
    public var mixedCase: String?
}

// ktorm-ksp generated code
public fun Department(
    id: Int? = Undefined.of(),
    name: String? = Undefined.of(),
    mixedCase: String? = Undefined.of()
) {
    // ignore code
}

// Use the built-in factory method of ktorm to create an instance, at this time all properties are unassigned
val department1 = org.ktorm.entity.Entity.create<Department>()
// assign value to the id property, at this time the id property is assigned
department1.id = 1

// Use 'constructor' in generated code to create an instance without passing in any parameters, 
// and all properties are unassigned at this time
val department2 = Department()
// assign value to the id property, at this time the id property is assigned
department2.id = 1

// Use 'constructor' in generated code to create an instance and pass in the id parameter. 
// At this time, the id property is assigned, and the other properties are unassigned
val department3 = Department(id = 1)
```





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

:us: [English](README.md) | :cn: 简体中文

```ktorm-ksp```插件, 为实体类生成批处理操作函数(```addAll``` & ```updateAll```)

支持的数据库列表:

- MySQL
- PostgreSQL
- SQLite

# 快速开始

1. 本插件依赖于```ktorm-ksp```, 需先在项目中添加```ktorm-ksp```的相关依赖配置
2. 根据使用的数据库在```build.gradle```或```pom.xml```中添加以下其中一个插件依赖
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



# 不同数据库方言实现

```addAll```函数在生成批量插入的SQL语句时, 为了尽量保持```列默认值```、```自增主键```生效, 并且由于不同SQL数据库的实现差异, 其生成的批量插入SQL语句也会有所不同. 下面会说明具体的生成方案.

## MySQL

MySQL在插入时由```default```关键字使用```列默认值```、```自增主键```

```SQL
create table employee
(
    id   int(11) primary key auto_increment,
    age  int(11) default 18,
    name varchar(32)
);

-- addAll生成语句
insert into employee(id, age, name) values (default, default, 'jack');
```

以上插入语句执行后, id值为```自增主键```生成值, age为```列默认值```18. 如果表列没有设置默认值并且列不是```not null```时, 使用```default```关键字最终会插入```null```值

什么情况下会使用```default```关键字?

- 当字段值为```null```且```addAll```方法的```nullAsDefaultValue```参数为```true```时
- 字段未赋值 (具体请参考[什么是字段未赋值](#什么是字段未赋值))

## PostgreSQL

PostgreSQL在插入时由```default```关键字使用```列默认值```、```自增主键```

```SQL
create table employee
(
    id   serial primary key,
    age  varchar(128) default 18,
    name varchar(32)
);

-- addAll生成语句
insert into Employee(id, age, name) values (default, default, 'jack');
```

以上插入语句执行后, id值为```自增主键```生成值, age为```列默认值```18. 如果表列没有设置默认值并且列不是```not null```时, 使用```default```关键字最终会插入```null```值

什么情况下会使用```default```关键字?

- 当字段值为```null```且```addAll```方法的```nullAsDefaultValue```参数为```true```时
- 字段未赋值 (具体请参考[什么是字段未赋值](#什么是字段未赋值))

## SQLite

由于SQLite中不支持```default```关键字, 在插入语句中对于```未赋值```的字段, 只能使用```null```进行插入.

那么如何在批量插入时让```列默认值```、```自增主键```生效呢？

- 自增主键

    SQLite允许插入值为```null```, 此时会自动生成自增主键值

- 列默认值

    插入值为```null```时默认值无法生效, 解决方法有两种: 1、如果实体类是data class, 那么我们可以代码层面为字段设置默认值. 2、
创建表时对默认值列做一些处理, 请参考下面的SQL语句
```SQL
create table employee 
(
    id integer primary key  autoincrement,
    name varchar,
    age integer not null on conflict replace default 18
);

-- addAll生成语句, age插入null后会使用默认值:18
insert into "employee" (id, name, age) values (null, 'name', null);
```
通过```on conflict replace```语句, 会在插入时自动将```null```自动替换成字段默认值, 不过缺点就是这个字段必须是```not null```

## 什么是字段未赋值

在ktorm中, 字段已赋值和字段未赋值对生成的SQL会有不同的影响. 只有基于```Entity```接口的实体类, 才有字段未赋值的概念, 具体请参考下面的代码 

```kotlin
@Table
public interface Department : Entity<Department> {
    @PrimaryKey
    public val id: Int
    public var name: String
    public var mixedCase: String?
}

// ktorm-ksp生成的伪构造函数
public fun Department(
  id: Int? = Undefined.of(),
  name: String? = Undefined.of(),
  mixedCase: String? = Undefined.of()
) {
    // 省略具体代码实现
}

// 使用ktorm内置的工厂方法创建实例, 此时所有字段均为未赋值
val department1 = org.ktorm.entity.Entity.create<Department>()
// 对id字段赋值, 此时id字段为已赋值
department1.id = 1

// 使用伪构造函数创建实例, 不传入任何参数, 此时所有字段均为未赋值
val department2 = Department()
// 对id字段赋值, 此时id字段为已赋值
department2.id = 1

// 使用伪构造函数创建实例并传入id参数, 此时id字段为已赋值, 其他字段均为未赋值
val department3 = Department(id=1)
```




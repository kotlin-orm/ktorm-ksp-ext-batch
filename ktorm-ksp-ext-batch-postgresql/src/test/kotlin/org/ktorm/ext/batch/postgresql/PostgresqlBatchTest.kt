package org.ktorm.ext.batch.postgresql

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions
import org.junit.Test

public class PostgresqlBatchTest: BasePostgresqlTest() {

    @Test
    public fun `addAll without default value`() {
        val (result1, result2) = twiceCompile(
            SourceFile.kotlin(
                "source.kt",
                """
                import org.ktorm.ksp.api.*
                import org.ktorm.entity.Entity
                import org.ktorm.database.Database
                import org.ktorm.entity.toList
                import org.ktorm.dsl.inList
                import org.ktorm.dsl.gt
                import org.ktorm.entity.filter

                @Table("t_department")
                interface Department : Entity<Department> {
                    @PrimaryKey
                    val id: Int
                    var name: String
                    var location: String
                    var number: Int
                }

                @Table("t_employee")
                data class Employee (
                    @PrimaryKey
                    val id: Int,
                    var name: String,
                    @Column("department_id")
                    var departmentId: Int
                )

                object TestBridge {
                    fun addAll(database: Database) {
                        var departments = listOf(
                            Department(id = 1, name = "department1", location = "location1", number = 1),
                            Department(id = 2, name = "department2", location = "location2", number = 2),
                        )
                        database.departments.addAll(departments)
                        departments = database.departments.toList()
                        assert(departments.size == 2)
                        assert(departments[0].id == 1)
                        assert(departments[0].name == "department1")
                        assert(departments[0].location == "location1")
                        assert(departments[0].number == 1)
                        assert(departments[1].id == 2)
                        assert(departments[1].name == "department2")
                        assert(departments[1].location == "location2")
                        assert(departments[1].number == 2)
                        
                        var employees = listOf(
                            Employee(id = 1, name = "employee1", departmentId = 1),
                            Employee(id = 2, name = "employee2", departmentId = 2),
                        )
                        database.employees.addAll(employees)
                        employees = database.employees.toList()
                        assert(employees.size == 2)
                        assert(employees[0].id == 1)
                        assert(employees[0].name == "employee1")
                        assert(employees[0].departmentId == 1)
                        assert(employees[1].id == 2)
                        assert(employees[1].name == "employee2")
                        assert(employees[1].departmentId == 2)
                    }
                }
                """,
            )
        )
        Assertions.assertThat(result1.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        Assertions.assertThat(result2.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        result2.invokeBridge("addAll", database)
    }

    @Test
    public fun `addAll with default value`() {
        val (result1, result2) = twiceCompile(
            SourceFile.kotlin(
                "source.kt",
                """
                import org.ktorm.ksp.api.*
                import org.ktorm.entity.Entity
                import org.ktorm.database.Database
                import org.ktorm.entity.toList
                import org.ktorm.dsl.inList
                import org.ktorm.dsl.gt
                import org.ktorm.entity.filter

                @Table("t_department")
                interface Department : Entity<Department> {
                    @PrimaryKey
                    val id: Int
                    var name: String
                    var location: String
                    var number: Int
                }

                @Table("t_employee")
                data class Employee (
                    @PrimaryKey
                    val id: Int? = null,
                    var name: String? = null,
                    @Column("department_id")
                    var departmentId: Int? = null
                )

                object TestBridge {
                    fun addAll(database: Database) {
                        var departments = listOf(
                            Department(name = "department1"),
                            Department(name = "department2"),
                        )
                        database.departments.addAll(departments)
                        departments = database.departments.toList()
                        assert(departments.size == 2)
                        assert(departments[0].id == 1)
                        assert(departments[1].id == 2)
                        for (department in departments) {
                            assert(department.location == "default_location")
                            assert(department.number == 100)
                        }

                        var employees = listOf(
                            Employee(),
                            Employee(),
                        )
                        database.employees.addAll(employees)
                        employees = database.employees.toList()
                        assert(employees.size == 2)
                        assert(employees[0].id == 1)
                        assert(employees[1].id == 2)
                        for (employee in employees) {
                            assert(employee.name == "default_name")
                            assert(employee.departmentId == null)
                        }
                    }
                }
                """,
            )
        )
        Assertions.assertThat(result1.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        Assertions.assertThat(result2.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        result2.invokeBridge("addAll", database)
    }

    @Test
    public fun `addAll with references`() {
        val (result1, result2) = twiceCompile(
            SourceFile.kotlin(
                "source.kt",
                """
                import org.ktorm.ksp.api.*
                import org.ktorm.entity.Entity
                import org.ktorm.database.Database
                import org.ktorm.entity.toList
                import org.ktorm.dsl.inList
                import org.ktorm.dsl.gt
                import org.ktorm.entity.filter

                @Table("t_department")
                interface Department : Entity<Department> {
                    @PrimaryKey
                    val id: Int
                    var name: String
                    var location: String
                    var number: Int
                }

                @Table("t_employee")
                interface Employee : Entity<Employee> {
                    @PrimaryKey
                    val id: Int
                    var name: String
                    @References("department_id")
                    var department: Department?
                }

                object TestBridge {
                    fun addAll(database: Database) {
                        var departments = listOf(
                            Department(name = "department1"),
                            Department(name = "department2"),
                        )
                        database.departments.addAll(departments)
                        departments = database.departments.toList()
                        var employees = listOf(
                            Employee(name = "employee1", department = departments[0]),
                            Employee(name = "employee2", department = departments[1]),
                            Employee(name = "employee3", department = null),
                        )
                        database.employees.addAll(employees)
                        employees = database.employees.toList()
                        assert(employees.size == 3)
                        assert(employees[0].name == "employee1")
                        assert(employees[0].department?.name == "department1")
                        assert(employees[1].name == "employee2")
                        assert(employees[1].department?.name == "department2")
                        assert(employees[2].name == "employee3")
                        assert(employees[2].department == null)
                    }
                }
                """,
            )
        )
        Assertions.assertThat(result1.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        Assertions.assertThat(result2.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        result2.invokeBridge("addAll", database)
    }

    @Test
    public fun updateAll() {
        val (result1, result2) = twiceCompile(
            SourceFile.kotlin(
                "source.kt",
                """
                import org.ktorm.ksp.api.*
                import org.ktorm.entity.Entity
                import org.ktorm.database.Database
                import org.ktorm.entity.toList
                import org.ktorm.dsl.inList
                import org.ktorm.dsl.gt
                import org.ktorm.entity.filter

                @Table("t_department")
                interface Department : Entity<Department> {
                    @PrimaryKey
                    val id: Int
                    var name: String
                    var location: String?
                    var number: Int
                }

                object TestBridge {
                    fun updateAll(database: Database) {
                        val list = listOf(
                            Department(name = "department1", location = null),
                            Department(name = "department2", location = null),
                        )
                        database.departments.addAll(list)
                        var items = database.departments.toList()
                        for (item in items) {
                            item.location = "Beijing"
                            item.number = 999
                        }
                        database.departments.updateAll(items)
                        items = database.departments.toList() 
                        assert(items.size == 2)
                        for (item in items) {
                            assert(item.location == "Beijing")
                            assert(item.number == 999)
                        }
                    }
                }
                """,
            )
        )
        Assertions.assertThat(result1.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        Assertions.assertThat(result2.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        result2.invokeBridge("updateAll", database)
    }
}

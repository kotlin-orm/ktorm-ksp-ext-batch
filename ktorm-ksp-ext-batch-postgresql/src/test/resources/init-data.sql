create table t_department
(
    id       serial primary key,
    name     varchar(128),
    location varchar(128) default 'default_location',
    number   int          default 100
);


create table t_employee
(
    id            serial primary key,
    name          varchar(128) default 'default_name',
    department_id int
);

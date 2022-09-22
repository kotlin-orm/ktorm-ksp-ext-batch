create table t_department
(
    id       serial       not null primary key,
    name     varchar(128) not null,
    location varchar(128) not null default 'default_location',
    number   int          not null default 100
);


create table t_employee
(
    id            serial       not null primary key,
    name          varchar(128) not null default 'default_name',
    department_id int
);

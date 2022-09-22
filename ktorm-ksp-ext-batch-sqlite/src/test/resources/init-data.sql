create table t_department
(
    id       integer      primary key autoincrement,
    name     varchar(128) not null,
    location varchar(128) not null on conflict replace default 'default_location',
    number   int          not null on conflict replace default  100
);


create table t_employee
(
    id            integer      primary key autoincrement,
    name          varchar(128) not null on conflict replace default 'default_name',
    department_id integer
);



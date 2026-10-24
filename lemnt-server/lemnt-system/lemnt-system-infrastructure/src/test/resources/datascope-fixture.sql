CREATE ALIAS FIND_IN_SET FOR "com.lemnt.system.infrastructure.auth.mapper.DataScopeSqlIntegrationTest.findInSet";
CREATE TABLE sys_dept (
    dept_id BIGINT PRIMARY KEY, parent_id BIGINT, ancestors VARCHAR(100), dept_name VARCHAR(50),
    order_num INT, leader VARCHAR(50), phone VARCHAR(50), email VARCHAR(50), status VARCHAR(1),
    del_flag VARCHAR(1), create_by VARCHAR(50), create_time TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP
);
CREATE TABLE sys_user (
    user_id BIGINT PRIMARY KEY, dept_id BIGINT, user_name VARCHAR(50), nick_name VARCHAR(50),
    email VARCHAR(50), phonenumber VARCHAR(50), sex VARCHAR(1), avatar VARCHAR(100), password VARCHAR(100),
    status VARCHAR(1), del_flag VARCHAR(1), login_ip VARCHAR(50), login_date TIMESTAMP, pwd_update_date TIMESTAMP,
    create_by VARCHAR(50), create_time TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP, remark VARCHAR(100)
);
CREATE TABLE sys_role (
    role_id BIGINT PRIMARY KEY, role_name VARCHAR(50), role_key VARCHAR(50), role_sort INT,
    data_scope VARCHAR(1), status VARCHAR(1), del_flag VARCHAR(1), create_time TIMESTAMP, remark VARCHAR(100)
);
CREATE TABLE sys_user_role (user_id BIGINT, role_id BIGINT);
CREATE TABLE sys_role_dept (role_id BIGINT, dept_id BIGINT);
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, status, del_flag)
VALUES (10, 0, '0', 'own', 1, '1', '0'), (11, 10, '0,10', 'child', 1, '1', '0'),
       (20, 0, '0', 'outside', 2, '1', '0');
INSERT INTO sys_user (user_id, dept_id, user_name, status, del_flag)
VALUES (100, 10, 'self', '1', '0'), (101, 10, 'colleague', '1', '0'),
       (110, 11, 'child', '1', '0'), (200, 20, 'outside', '1', '0'), (300, 10, 'deleted', '1', '1');
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag)
VALUES (7, 'local', 'local', 1, '3', '1', '0'), (8, 'outside', 'outside', 2, '3', '1', '0');
INSERT INTO sys_user_role VALUES (100, 7), (200, 8);
INSERT INTO sys_role_dept VALUES (7, 20);

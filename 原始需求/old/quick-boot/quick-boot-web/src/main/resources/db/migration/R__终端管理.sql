alter table sys_oauth_client
    add client_name varchar(50) null comment '客户端名称' after id;

alter table sys_oauth_client
    add public_key varchar(512) null comment '公钥';

alter table sys_oauth_client
    add private_key varchar(250) null comment '私钥';

alter table sys_oauth_client
    add status varchar(5) null comment '状态';


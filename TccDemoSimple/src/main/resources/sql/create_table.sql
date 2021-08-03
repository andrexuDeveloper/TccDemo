## 账户余额表，存储账户余额信息
create table if not exists account
(
    account_no     varchar(64) not null,
    amount         DOUBLE COMMENT '可用金额',
    freezed_amount DOUBLE COMMENT '冻结金额',
    primary key (account_no)
);

## 账号操作流水表，记录每一笔分布式事务操作的账号、操作金额、操作类型（扣钱/加钱），TCC模式下才会使用到此表
create table if not exists account_transaction
(
    tx_id      varchar(128) not null,
    account_no varchar(256) not null,
    amount     DOUBLE,
    type       varchar(10) not null COMMENT '类型 1扣钱 2加钱' ,
    `tx_status` int(11) DEFAULT NULL COMMENT '状态 1处理，2失败 3成功',
    primary key (tx_id)
);


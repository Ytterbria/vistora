create table if not exists space
(
    id          bigint auto_increment comment 'id' primary key,
    spaceName   varchar(128)                       not null comment '空间名称',
    spaceLevel  int    default 0                   not null comment '空间级别: 0-普通空间, 1-专业版,2-企业版',
    spaceSize   bigint default 0                   not null comment '空间已用容量',
    spaceCount  bigint default 0                   not null comment '空间已用数量',
    maxSize     bigint default 0                   not null comment '空间最大容量',
    maxCount    bigint default 0                   not null comment '空间最大数量',
    userId      bigint                             not null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    editTime    datetime default CURRENT_TIMESTAMP not null comment '最近编辑时间',
    isDeleted   bit    default 0                   not null comment '是否删除: 0-未删除, 1-已删除',

    index idx_userId (userId),          -- 提升基于用户 ID 的查询性能
    index idx_spaceName (spaceName),    -- 提升基于空间名称的查询性能
    index idx_spaceLevel (spaceLevel)   -- 提升基于空间级别的查询性能
) comment '空间' collate= utf8mb4_unicode_ci;   -- 设置表的字符集为 utf8mb4_unicode_ci

alter table space
    add column spaceType int default 0 not null comment '空间类型: 0-私有空间,1-团队空间';

create index idx_spaceType on space (spaceType); -- 创建基于 spaceType 列的索引
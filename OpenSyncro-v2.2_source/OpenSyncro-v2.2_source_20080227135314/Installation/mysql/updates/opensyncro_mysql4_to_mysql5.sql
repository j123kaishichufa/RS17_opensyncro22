--- OpenSyncro v2.0+ database upgrade dump for MySQL 4.0 to
--- MySQL 5.0 transition.

--- Execute this on MySQL 5.0 to make an existing OpenSyncro v2.0+
--- database migrated from MySQL 4.0, compatible with MySQL 5.0's
--- traditional SQL mode.


--- Set charset of all tables to UTF-8 ---

alter table SyncroLog convert to charset utf8;

alter table SyncroLogMessage convert to charset utf8;

alter table SyncroPipe convert to charset utf8;

alter table SyncroPipeComponentData convert to charset utf8;

alter table SyncroPipeComponentDataAttr convert to charset utf8;

alter table SyncroPipeConverterDatas convert to charset utf8;

alter table SyncroUser convert to charset utf8;

alter table SyncroPipeExecutionQueue convert to charset utf8;

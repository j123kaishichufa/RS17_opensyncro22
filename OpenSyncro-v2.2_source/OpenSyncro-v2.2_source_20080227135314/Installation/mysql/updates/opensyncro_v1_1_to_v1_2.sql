--- OpenSyncro v1.1 to v1.2 database upgrade dump ---

alter table SyncroPipeComponentDataAttr change value value LONGTEXT;
alter table SyncroLogMessage change message message TEXT;

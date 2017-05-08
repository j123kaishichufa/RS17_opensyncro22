--- OpenSyncro v2.0 to v2.1 database upgrade dump ---

--- Substitute datatype BIT with TINYINT(1) in SyncroPipe for better MySQL 5.0 compatibility ---

alter table SyncroPipe modify rpcStartEnabled TINYINT(1);

alter table SyncroPipe modify httpStartEnabled TINYINT(1);

alter table SyncroPipe modify abortMailEnabled TINYINT(1);

--- Delete obsolete "Fetch only updated orders" setting from RemoteOrderSource component ---

delete SyncroPipeComponentDataAttr from SyncroPipeComponentDataAttr, SyncroPipe where SyncroPipeComponentDataAttr.component = SyncroPipe.sourceData and SyncroPipe.sourceID = 'smilehouse.opensyncro.defaultcomponents.workspace.RemoteOrderSource' and SyncroPipeComponentDataAttr.name = 'onlyUpdated';

--- OpenSyncro v1.2 to v1.3 database upgrade dump ---

alter table SyncroPipe add mailHost VARCHAR(255),add recipientAddress VARCHAR(255), add transferLogNotificationLevel BIGINT default 1;

alter table SyncroLogMessage drop PRIMARY KEY;
alter table SyncroLogMessage add id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY; 
alter table SyncroLogMessage add messageType BIGINT default 2;

--- OpenSyncro v1.3 to v2.0 database upgrade dump ---

--- New column to SyncroPipe ---
alter table SyncroPipe add abortMailEnabled BIT default 0;

--- Miscellaneous cleanup: set default values to existing boolean type columns ---
alter table SyncroPipe alter rpcStartEnabled set default 0;
alter table SyncroPipe alter httpStartEnabled set default 0;

--- Create new table for Pipe execution request queue ---
create table SyncroPipeExecutionQueue (id BIGINT NOT NULL AUTO_INCREMENT, pipe BIGINT not null, requestDate DATETIME, startDate DATETIME, primary key (id));

--- New columns for keeping track of pipe execution information ---
alter table SyncroPipe add startTime DATETIME default null;
alter table SyncroPipe add endTime DATETIME default null;
alter table SyncroPipe add lastStatus TEXT;
alter table SyncroPipe add duration BIGINT default 0;
alter table SyncroPipe add lastUser TEXT;

--- New column to SyncroLog ---
alter table SyncroLog add userName TEXT;

--- Bugfix: Remove deleted Pipes' orphan log message lines left in the SyncroLogMessage table
delete from SyncroLogMessage where logEntry is null;

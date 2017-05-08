--- OpenSyncro (empty) database initialization dump for MySQL 4.0 and 5.0+ ---

create table SyncroPipeComponentData (id BIGINT NOT NULL AUTO_INCREMENT, primary key (id));

create table SyncroLogMessage (logEntry BIGINT, message TEXT, messageIndex INTEGER,messageType BIGINT,id BIGINT not null AUTO_INCREMENT,primary key(id));

create table SyncroLog (id BIGINT NOT NULL AUTO_INCREMENT, pipe BIGINT not null, eventTime DATETIME, statusCode INTEGER, userName TEXT,primary key (id));

create table SyncroPipeConverterDatas (id BIGINT NOT NULL AUTO_INCREMENT, parentPipe BIGINT not null, converterID VARCHAR(255), converterData BIGINT, position INTEGER, primary key (id));

create table SyncroPipe (id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(255), loggingVerbosityLevel INTEGER, startPassword VARCHAR(255), rpcStartEnabled TINYINT(1), httpStartEnabled TINYINT(1), abortMailEnabled TINYINT(1), sourceID VARCHAR(255), destinationID VARCHAR(255), sourceData BIGINT, destinationData BIGINT, mailHost VARCHAR(255),recipientAddress VARCHAR(255),transferLogNotificationLevel BIGINT, startTime DATETIME, endTime DATETIME, lastStatus TEXT, duration BIGINT, lastUser TEXT, primary key (id));

create table SyncroPipeComponentDataAttr (component BIGINT not null, value LONGTEXT, name VARCHAR(255) not null, primary key (component, name));

create table SyncroUser (login VARCHAR(255) not null, password VARCHAR(255), name VARCHAR(255), primary key (login));

create table SyncroPipeExecutionQueue (id BIGINT NOT NULL AUTO_INCREMENT, pipe BIGINT not null, requestDate DATETIME, startDate DATETIME, primary key (id));

alter table SyncroLogMessage add index (id), add constraint FK1873D07B7695DF8E foreign key (logEntry) references SyncroLog (id);

alter table SyncroLog add index (pipe), add constraint FK95F585EC3481AE foreign key (pipe) references SyncroPipe (id);

alter table SyncroPipeConverterDatas add index (parentPipe), add constraint FK286CDEFF1689978 foreign key (parentPipe) references SyncroPipe (id);

alter table SyncroPipeConverterDatas add index (converterData), add constraint FK286CDEF932A1D6A foreign key (converterData) references SyncroPipeComponentData (id);

alter table SyncroPipe add index (destinationData), add constraint FK28BCF40696E9F0D8 foreign key (destinationData) references SyncroPipeComponentData (id);

alter table SyncroPipe add index (sourceData), add constraint FK28BCF406BDB94665 foreign key (sourceData) references SyncroPipeComponentData (id);

alter table SyncroPipeComponentDataAttr add index (component), add constraint FK9842A572AC8F1CFD foreign key (component) references SyncroPipeComponentData (id);

insert into SyncroUser values ("user", "password", "Default User");

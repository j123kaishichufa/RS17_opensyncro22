--- OpenSyncro v2.1 to v2.2 database upgrade dump ---

UPDATE SyncroPipeComponentDataAttr SPCDA,SyncroPipeConverterDatas SPCD SET SPCDA.name='writetype', SPCDA.value='write_type_alwaysappend' WHERE SPCDA.component=SPCD.converterData AND 
SPCDA.name='append' AND SPCDA.value='true' AND SPCD.converterID='smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileWriteConverter';

UPDATE SyncroPipeComponentDataAttr SPCDA,SyncroPipeConverterDatas SPCD SET SPCDA.name='writetype', SPCDA.value='write_type_iterationstartoverwrite' WHERE SPCDA.component=SPCD.converterData AND 
SPCDA.name='append' AND SPCDA.value='false' AND SPCD.converterID='smilehouse.opensyncro.defaultcomponents.filesystem.LocalFileWriteConverter';
-------------------------------------------
OpenSyncro MySQL database updates directory
-------------------------------------------

This directory contains database dump files for upgrading an existing
OpenSyncro database for use with a newer version of OpenSyncro web
application.

OpenSyncro v2.1 -> v2.2: Execute opensyncro_v2_1_to_v2_2.sql.

OpenSyncro v2.0 -> v2.1: Execute opensyncro_v2_0_to_v2_1.sql.
                         Note: removes RemoteOrderSource component's obsoleted
                         parameter "Fetch only the updated orders".

OpenSyncro v1.3 -> v2.0: Execute opensyncro_v1_3_to_v2_0.sql.

OpenSyncro v1.2 -> v1.3: Execute opensyncro_v1_2_to_v1_3.sql.

OpenSyncro v1.1 -> v1.2: Execute opensyncro_v1_1_to_v1_2.sql to apply the
                         recommended changes. However, v1.2 can still use an
                         existing v1.0 and v1.1 database without modification.

OpenSyncro v1.0 -> v1.1: No changes in database; v1.1 can use an existing
                         v1.0 database without modification.

---

MySQL 4.0 -> 5.0: Execute opensyncro_mysql4_to_mysql5.sql on MySQL 5.0 to
                  make an OpenSyncro database migrated from MySQL 4.0 fully
                  compatible with MySQL 5.0's traditional SQL mode. Supports
                  OpenSyncro v2.0 and newer databases.
                  
---

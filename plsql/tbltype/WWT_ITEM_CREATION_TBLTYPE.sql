CREATE OR REPLACE TYPE APPS.WWT_ITEM_CREATION_TBLTYPE IS TABLE OF APPS.WWT_ITEM_CREATION_RECTYPE;
/

GRANT EXECUTE ON APPS.WWT_ITEM_CREATION_TBLTYPE TO CF_TEMP_ROLE;

GRANT EXECUTE ON APPS.WWT_ITEM_CREATION_TBLTYPE TO WL_USER;

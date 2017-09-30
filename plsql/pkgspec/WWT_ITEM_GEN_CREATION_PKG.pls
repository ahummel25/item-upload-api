CREATE OR REPLACE PACKAGE APPS.wwt_item_gen_creation_pkg
AS


PROCEDURE create_items(p_item_tbltype IN apps.wwt_item_creation_tbltype,
                       p_username     IN VARCHAR2);
   

END wwt_item_gen_creation_pkg;
/

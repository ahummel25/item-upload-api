CREATE OR REPLACE PACKAGE BODY APPS.wwt_item_gen_creation_pkg
AS

   TYPE g_message_tab_type IS TABLE OF VARCHAR2(32767)
   INDEX BY PLS_INTEGER;

   g_log_message_tab g_message_tab_type;
   g_pkg VARCHAR2(30) := 'WWT_ITEM_GEN_CREATION_PKG.';
   g_items_exist VARCHAR2(20000) := NULL;

PROCEDURE write_log_messages(p_username IN VARCHAR2,
                             p_ext      IN VARCHAR2)
  IS

    l_debug                       BOOLEAN;
    l_log_file_handle             UTL_FILE.FILE_TYPE;
    l_log_file_name               VARCHAR2(1000);

  BEGIN

    apps.wwt_write_debug_log_pkg.get_debug_handles (p_user_name          => p_username,
                                                    p_pkg_name           => 'WWT_ITEM_GEN_CREATION_PKG',
                                                    p_directory_name     => 'DEBUG_TMP',
                                                    p_ext                => p_ext,
                                                    x_debug_file_name    => l_log_file_name,
                                                    x_debug_flag         => l_debug,
                                                    x_debug_file_handle  => l_log_file_handle);

    /*L_DEBUG will be true if p_ext is 'log' or this process is set up in the lookup
      WWT_RUNTIME_DEBUG, otherwise it will be false, we will have no file handle,
      and so nothing to write to, so skip this*/
    IF l_debug THEN

      <<g_dbg_message_tab_loop>>
      FOR i in 1..g_log_message_tab.COUNT LOOP

        apps.wwt_write_debug_log_pkg.write_log(p_debug_file_handle => l_log_file_handle,
                                               p_message           => g_log_message_tab(i));

      END LOOP g_dbg_message_tab_loop;

      UTL_FILE.FFLUSH(FILE => l_log_file_handle);

      apps.wwt_write_debug_log_pkg.close_log( x_debug_file_handle => l_log_file_handle);

    END IF;

  END write_log_messages;

  PROCEDURE append_message(p_msg IN VARCHAR2)
  IS

  BEGIN

    g_log_message_tab(g_log_message_tab.COUNT + 1) := g_pkg || p_msg;

  END append_message;

   PROCEDURE do_any_items_exist(p_concatenated_segments IN VARCHAR2,
                                p_organization_id       IN PLS_INTEGER)
   IS

      l_item_already_exists PLS_INTEGER := 0;

   BEGIN

      SELECT COUNT(1)
        INTO l_item_already_exists
        FROM apps.mtl_system_items_kfv
       WHERE concatenated_segments = p_concatenated_segments
         AND organization_id = p_organization_id;

      IF l_item_already_exists > 0 THEN

         IF NVL(g_items_exist, CHR(4)) NOT LIKE '%' || p_concatenated_segments || '%' THEN

            g_items_exist := g_items_exist || p_concatenated_segments || ', ';

         END IF;

      END IF;

   END do_any_items_exist;

PROCEDURE create_items(p_item_tbltype   IN apps.wwt_item_creation_tbltype,
                       p_username       IN VARCHAR2,
                       x_return_status  OUT VARCHAR2,
                       x_return_message OUT VARCHAR2)

IS

   CURSOR get_item_org_cur
       IS SELECT DISTINCT organization_code
            FROM TABLE(p_item_tbltype);

   CURSOR get_data_cur
       IS SELECT *
            FROM TABLE(p_item_tbltype);

   c_procedure           CONSTANT VARCHAR2(30) := 'CREATE_ITEMS: ';

   l_item_tbltype        apps.wwt_item_creation_tbltype;
   l_dml_errors_exp      EXCEPTION;
   PRAGMA EXCEPTION_INIT (l_dml_errors_exp, -24381);
   l_error_idx           PLS_INTEGER;

   l_request_id         PLS_INTEGER;
   l_resp_name          VARCHAR2(30) := 'INVENTORY';
   l_resp_id            PLS_INTEGER;
   l_user_id            PLS_INTEGER;
   l_application_id     PLS_INTEGER;
   l_organization_code     apps.mtl_parameters.organization_code%TYPE;
   l_concatenated_segments apps.mtl_system_items_kfv.concatenated_segments%TYPE;

BEGIN

   x_return_status := APPS.FND_API.G_RET_STS_SUCCESS;

   l_user_id := apps.wwt_util_user.get_runtime_user_id(p_user_name => p_username);

   FOR get_data_rec IN get_data_cur LOOP

      l_concatenated_segments := get_data_rec.segment2 || '.' || get_data_rec.segment3 || '.' || get_data_rec.segment1 || '.' || get_data_rec.segment4;

      do_any_items_exist(p_concatenated_segments => l_concatenated_segments,
                         p_organization_id       => apps.wwt_util_organization_pkg.get_organization_id_from_code(get_data_rec.organization_code));

   END LOOP;

   SELECT responsibility_id,
          application_id
     INTO l_resp_id,
          l_application_id
     FROM apps.fnd_responsibility
    WHERE responsibility_key = l_resp_name;

   apps.fnd_global.apps_initialize(l_user_id, l_resp_id, l_application_id);
   apps.mo_global.init('INV');

   l_item_tbltype := p_item_tbltype;

   FORALL l_ctr IN 1 .. l_item_tbltype.COUNT SAVE EXCEPTIONS
      INSERT INTO apps.mtl_system_items_interface
                  (segment2,
                   segment3,
                   segment1,
                   segment4,
                   organization_id,
                   process_flag,
                   set_process_id,
                   transaction_type,
                   description,
                   primary_uom_code,
                   serial_number_control_code,
                   template_id,
                   attribute1,
                   attribute15,
                   creation_date,
                   created_by,
                   last_update_date,
                   last_updated_by)
           VALUES (l_item_tbltype(l_ctr).segment2,
                   l_item_tbltype(l_ctr).segment3,
                   l_item_tbltype(l_ctr).segment1,
                   l_item_tbltype(l_ctr).segment4,
                   apps.wwt_util_organization_pkg.get_organization_id_from_code(l_item_tbltype(l_ctr).organization_code),
                   1, --Pending, Items ready to be processed
                   55555,
                   l_item_tbltype(l_ctr).transaction_type,
                   l_item_tbltype(l_ctr).description,
                   l_item_tbltype(l_ctr).primary_uom_code,
                   l_item_tbltype(l_ctr).serial_number_control_code,
                   l_item_tbltype(l_ctr).template_id,
                   l_item_tbltype(l_ctr).attribute1,
                   l_item_tbltype(l_ctr).attribute15,
                   SYSDATE,
                   APPS.FND_GLOBAL.USER_ID,
                   SYSDATE,
                   APPS.FND_GLOBAL.USER_ID);

   OPEN get_item_org_cur;
   LOOP
      FETCH get_item_org_cur INTO l_organization_code;

      EXIT WHEN get_item_org_cur%NOTFOUND;

      l_request_id := apps.fnd_request.submit_request(application =>  'INV',
                                                      program     =>  'INCOIN',
                                                      argument1   =>  apps.wwt_util_organization_pkg.get_organization_id_from_code(l_organization_code),
                                                      argument2   =>  2, -- 1 = All orgs, 2 - No
                                                      argument3   =>  1, -- Validate Items
                                                      argument4   =>  1, -- Process Items
                                                      argument5   =>  1, -- Delete processed rows
                                                      argument6   =>  55555,
                                                      argument7   =>  1); --1 = Create Item, 2 = Update Item

      COMMIT;

      IF l_request_id = 0 THEN
         x_return_status  := APPS.FND_API.G_RET_STS_ERROR;
         x_return_message := FND_MESSAGE.GET;
      END IF;

   END LOOP;

   CLOSE get_item_org_cur;

   write_log_messages(p_username => p_username,
                      p_ext      => 'log');

   IF g_items_exist IS NOT NULL THEN

      x_return_message := RTRIM(g_items_exist, ', ');
      g_items_exist := NULL;

   END IF;

EXCEPTION
   WHEN l_dml_errors_exp THEN

      x_return_status := APPS.FND_API.G_RET_STS_ERROR;
      x_return_message := 'Item Bulk Insert Error - Check Logs';

      append_message(c_procedure || 'BULK EXCEPTION count => ' || SQL%BULK_EXCEPTIONS.COUNT);

      <<error_msgs>>
      FOR l_ctr IN 1..SQL%BULK_EXCEPTIONS.COUNT LOOP
         l_error_idx   := SQL%BULK_EXCEPTIONS (l_ctr).ERROR_INDEX;

         append_message (c_procedure || 'BULK EXCEPTION Message => Index #: '
                                     || l_error_idx
                                     || ' Segment2: '
                                     || l_item_tbltype(l_error_idx).segment2
                                     || ' Organization ID: '
                                     || l_item_tbltype(l_error_idx).organization_code
                                     || ' Error_Code: '
                                     || SQL%BULK_EXCEPTIONS (l_ctr).error_code);
      END LOOP error_msgs;

      write_log_messages(p_username => p_username,
                         p_ext      => 'log');

      IF get_item_org_cur%ISOPEN THEN
         CLOSE get_item_org_cur;
      END IF;

   WHEN OTHERS THEN

      x_return_status := APPS.FND_API.G_RET_STS_UNEXP_ERROR;
      x_return_message := 'Unexpected error - ' || SQLERRM;

      IF get_item_org_cur%ISOPEN THEN
         CLOSE get_item_org_cur;
      END IF;

      append_message(c_procedure || ' Error: ' || SQLERRM);

      write_log_messages(p_username => p_username,
                         p_ext   => 'log');

END;


END wwt_item_gen_creation_pkg;
/
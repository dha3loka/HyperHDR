/* Auto-generated libsystemd.so.0 shim for running HyperHDR v22.0.0.0 in Termux (glibc-repo) */
#include <errno.h>
#include <stddef.h>
#include <stdint.h>

int sd_bus_add_match(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_add_match_async(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_add_object_manager(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_add_object_vtable(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_call(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_call_async(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_bus_close_unref(void* p) { (void)p; return NULL; }
int sd_bus_creds_get_egid(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_creds_get_euid(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_creds_get_gid(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_creds_get_pid(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_creds_get_selinux_context(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_creds_get_supplementary_gids(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_creds_get_uid(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_bus_creds_unref(void* p) { (void)p; return NULL; }
int sd_bus_emit_interfaces_added_strv(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_emit_interfaces_removed_strv(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_emit_object_added(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_emit_object_removed(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_emit_properties_changed_strv(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_bus_error_free(void* p) { (void)p; return NULL; }
int sd_bus_error_is_set(void* p) { (void)p; return 0; }
int sd_bus_error_set(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_error_set_errno(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_flush(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_bus_flush_close_unref(void* p) { (void)p; return NULL; }
int sd_bus_get_current_message(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_get_events(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_get_fd(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_get_method_call_timeout(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_get_n_queued_read(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_get_timeout(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_get_unique_name(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_interface_name_is_valid(void* p) { (void)p; return 0; }
int sd_bus_match_signal(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_member_name_is_valid(void* p) { (void)p; return 0; }
int sd_bus_message_append_array(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_append_basic(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_append_string_space(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_at_end(void* p) { (void)p; return 0; }
int sd_bus_message_close_container(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_copy(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_enter_container(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_exit_container(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_bus(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_destination(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_error(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_expect_reply(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_interface(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_member(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_path(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_get_sender(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_is_empty(void* p) { (void)p; return 0; }
int sd_bus_message_new(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_new_method_call(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_new_method_error(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_new_method_return(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_new_signal(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_open_container(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_peek_type(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_read_array(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_read_basic(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_ref(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_rewind(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_seal(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_set_destination(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_message_set_expect_reply(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_bus_message_unref(void* p) { (void)p; return NULL; }
int sd_bus_new(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_object_path_is_valid(void* p) { (void)p; return 0; }
int sd_bus_open(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_open_system(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_open_system_remote(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_open_user(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_process(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_query_sender_creds(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_release_name(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_request_name(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_send(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_service_name_is_valid(void* p) { (void)p; return 0; }
int sd_bus_set_address(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_set_bus_client(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_set_fd(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_set_method_call_timeout(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_set_server(void) { return -95; /* -EOPNOTSUPP */ }
int sd_bus_set_trusted(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_bus_slot_unref(void* p) { (void)p; return NULL; }
int sd_bus_start(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_add_io(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_add_time(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_default(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_ref(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_event_source_disable_unref(void* p) { (void)p; return NULL; }
int sd_event_source_set_description(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_source_set_enabled(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_source_set_io_events(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_source_set_prepare(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_source_set_priority(void) { return -95; /* -EOPNOTSUPP */ }
int sd_event_source_set_time(void) { return -95; /* -EOPNOTSUPP */ }
void* sd_event_unref(void* p) { (void)p; return NULL; }
int sd_id128_randomize(void) { return -95; /* -EOPNOTSUPP */ }

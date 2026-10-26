/* Auto-generated libdbus-1.so.3 shim for running HyperHDR v24.0.0-beta.0 in Termux (glibc-repo) */
#include <stddef.h>
#include <stdint.h>

typedef struct DBusError {
    const char *name;
    const char *message;
    unsigned int dummy1 : 1;
    unsigned int dummy2 : 1;
    unsigned int dummy3 : 1;
    unsigned int dummy4 : 1;
    unsigned int dummy5 : 1;
    void *padding1;
} DBusError;

void dbus_error_init(DBusError *error) {
    if (error) {
        error->name = NULL;
        error->message = NULL;
    }
}

void dbus_error_free(DBusError *error) {
    if (error) {
        error->name = NULL;
        error->message = NULL;
    }
}

int dbus_error_is_set(const DBusError *error) {
    return (error && error->name != NULL) ? 1 : 0;
}

void* dbus_bus_get_private(int type, DBusError *error) {
    (void)type;
    if (error) {
        error->name = "org.freedesktop.DBus.Error.FileNotFound";
        error->message = "D-Bus not available in headless Termux";
    }
    return NULL;
}

void dbus_bus_add_match(void *conn, const char *rule, DBusError *error) {
    (void)conn; (void)rule; (void)error;
}

const char* dbus_bus_get_unique_name(void *conn) {
    (void)conn; return NULL;
}

int dbus_connection_add_filter(void *conn, void *fn, void *user_data, void *free_fn) {
    (void)conn; (void)fn; (void)user_data; (void)free_fn; return 0;
}

void dbus_connection_close(void *conn) { (void)conn; }
int dbus_connection_dispatch(void *conn) { (void)conn; return 2; /* DBUS_DISPATCH_COMPLETE */ }
void dbus_connection_flush(void *conn) { (void)conn; }
int dbus_connection_get_dispatch_status(void *conn) { (void)conn; return 2; }
void dbus_connection_remove_filter(void *conn, void *fn, void *user_data) { (void)conn; (void)fn; (void)user_data; }
int dbus_connection_send(void *conn, void *msg, uint32_t *serial) { (void)conn; (void)msg; (void)serial; return 0; }
void* dbus_connection_send_with_reply_and_block(void *conn, void *msg, int timeout_ms, DBusError *error) {
    (void)conn; (void)msg; (void)timeout_ms; (void)error; return NULL;
}
void dbus_connection_set_exit_on_disconnect(void *conn, int exit_on_disconnect) { (void)conn; (void)exit_on_disconnect; }
int dbus_connection_set_watch_functions(void *conn, void *add_fn, void *rem_fn, void *toggled_fn, void *data, void *free_fn) {
    (void)conn; (void)add_fn; (void)rem_fn; (void)toggled_fn; (void)data; (void)free_fn; return 0;
}
int dbus_connection_try_register_object_path(void *conn, const char *path, const void *vtable, void *user_data, DBusError *error) {
    (void)conn; (void)path; (void)vtable; (void)user_data; (void)error; return 0;
}
void dbus_connection_unref(void *conn) { (void)conn; }

const char* dbus_message_get_interface(void *msg) { (void)msg; return NULL; }
const char* dbus_message_get_member(void *msg) { (void)msg; return NULL; }
const char* dbus_message_get_path(void *msg) { (void)msg; return NULL; }
int dbus_message_get_type(void *msg) { (void)msg; return 0; }

void dbus_message_iter_abandon_container_if_open(void *iter, void *sub) { (void)iter; (void)sub; }
int dbus_message_iter_append_basic(void *iter, int type, const void *value) { (void)iter; (void)type; (void)value; return 0; }
int dbus_message_iter_close_container(void *iter, void *sub) { (void)iter; (void)sub; return 0; }
int dbus_message_iter_get_arg_type(void *iter) { (void)iter; return 0; }
void dbus_message_iter_get_basic(void *iter, void *value) { (void)iter; (void)value; }
int dbus_message_iter_get_element_type(void *iter) { (void)iter; return 0; }
int dbus_message_iter_init(void *msg, void *iter) { (void)msg; (void)iter; return 0; }
void dbus_message_iter_init_append(void *msg, void *iter) { (void)msg; (void)iter; }
int dbus_message_iter_next(void *iter) { (void)iter; return 0; }
int dbus_message_iter_open_container(void *iter, int type, const char *sig, void *sub) { (void)iter; (void)type; (void)sig; (void)sub; return 0; }
void dbus_message_iter_recurse(void *iter, void *sub) { (void)iter; (void)sub; }

void* dbus_message_new_error(void *reply_to, const char *name, const char *message) { (void)reply_to; (void)name; (void)message; return NULL; }
void* dbus_message_new_method_call(const char *dest, const char *path, const char *iface, const char *method) {
    (void)dest; (void)path; (void)iface; (void)method; return NULL;
}
void* dbus_message_new_method_return(void *method_call) { (void)method_call; return NULL; }
void* dbus_message_new_signal(const char *path, const char *iface, const char *name) { (void)path; (void)iface; (void)name; return NULL; }
void dbus_message_unref(void *msg) { (void)msg; }

void* dbus_watch_get_data(void *watch) { (void)watch; return NULL; }
int dbus_watch_get_enabled(void *watch) { (void)watch; return 0; }
unsigned int dbus_watch_get_flags(void *watch) { (void)watch; return 0; }
int dbus_watch_get_unix_fd(void *watch) { (void)watch; return -1; }
int dbus_watch_handle(void *watch, unsigned int flags) { (void)watch; (void)flags; return 0; }
void dbus_watch_set_data(void *watch, void *data, void *free_fn) { (void)watch; (void)data; (void)free_fn; }

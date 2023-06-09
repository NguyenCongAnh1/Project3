module Security {
    requires Image;
    requires com.google.gson;
    requires java.prefs;
    requires java.datatransfer;
    requires java.desktop;
    requires miglayout;
    requires com.google.common;
    exports com.udacity.catpoint.security.data;
    exports com.udacity.catpoint.security.application;
    exports com.udacity.catpoint.security.service;
    opens com.udacity.catpoint.security.data to com.google.gson;
    exports com.udacity.catpoint.security;
}
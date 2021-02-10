package com.codeoctoprint.Useful;

public class URLCleanser {
    public String clean(String path) {
        if (!path.isEmpty()) {
            // Make sure it ends with /
            if (path.charAt(path.length()-1) != '/') {
                path += "/";
            }

            // Check for http or https tag
            if (!path.contains("http://") && !path.contains("https://")) {
                path = "http://" + path;
            }
        } else {
            path = "http://";
        }
        return path;
    }

    public String combineURL(String url, String path) {
        return url + path;
    }
}

package xyz.alviksar.orchidarium;

/**
 * Common constants.
 */
public class OrchidariumContract {
    // Firebase database and storage references
    public static final String REFERENCE_ORCHIDS_DATA = "orchids";
    public static final String REFERENCE_ORCHIDS_PHOTOS = "orchid_photos";

    // Firebase data fields
    public static final String FIELD_ISVISIBLEFORSALE = "isVisibleForSale";
    public static final String FIELD_FORSALETIME= "forSaleTime";
    public static final String FIELD_NAME = "name";

    // Firebase notification topic
    public static final String NOTIFICATION_TOPIC = "news";
}

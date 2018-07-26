package xyz.alviksar.orchidarium;

import xyz.alviksar.orchidarium.model.OrchidEntity;

public class DummyData {
    public static OrchidEntity getOrchid(int code) {
        OrchidEntity orchid = new OrchidEntity();
        orchid.setIsVisibleForSale(true);
        orchid.setCode("test1-"+String.valueOf(code));
        orchid.setName("Beautiful orchid");
        orchid.setAge(2);
        orchid.setPotSize("2,5");
        orchid.setRetailPrice(12345.0);
        orchid.setDescription("This orchid is added for test purpose only");
        return  orchid;
    }
}

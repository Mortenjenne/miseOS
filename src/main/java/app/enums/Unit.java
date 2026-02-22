package app.enums;

public enum Unit {

    KG("kg"),
    G("g"),
    L("l"),
    ML("ml"),
    PCS("stk"),
    BUNCH("bundt"),
    CAN("dåse"),
    BOX("kasse"),
    BAG("pose"),
    PACK("pakke"),
    BOTTLE("flaske"),
    SLICE("skive"),
    SIDES("sider");

    private final String danish;

    Unit(String danish)
    {
        this.danish = danish;
    }

    public String getDisplayName()
    {
        return danish;
    }

    public static Unit fromString(String text)
                                                          {
        return valueOf(text.trim().toUpperCase());
    }
}

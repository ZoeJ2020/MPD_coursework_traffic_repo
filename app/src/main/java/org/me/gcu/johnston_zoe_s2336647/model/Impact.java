package org.me.gcu.johnston_zoe_s2336647.model;

// impact for each Situation. certain tags may not appear so they are nullable (capital letter)
public class Impact {
    //    number of lanes still open (incidents only, null if not given)
    private Integer operationalLaneCount;

    //    number of lanes restricted (incidents only, null if not given)
    private Integer restrictedLaneCount;

    //    delay level (roadworks only) delays, longDelays or veryLongDelays. null if not given
    private String delayType;

    //    constructor
    public Impact() { }

    //    get methods
    public Integer getOperationalLaneCount() {
        return operationalLaneCount;
    }

    public Integer getRestrictedLaneCount() {
        return restrictedLaneCount;
    }

    public String getDelayType() {
        return delayType;
    }

//    set methods

    public void setOperationalLaneCount(Integer operationalLaneCount) {
        this.operationalLaneCount = operationalLaneCount;
    }

    public void setRestrictedLaneCount(Integer restrictedLaneCount) {
        this.restrictedLaneCount = restrictedLaneCount;
    }

    public void setDelayType(String delayType) {
        this.delayType = delayType;
    }

//    toString for debug logging
    @Override
    public String toString() {
        return "Impact{operationalLanes=" + operationalLaneCount + ", restrictedLanes=" + restrictedLaneCount
                + ", delayType=" + delayType + "}";
    }

}
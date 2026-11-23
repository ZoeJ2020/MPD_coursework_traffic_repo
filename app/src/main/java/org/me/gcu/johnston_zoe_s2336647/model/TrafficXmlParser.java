package org.me.gcu.johnston_zoe_s2336647.model;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

// TrafficXmlParser handles XML parsing from Traffic Scotland feeds.
public class TrafficXmlParser {

    // namespace URI found in Traffic Scotland feed for reading xsi: attributes - parsing is namespace-aware.
    private static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

    //    list to hold Situation objects
    public ArrayList<Situation> parseSituations(String dataToParse, String category)
            throws XmlPullParserException, IOException {

        ArrayList<Situation> situations = new ArrayList<>();

        XmlPullParserFactory factory =
                XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput( new StringReader( dataToParse ) );

        int eventType = xpp.getEventType();

        Situation current = null;
//      Multiple <value> tags exist in the XML,
//      so we must check that we are inside a comment to get the right text.
        boolean isInComment = false;

//      the from/toto point being read, and the last location text waiting for its type tag.
        Location currentLocation = null;
        String lastValue = null;

        while (eventType != XmlPullParser.END_DOCUMENT){

            String eventName = xpp.getName();

            if (eventType == XmlPullParser.START_TAG){

                switch (eventName){
                    case "situation": {
                        String id = xpp.getAttributeValue(null, "id");
                        current = new Situation(id, category);
                        break;
                    }

                    case "situationRecord":
                        String raw_type = xpp.getAttributeValue(XSI_NAMESPACE_URI, "type");
                        current.setRawType(raw_type);
                        current.setFormattedType(formatType(raw_type));
                        break;

                    case "generalPublicComment":
                        isInComment = true;
                        break;

                    case "value":
                        if (isInComment){
                            String description = xpp.nextText().trim();
                            current.setDescription(description);
                        } else if (currentLocation != null){
                            lastValue = xpp.nextText().trim();
                        }
                        break;

                    case "situationRecordCreationTime":
                        if (current != null){
                            current.setCreatedTime(xpp.nextText().trim());
                        }
                        break;

                    case "situationRecordVersionTime":
                        if (current != null){
                            current.setUpdatedTime(xpp.nextText().trim());
                        }
                        break;

                    case "overallStartTime":
                        if (current != null){
                            current.setStartTime(xpp.nextText().trim());
                        }
                        break;

                    case "overallEndTime":
                        if (current != null){
                            current.setEndTime(xpp.nextText().trim());
                        }
                        break;

                    case "tpegDirection":
                        if (current != null){
                            current.setDirection(xpp.nextText().trim());
                        }
                        break;

                    case "delaysType":
                        if (current != null){
                            current.getImpact().setDelayType(xpp.nextText().trim());
                        }
                        break;

                    case "numberOfOperationalLanes":
                        if (current != null){
                            try {
                                current.getImpact().setOperationalLaneCount(Integer.valueOf(xpp.nextText().trim()));
                            } catch (NumberFormatException e) {
                                Log.w("PARSE", "Operational lane count is not a number");
                            }
                        }
                        break;

                    case "numberOfLanesRestricted":
                        if (current != null){
                            try {
                                current.getImpact().setRestrictedLaneCount(Integer.valueOf(xpp.nextText().trim()));
                            } catch (NumberFormatException e) {
                                Log.w("PARSE", "Restricted lane count is not a number");
                            }
                        }
                        break;

                    case "from":
                    case "to":
                        currentLocation = new Location(xpp.getName());
                        lastValue = null;
                        break;

                    case "latitude":
                        if (currentLocation != null){
                            try {
                                currentLocation.setLatitude(Float.parseFloat(xpp.nextText().trim()));
                            } catch (NumberFormatException e) {
                                Log.w("PARSE", "Latitude is not a number");
                            }
                        }
                        break;

                    case "longitude":
                        if (currentLocation != null){
                            try {
                                currentLocation.setLongitude(Float.parseFloat(xpp.nextText().trim()));
                            } catch (NumberFormatException e) {
                                Log.w("PARSE", "Longitude is not a number");
                            }
                        }
                        break;

                    case "tpegOtherPointDescriptorType":
                    case "tpegIlcPointDescriptorType": {
                        String descriptorType = xpp.nextText().trim();
                        if (currentLocation != null && lastValue != null){
                            switch (descriptorType){
                                case "linkName":
                                case "tpegIlcName1":
                                case "tpegIlcName2":
                                    currentLocation.addRoadName(lastValue);
                                    break;

                                case "nonLinkedPointName":
                                case "intersectionName":
                                    currentLocation.setPlaceName(lastValue);
                                    break;
                            }
                        }
                        break;
                    }
                }

            } else if (eventType == XmlPullParser.END_TAG) {

                switch (eventName){
                    case "situation": {
                        situations.add(current);
                        break;
                    }

                    case "generalPublicComment":
                        isInComment = false;
                        break;

                    case "from":
                    case "to":
                        if (current != null && currentLocation != null){
                            current.addLocation(currentLocation);
                        }
                        currentLocation = null;
                        break;

                }
            }

            eventType = xpp.next();
        }
        return situations;
    }

//    convert rawType into readable human string and store it seperately
    private String formatType(String raw_type){

        if (raw_type == null || raw_type.isEmpty()){
            return "Unknown";
        }

        String formatted_type = raw_type.replaceAll("(?<=[a-z])(?=[A-Z])", " ");

        String lowercase_type = formatted_type.toLowerCase();

        Character first_letter = Character.toUpperCase(formatted_type.charAt(0));
        String remaining_string = lowercase_type.substring(1);

        String final_string = first_letter + remaining_string;

        return final_string;
    }
}
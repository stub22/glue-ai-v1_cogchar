package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BonyJointConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BonyJointConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"jointId\",\"type\":\"int\"},{\"name\":\"bone\",\"type\":\"string\"},{\"name\":\"minPitch\",\"type\":\"double\"},{\"name\":\"maxPitch\",\"type\":\"double\"},{\"name\":\"minRoll\",\"type\":\"double\"},{\"name\":\"maxRoll\",\"type\":\"double\"},{\"name\":\"minYaw\",\"type\":\"double\"},{\"name\":\"maxYaw\",\"type\":\"double\"},{\"name\":\"defaultPosition\",\"type\":\"double\"}]}");
  public org.apache.avro.util.Utf8 name;
  public int jointId;
  public org.apache.avro.util.Utf8 bone;
  public double minPitch;
  public double maxPitch;
  public double minRoll;
  public double maxRoll;
  public double minYaw;
  public double maxYaw;
  public double defaultPosition;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return jointId;
    case 2: return bone;
    case 3: return minPitch;
    case 4: return maxPitch;
    case 5: return minRoll;
    case 6: return maxRoll;
    case 7: return minYaw;
    case 8: return maxYaw;
    case 9: return defaultPosition;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (org.apache.avro.util.Utf8)value$; break;
    case 1: jointId = (java.lang.Integer)value$; break;
    case 2: bone = (org.apache.avro.util.Utf8)value$; break;
    case 3: minPitch = (java.lang.Double)value$; break;
    case 4: maxPitch = (java.lang.Double)value$; break;
    case 5: minRoll = (java.lang.Double)value$; break;
    case 6: maxRoll = (java.lang.Double)value$; break;
    case 7: minYaw = (java.lang.Double)value$; break;
    case 8: maxYaw = (java.lang.Double)value$; break;
    case 9: defaultPosition = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}

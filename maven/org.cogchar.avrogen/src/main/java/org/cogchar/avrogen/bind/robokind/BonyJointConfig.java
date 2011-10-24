package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BonyJointConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BonyJointConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"jointId\",\"type\":\"int\"},{\"name\":\"bone\",\"type\":\"string\"},{\"name\":\"rotationAbout\",\"type\":{\"type\":\"enum\",\"name\":\"RotationAxis\",\"symbols\":[\"X_AXIS\",\"Y_AXIS\",\"Z_AXIS\"]}},{\"name\":\"minPosition\",\"type\":\"double\"},{\"name\":\"maxPosition\",\"type\":\"double\"},{\"name\":\"defaultPosition\",\"type\":\"double\"}]}");
  public org.apache.avro.util.Utf8 name;
  public int jointId;
  public org.apache.avro.util.Utf8 bone;
  public org.cogchar.avrogen.bind.robokind.RotationAxis rotationAbout;
  public double minPosition;
  public double maxPosition;
  public double defaultPosition;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return jointId;
    case 2: return bone;
    case 3: return rotationAbout;
    case 4: return minPosition;
    case 5: return maxPosition;
    case 6: return defaultPosition;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (org.apache.avro.util.Utf8)value$; break;
    case 1: jointId = (java.lang.Integer)value$; break;
    case 2: bone = (org.apache.avro.util.Utf8)value$; break;
    case 3: rotationAbout = (org.cogchar.avrogen.bind.robokind.RotationAxis)value$; break;
    case 4: minPosition = (java.lang.Double)value$; break;
    case 5: maxPosition = (java.lang.Double)value$; break;
    case 6: defaultPosition = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}

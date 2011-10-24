package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BonyRobotConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BonyRobotConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"robotId\",\"type\":\"string\"},{\"name\":\"ogreModelPath\",\"type\":\"string\"},{\"name\":\"jointConfigs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"BonyJointConfig\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"jointId\",\"type\":\"int\"},{\"name\":\"bone\",\"type\":\"string\"},{\"name\":\"rotationAbout\",\"type\":{\"type\":\"enum\",\"name\":\"RotationAxis\",\"symbols\":[\"X_AXIS\",\"Y_AXIS\",\"Z_AXIS\"]}},{\"name\":\"minPosition\",\"type\":\"double\"},{\"name\":\"maxPosition\",\"type\":\"double\"},{\"name\":\"defaultPosition\",\"type\":\"double\"}]}}}]}");
  public org.apache.avro.util.Utf8 robotId;
  public org.apache.avro.util.Utf8 ogreModelPath;
  public org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BonyJointConfig> jointConfigs;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return robotId;
    case 1: return ogreModelPath;
    case 2: return jointConfigs;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: robotId = (org.apache.avro.util.Utf8)value$; break;
    case 1: ogreModelPath = (org.apache.avro.util.Utf8)value$; break;
    case 2: jointConfigs = (org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BonyJointConfig>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}

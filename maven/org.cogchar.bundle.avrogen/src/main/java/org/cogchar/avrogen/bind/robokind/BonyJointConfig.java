package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BonyJointConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BonyJointConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"jointId\",\"type\":\"int\"},{\"name\":\"boneRotations\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"BoneRotationRangeConfig\",\"fields\":[{\"name\":\"boneName\",\"type\":\"string\"},{\"name\":\"rotationAxis\",\"type\":{\"type\":\"enum\",\"name\":\"RotationAxis\",\"symbols\":[\"PITCH\",\"ROLL\",\"YAW\"]}},{\"name\":\"minPosition\",\"type\":\"double\"},{\"name\":\"maxPosition\",\"type\":\"double\"}]}}},{\"name\":\"normalizedDefaultPosition\",\"type\":\"double\"}]}");
  public org.apache.avro.util.Utf8 name;
  public int jointId;
  public org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BoneRotationRangeConfig> boneRotations;
  public double normalizedDefaultPosition;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return jointId;
    case 2: return boneRotations;
    case 3: return normalizedDefaultPosition;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (org.apache.avro.util.Utf8)value$; break;
    case 1: jointId = (java.lang.Integer)value$; break;
    case 2: boneRotations = (org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BoneRotationRangeConfig>)value$; break;
    case 3: normalizedDefaultPosition = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}

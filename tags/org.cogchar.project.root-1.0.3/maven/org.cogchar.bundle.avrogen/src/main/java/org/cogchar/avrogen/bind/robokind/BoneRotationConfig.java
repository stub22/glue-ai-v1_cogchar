package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BoneRotationConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BoneRotationConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"boneName\",\"type\":\"string\"},{\"name\":\"rotationAxis\",\"type\":{\"type\":\"enum\",\"name\":\"RotationAxis\",\"symbols\":[\"PITCH\",\"ROLL\",\"YAW\"]}},{\"name\":\"rotationRadians\",\"type\":\"double\"}]}");
  public org.apache.avro.util.Utf8 boneName;
  public org.cogchar.avrogen.bind.robokind.RotationAxis rotationAxis;
  public double rotationRadians;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return boneName;
    case 1: return rotationAxis;
    case 2: return rotationRadians;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: boneName = (org.apache.avro.util.Utf8)value$; break;
    case 1: rotationAxis = (org.cogchar.avrogen.bind.robokind.RotationAxis)value$; break;
    case 2: rotationRadians = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}

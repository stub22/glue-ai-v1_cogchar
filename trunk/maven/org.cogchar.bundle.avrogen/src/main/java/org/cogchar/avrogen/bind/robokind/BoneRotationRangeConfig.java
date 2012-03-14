package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BoneRotationRangeConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BoneRotationRangeConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"boneName\",\"type\":\"string\"},{\"name\":\"rotationAxis\",\"type\":{\"type\":\"enum\",\"name\":\"RotationAxis\",\"symbols\":[\"PITCH\",\"ROLL\",\"YAW\"]}},{\"name\":\"minPosition\",\"type\":\"double\"},{\"name\":\"maxPosition\",\"type\":\"double\"}]}");
  public org.apache.avro.util.Utf8 boneName;
  public RotationAxis rotationAxis;
  public double minPosition;
  public double maxPosition;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return boneName;
    case 1: return rotationAxis;
    case 2: return minPosition;
    case 3: return maxPosition;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: boneName = (org.apache.avro.util.Utf8)value$; break;
    case 1: rotationAxis = (RotationAxis)value$; break;
    case 2: minPosition = (java.lang.Double)value$; break;
    case 3: maxPosition = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}

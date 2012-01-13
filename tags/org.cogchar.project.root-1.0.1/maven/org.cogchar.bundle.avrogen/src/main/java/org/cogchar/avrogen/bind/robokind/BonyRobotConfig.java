package org.cogchar.avrogen.bind.robokind;

@SuppressWarnings("all")
public class BonyRobotConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"BonyRobotConfig\",\"namespace\":\"org.cogchar.avrogen.bind.robokind\",\"fields\":[{\"name\":\"robotId\",\"type\":\"string\"},{\"name\":\"ogreModelPath\",\"type\":\"string\"},{\"name\":\"jointConfigs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"BonyJointConfig\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"jointId\",\"type\":\"int\"},{\"name\":\"boneRotations\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"BoneRotationRangeConfig\",\"fields\":[{\"name\":\"boneName\",\"type\":\"string\"},{\"name\":\"rotationAxis\",\"type\":{\"type\":\"enum\",\"name\":\"RotationAxis\",\"symbols\":[\"PITCH\",\"ROLL\",\"YAW\"]}},{\"name\":\"minPosition\",\"type\":\"double\"},{\"name\":\"maxPosition\",\"type\":\"double\"}]}}},{\"name\":\"normalizedDefaultPosition\",\"type\":\"double\"}]}}},{\"name\":\"initialBonePositions\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"BoneRotationConfig\",\"fields\":[{\"name\":\"boneName\",\"type\":\"string\"},{\"name\":\"rotationAxis\",\"type\":\"RotationAxis\"},{\"name\":\"rotationRadians\",\"type\":\"double\"}]}}}]}");
  public org.apache.avro.util.Utf8 robotId;
  public org.apache.avro.util.Utf8 ogreModelPath;
  public org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BonyJointConfig> jointConfigs;
  public org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BoneRotationConfig> initialBonePositions;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return robotId;
    case 1: return ogreModelPath;
    case 2: return jointConfigs;
    case 3: return initialBonePositions;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: robotId = (org.apache.avro.util.Utf8)value$; break;
    case 1: ogreModelPath = (org.apache.avro.util.Utf8)value$; break;
    case 2: jointConfigs = (org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BonyJointConfig>)value$; break;
    case 3: initialBonePositions = (org.apache.avro.generic.GenericArray<org.cogchar.avrogen.bind.robokind.BoneRotationConfig>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}


These libraries are copied from the JME3 
LWJGL-natives jar version of 2011-12-15.

They are copied into the output NBUI application,
in two locations, winding up in the "root" directory
of the application, which is where JME3 running under
the NBUI app is able to find them.

It would be better to pull these libs from
the LWJGL jar during the build of this project.
It would be better still to get the jars from an
native-library manifests in an OSGi bundle.

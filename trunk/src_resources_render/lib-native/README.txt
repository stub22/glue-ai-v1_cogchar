
These libraries are copied from the JME3 
LWJGL-natives jar version of 2012-02-09.

The native library files in this distribution (as
of 2012-03-16, at which time we are still using
the JME3 nightly build of 2012-02-09), have 
a build timestamp of 2011-11-13 9:16PM (MST?)

The "official" copy, from a Cogchar perspective,
is currently the one kept in our bundle:

	org.cogchar.bundle.render.resources
	
Other programs which need copies of the library should
copy them from the source tree (or output jar) of this
bundle.

They are copied into the output NBUI module, from which
they are copied into the eventual NB application
in TWO locations, most importantly in the "root" directory
of the application, which is where JME3 running under
the NBUI app is able to find them.

It would be better to pull these libs from
the LWJGL jar during the build of this project.

It would be better still to get the jars from an
native-library manifests in an OSGi bundle.

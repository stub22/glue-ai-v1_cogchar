
This directory contains a patched version of the Felix framework jar, version 2.0.3.

The only difference from the original is a single line added to the default.properties
file, which allows the framework, when running under JVM 1.6, to supply the 
sun.misc.Unsafe package for use by other bundles.

The problem we are working around is discussed here:

http://lists.apidesign.org/pipermail/netigso/2011-December/000203.html


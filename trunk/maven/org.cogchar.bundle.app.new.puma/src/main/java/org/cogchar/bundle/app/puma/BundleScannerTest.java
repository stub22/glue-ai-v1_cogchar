package org.cogchar.bundle.app.puma;

import org.cogchar.blob.circus.GraphScanTest;
import org.cogchar.blob.entry.BundleEntryHost;
import org.cogchar.bundle.core.CogcharCoreActivator;
import org.ontoware.rdf2go.model.Model;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class BundleScannerTest {

    public static void scanBundleFromContext(BundleContext context) throws Exception {
        // TODO add activation code here

        Bundle bundle = context.getBundle();
        scanBundleContents(bundle);
        System.exit(0);
    }

    public static void scanBundleCore() throws Exception {
        final Class<CogcharCoreActivator> bundleClass = org.cogchar.bundle.core.CogcharCoreActivator.class;
        scanBundleFromClass(bundleClass);
    }

    public static void scanBundleFromClass(Class bundleClass) throws Exception {

        // TODO add activation code here
        Bundle bundle = FrameworkUtil.getBundle(bundleClass);
        scanBundleContents(bundle);

        System.exit(0);
    }

    private static void scanBundleContents(Bundle bundle) {
        BundleEntryHost testBundleEntryHost = new BundleEntryHost(bundle);
        Model bundleScanResultModel = GraphScanTest.makeEmptyTempR2GoModel();

        String scannableFolderPath = "";// Replace with folder you would like to scan, or leave empty to scan entire OSGI bundle
        Integer maxEntries = 200;
        int foundRDFCount = GraphScanTest.scanDeepGraphFolderIntoGHostRecords(testBundleEntryHost, scannableFolderPath, scala.Int.unbox(maxEntries), bundleScanResultModel);
        System.out.printf("Deep-scanned OSGI Bundles \"%s\" folder and found %d results: %s\n", scannableFolderPath, foundRDFCount,
                bundleScanResultModel.getUnderlyingModelImplementation().toString());
        System.out.println(foundRDFCount);
    }
}

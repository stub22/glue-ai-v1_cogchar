package org.cogchar.bundle.app.puma;

import org.cogchar.blob.circus.GraphScanTest;
import org.cogchar.blob.entry.BundleEntryHost;
import org.ontoware.rdf2go.model.Model;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class BundleScannerTest {

    public static void scanBundle(BundleContext context) throws Exception {
        // TODO add activation code here

        Bundle bundle = context.getBundle();
        BundleEntryHost testBundleEntryHost = new BundleEntryHost(bundle);
        Model bundleScanResultModel = GraphScanTest.makeEmptyTempR2GoModel();

        String scannableFolderPath = "";// Replace with folder you would like to scan, or leave empty to scan entire OSGI bundle
        Integer maxEntries = 200;
        int foundRDFCount = GraphScanTest.scanDeepGraphFolderIntoGHostRecords(testBundleEntryHost, scannableFolderPath, scala.Int.unbox(maxEntries), bundleScanResultModel);
        System.out.printf("Deep-scanned OSGI Bundles \"%s\" folder and found %d results: %s\n", scannableFolderPath, foundRDFCount,
                bundleScanResultModel.getUnderlyingModelImplementation().toString());
        System.out.println(foundRDFCount);

        System.exit(0);
    }
}

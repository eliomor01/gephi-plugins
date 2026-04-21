package my.self.beautifulpdfs;

import org.junit.Assert;
import org.junit.Test;

public class BeautifulPDFExporterTest {

    @Test
    public void testDefaultValues() {
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();

        Assert.assertTrue(exporter.isRasterize());
        Assert.assertEquals(300, exporter.getDpi());
    }

    @Test
    public void testSetDpiClampsAtMinimumOne() {
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();

        exporter.setDpi(600);
        Assert.assertEquals(600, exporter.getDpi());

        exporter.setDpi(0);
        Assert.assertEquals(1, exporter.getDpi());

        exporter.setDpi(-42);
        Assert.assertEquals(1, exporter.getDpi());
    }

    @Test
    public void testRasterizeFlagRoundTrip() {
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();

        exporter.setRasterize(false);
        Assert.assertFalse(exporter.isRasterize());

        exporter.setRasterize(true);
        Assert.assertTrue(exporter.isRasterize());
    }
}

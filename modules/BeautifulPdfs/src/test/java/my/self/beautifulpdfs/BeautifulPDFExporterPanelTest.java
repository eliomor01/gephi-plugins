package my.self.beautifulpdfs;

import java.lang.reflect.Field;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Assert;
import org.junit.Test;

public class BeautifulPDFExporterPanelTest {

    private static final double POINTS_PER_MM = 2.8346456692895527;

    @Test
    public void testSetupMirrorsExporterValuesAndTogglesDpiEnabled() throws Exception {
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();
        exporter.setRasterize(false);
        exporter.setDpi(450);

        BeautifulPDFExporterPanel panel = new BeautifulPDFExporterPanel();
        panel.setup(exporter);

        JCheckBox rasterizeCheckbox = getField(panel, "rasterizeCheckbox", JCheckBox.class);
        JSpinner dpiSpinner = getField(panel, "dpiSpinner", JSpinner.class);

        Assert.assertFalse(rasterizeCheckbox.isSelected());
        Assert.assertEquals(450, ((Integer) dpiSpinner.getValue()).intValue());
        Assert.assertFalse("DPI control should be disabled when rasterization is off", dpiSpinner.isEnabled());
    }

    @Test
    public void testUnsetupWritesRasterizeAndDpiBackToExporter() throws Exception {
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();
        exporter.setRasterize(true);
        exporter.setDpi(300);

        BeautifulPDFExporterPanel panel = new BeautifulPDFExporterPanel();
        panel.setup(exporter);

        JCheckBox rasterizeCheckbox = getField(panel, "rasterizeCheckbox", JCheckBox.class);
        JSpinner dpiSpinner = getField(panel, "dpiSpinner", JSpinner.class);

        rasterizeCheckbox.setSelected(false);
        dpiSpinner.setValue(720);

        panel.unsetup(exporter);

        Assert.assertFalse(exporter.isRasterize());
        Assert.assertEquals(720, exporter.getDpi());
    }

    @Test
    public void testCustomSizeAndMarginsAreConvertedFromMmToPointsOnUnsetup() throws Exception {
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();
        BeautifulPDFExporterPanel panel = new BeautifulPDFExporterPanel();
        panel.setup(exporter);

        @SuppressWarnings("rawtypes")
        JComboBox pageSizeCombo = getField(panel, "pageSizeCombo", JComboBox.class);
        JTextField widthMmField = getField(panel, "widthMmField", JTextField.class);
        JTextField heightMmField = getField(panel, "heightMmField", JTextField.class);
        JTextField marginTopMmField = getField(panel, "marginTopMmField", JTextField.class);
        JTextField marginBottomMmField = getField(panel, "marginBottomMmField", JTextField.class);
        JTextField marginLeftMmField = getField(panel, "marginLeftMmField", JTextField.class);
        JTextField marginRightMmField = getField(panel, "marginRightMmField", JTextField.class);

        pageSizeCombo.setSelectedIndex(pageSizeCombo.getItemCount() - 1); // Custom

        widthMmField.setText("210");
        heightMmField.setText("297");
        marginTopMmField.setText("10");
        marginBottomMmField.setText("11");
        marginLeftMmField.setText("12");
        marginRightMmField.setText("13");

        panel.unsetup(exporter);

        PDRectangle page = exporter.getPageSize();
        Assert.assertEquals(210 * POINTS_PER_MM, page.getWidth(), 0.01);
        Assert.assertEquals(297 * POINTS_PER_MM, page.getHeight(), 0.01);
        Assert.assertEquals(10 * POINTS_PER_MM, exporter.getMarginTop(), 0.01);
        Assert.assertEquals(11 * POINTS_PER_MM, exporter.getMarginBottom(), 0.01);
        Assert.assertEquals(12 * POINTS_PER_MM, exporter.getMarginLeft(), 0.01);
        Assert.assertEquals(13 * POINTS_PER_MM, exporter.getMarginRight(), 0.01);
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}

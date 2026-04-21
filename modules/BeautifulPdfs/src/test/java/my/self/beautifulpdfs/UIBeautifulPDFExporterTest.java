package my.self.beautifulpdfs;

import java.lang.reflect.Field;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import org.junit.Assert;
import org.junit.Test;

public class UIBeautifulPDFExporterTest {

    @Test
    public void testSetupBeforeGetPanelStillAppliesState() throws Exception {
        UIBeautifulPDFExporter ui = new UIBeautifulPDFExporter();
        BeautifulPDFExporter exporter = new BeautifulPDFExporter();
        exporter.setRasterize(false);
        exporter.setDpi(555);

        ui.setup(exporter);
        ui.getPanel();

        BeautifulPDFExporterPanel panel = getField(ui, "panel", BeautifulPDFExporterPanel.class);
        JCheckBox rasterizeCheckbox = getField(panel, "rasterizeCheckbox", JCheckBox.class);
        JSpinner dpiSpinner = getField(panel, "dpiSpinner", JSpinner.class);

        Assert.assertFalse(rasterizeCheckbox.isSelected());
        Assert.assertEquals(555, ((Integer) dpiSpinner.getValue()).intValue());
        Assert.assertFalse(dpiSpinner.isEnabled());
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}

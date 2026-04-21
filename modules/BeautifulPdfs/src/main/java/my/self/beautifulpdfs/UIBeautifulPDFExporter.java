package my.self.beautifulpdfs;

import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ExporterUI.class)
public class UIBeautifulPDFExporter implements ExporterUI {

    private BeautifulPDFExporter exporter;
    private BeautifulPDFExporterPanel panel;

    @Override
    public JPanel getPanel() {
        panel = new BeautifulPDFExporterPanel();
        if (exporter != null) {
            panel.setup(exporter);
        }
        return panel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (BeautifulPDFExporter) exporter;
        if (panel != null) {
            panel.setup(this.exporter);
        }
    }

    @Override
    public void unsetup(boolean update) {
        if (update && exporter != null && panel != null) {
            panel.unsetup(exporter);
        }
        exporter = null;
        panel = null;
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof BeautifulPDFExporter;
    }

    @Override
    public String getDisplayName() {
        return "Beautiful PDF (Rasterized)";
    }
}

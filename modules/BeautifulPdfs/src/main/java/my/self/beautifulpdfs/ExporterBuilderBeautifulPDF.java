package my.self.beautifulpdfs;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.io.exporter.spi.VectorFileExporterBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = VectorFileExporterBuilder.class)
public class ExporterBuilderBeautifulPDF implements VectorFileExporterBuilder {

    @Override
    public VectorExporter buildExporter() {
        return new BeautifulPDFExporter();
    }

    @Override
    public FileType[] getFileTypes() {
        return new FileType[] {
            new FileType(".pdf", "PDF")
        };
    }

    @Override
    public String getName() {
        return "Beautiful PDF (Rasterized)";
    }
}

package my.self.beautifulpdfs;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Exports a graph to PDF using the standard Preview->PDF pipeline, then
 * rasterizes each page at the configured DPI and writes the final PDF.
 */
public class BeautifulPDFExporter implements ByteExporter, VectorExporter, LongTask {

    private ProgressTicket progress;
    private Workspace workspace;
    private OutputStream stream;
    private boolean cancel = false;
    private PDFTarget target;

    // Parameters (kept in sync with stock PDF exporter for compatibility)
    private float marginTop = 18f;
    private float marginBottom = 18f;
    private float marginLeft = 18f;
    private float marginRight = 18f;
    private boolean landscape = false;
    private PDRectangle pageSize = PDRectangle.A4;
    private boolean transparentBackground = false;
    private boolean rasterize = true;
    private int dpi = 300;

    @Override
    public boolean execute() {
        Progress.start(progress);

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
        controller.refreshPreview(workspace);
        PreviewProperties props = controller.getModel(workspace).getProperties();

        try (PDDocument vectorDoc = new PDDocument()) {
            vectorDoc.setVersion(1.5f);

            PDRectangle size = landscape ? new PDRectangle(pageSize.getHeight(), pageSize.getWidth()) : pageSize;
            PDPage page = new PDPage(size);
            vectorDoc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(vectorDoc, page)) {
                props.putValue(PDFTarget.LANDSCAPE, landscape);
                props.putValue(PDFTarget.PAGESIZE, size);
                props.putValue(PDFTarget.MARGIN_TOP, marginTop);
                props.putValue(PDFTarget.MARGIN_LEFT, marginLeft);
                props.putValue(PDFTarget.MARGIN_BOTTOM, marginBottom);
                props.putValue(PDFTarget.MARGIN_RIGHT, marginRight);
                props.putValue(PDFTarget.PDF_CONTENT_BYTE, contentStream);
                props.putValue(PDFTarget.PDF_DOCUMENT, vectorDoc);
                props.putValue(PDFTarget.TRANSPARENT_BACKGROUND, transparentBackground);

                target = (PDFTarget) controller.getRenderTarget(RenderTarget.PDF_TARGET, workspace);
                if (target instanceof LongTask) {
                    ((LongTask) target).setProgressTicket(progress);
                }

                controller.render(target, workspace);
            }

            if (rasterize) {
                rasterizeAndWrite(vectorDoc);
            } else {
                vectorDoc.save(stream);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Progress.finish(progress);

            props.putValue(PDFTarget.PDF_CONTENT_BYTE, null);
            props.putValue(PDFTarget.PAGESIZE, null);
            props.putValue(PDFTarget.PDF_DOCUMENT, null);
        }

        return !cancel;
    }

    private void rasterizeAndWrite(PDDocument vectorDoc) throws Exception {
        if (cancel) {
            return;
        }

        int safeDpi = Math.max(1, dpi);
        PDFRenderer renderer = new PDFRenderer(vectorDoc);

        try (PDDocument rasterizedDoc = new PDDocument()) {
            for (int i = 0; i < vectorDoc.getNumberOfPages(); i++) {
                if (cancel) {
                    return;
                }

                PDPage sourcePage = vectorDoc.getPage(i);
                PDRectangle mediaBox = sourcePage.getMediaBox();

                BufferedImage image = renderer.renderImageWithDPI(i, safeDpi, ImageType.RGB);

                PDPage outPage = new PDPage(mediaBox);
                rasterizedDoc.addPage(outPage);

                PDImageXObject pdImage = LosslessFactory.createFromImage(rasterizedDoc, image);
                try (PDPageContentStream outStream = new PDPageContentStream(rasterizedDoc, outPage)) {
                    outStream.drawImage(pdImage, 0, 0, mediaBox.getWidth(), mediaBox.getHeight());
                }
            }

            rasterizedDoc.save(stream);
        }
    }

    @Override
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public PDRectangle getPageSize() {
        return pageSize;
    }

    public void setPageSize(PDRectangle pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = Math.max(1, dpi);
    }

    public boolean isRasterize() {
        return rasterize;
    }

    public void setRasterize(boolean rasterize) {
        this.rasterize = rasterize;
    }
}

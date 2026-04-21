package my.self.beautifulpdfs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class BeautifulPDFExporterPanel extends JPanel {

    private static final double POINTS_PER_INCH = 72.0;
    private static final double POINTS_PER_MM = 2.8346456692895527;

    private final JComboBox<PageSizeOption> pageSizeCombo;
    private final JTextField widthMmField;
    private final JTextField heightMmField;
    private final JCheckBox landscapeCheckbox;
    private final JTextField marginTopMmField;
    private final JTextField marginBottomMmField;
    private final JTextField marginLeftMmField;
    private final JTextField marginRightMmField;
    private final JCheckBox transparentBackgroundCheckbox;
    private final JCheckBox rasterizeCheckbox;
    private final JSpinner dpiSpinner;
    private final List<PageSizeOption> sizeOptions;

    public BeautifulPDFExporterPanel() {
        super(new GridBagLayout());
        this.sizeOptions = buildPageSizeOptions();

        pageSizeCombo = new JComboBox<>(sizeOptions.toArray(new PageSizeOption[0]));
        widthMmField = new JTextField(8);
        heightMmField = new JTextField(8);
        landscapeCheckbox = new JCheckBox("Landscape");
        marginTopMmField = new JTextField(6);
        marginBottomMmField = new JTextField(6);
        marginLeftMmField = new JTextField(6);
        marginRightMmField = new JTextField(6);
        transparentBackgroundCheckbox = new JCheckBox("Transparent background");
        rasterizeCheckbox = new JCheckBox("Rasterize pages before saving");
        dpiSpinner = new JSpinner(new SpinnerNumberModel(300, 1, 4800, 1));

        pageSizeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                PageSizeOption selected = (PageSizeOption) pageSizeCombo.getSelectedItem();
                boolean isCustom = selected != null && selected.custom;
                widthMmField.setEnabled(isCustom);
                heightMmField.setEnabled(isCustom);
                if (!isCustom && selected != null) {
                    widthMmField.setText(formatDouble(pointsToMm(selected.pageSize.getWidth())));
                    heightMmField.setText(formatDouble(pointsToMm(selected.pageSize.getHeight())));
                }
            }
        });
        rasterizeCheckbox.addItemListener(e -> dpiSpinner.setEnabled(rasterizeCheckbox.isSelected()));

        buildLayout();
        PageSizeOption first = sizeOptions.get(0);
        widthMmField.setText(formatDouble(pointsToMm(first.pageSize.getWidth())));
        heightMmField.setText(formatDouble(pointsToMm(first.pageSize.getHeight())));
        widthMmField.setEnabled(false);
        heightMmField.setEnabled(false);
    }

    public void setup(BeautifulPDFExporter exporter) {
        PDRectangle pageSize = exporter.getPageSize();
        PageSizeOption matching = findMatching(pageSize);
        if (matching != null) {
            pageSizeCombo.setSelectedItem(matching);
            widthMmField.setText(formatDouble(pointsToMm(matching.pageSize.getWidth())));
            heightMmField.setText(formatDouble(pointsToMm(matching.pageSize.getHeight())));
            widthMmField.setEnabled(false);
            heightMmField.setEnabled(false);
        } else {
            pageSizeCombo.setSelectedItem(sizeOptions.get(sizeOptions.size() - 1));
            widthMmField.setText(formatDouble(pointsToMm(pageSize.getWidth())));
            heightMmField.setText(formatDouble(pointsToMm(pageSize.getHeight())));
            widthMmField.setEnabled(true);
            heightMmField.setEnabled(true);
        }

        landscapeCheckbox.setSelected(exporter.isLandscape());
        marginTopMmField.setText(formatDouble(pointsToMm(exporter.getMarginTop())));
        marginBottomMmField.setText(formatDouble(pointsToMm(exporter.getMarginBottom())));
        marginLeftMmField.setText(formatDouble(pointsToMm(exporter.getMarginLeft())));
        marginRightMmField.setText(formatDouble(pointsToMm(exporter.getMarginRight())));
        transparentBackgroundCheckbox.setSelected(exporter.isTransparentBackground());
        rasterizeCheckbox.setSelected(exporter.isRasterize());
        dpiSpinner.setValue(exporter.getDpi());
        dpiSpinner.setEnabled(rasterizeCheckbox.isSelected());
    }

    public void unsetup(BeautifulPDFExporter exporter) {
        PageSizeOption selected = (PageSizeOption) pageSizeCombo.getSelectedItem();
        PDRectangle pageSize;
        if (selected != null && !selected.custom) {
            pageSize = selected.pageSize;
        } else {
            float widthPt = (float) mmToPoints(parsePositive(widthMmField.getText(), pointsToMm(exporter.getPageSize().getWidth())));
            float heightPt = (float) mmToPoints(parsePositive(heightMmField.getText(), pointsToMm(exporter.getPageSize().getHeight())));
            pageSize = new PDRectangle(widthPt, heightPt);
        }
        exporter.setPageSize(pageSize);

        exporter.setLandscape(landscapeCheckbox.isSelected());
        exporter.setMarginTop((float) mmToPoints(parsePositive(marginTopMmField.getText(), pointsToMm(exporter.getMarginTop()))));
        exporter.setMarginBottom((float) mmToPoints(parsePositive(marginBottomMmField.getText(), pointsToMm(exporter.getMarginBottom()))));
        exporter.setMarginLeft((float) mmToPoints(parsePositive(marginLeftMmField.getText(), pointsToMm(exporter.getMarginLeft()))));
        exporter.setMarginRight((float) mmToPoints(parsePositive(marginRightMmField.getText(), pointsToMm(exporter.getMarginRight()))));
        exporter.setTransparentBackground(transparentBackgroundCheckbox.isSelected());
        exporter.setRasterize(rasterizeCheckbox.isSelected());
        exporter.setDpi((Integer) dpiSpinner.getValue());
    }

    private void buildLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(gbc, row++, "Page size:", pageSizeCombo);
        addRow(gbc, row++, "Custom width (mm):", widthMmField);
        addRow(gbc, row++, "Custom height (mm):", heightMmField);
        addRow(gbc, row++, "Orientation:", landscapeCheckbox);
        addRow(gbc, row++, "Margin top (mm):", marginTopMmField);
        addRow(gbc, row++, "Margin bottom (mm):", marginBottomMmField);
        addRow(gbc, row++, "Margin left (mm):", marginLeftMmField);
        addRow(gbc, row++, "Margin right (mm):", marginRightMmField);
        addRow(gbc, row++, "Background:", transparentBackgroundCheckbox);
        addRow(gbc, row++, "Rasterization:", rasterizeCheckbox);
        addRow(gbc, row, "Rasterization DPI:", dpiSpinner);
    }

    private void addRow(GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(component, gbc);
    }

    private List<PageSizeOption> buildPageSizeOptions() {
        List<PageSizeOption> options = new ArrayList<>();
        options.add(new PageSizeOption("A0", PDRectangle.A0));
        options.add(new PageSizeOption("A1", PDRectangle.A1));
        options.add(new PageSizeOption("A2", PDRectangle.A2));
        options.add(new PageSizeOption("A3", PDRectangle.A3));
        options.add(new PageSizeOption("A4", PDRectangle.A4));
        options.add(new PageSizeOption("A5", PDRectangle.A5));
        options.add(new PageSizeOption("Letter", PDRectangle.LETTER));
        options.add(new PageSizeOption("Legal", PDRectangle.LEGAL));
        options.add(PageSizeOption.custom());
        return options;
    }

    private PageSizeOption findMatching(PDRectangle pageSize) {
        for (PageSizeOption option : sizeOptions) {
            if (option.custom) {
                continue;
            }
            if (almostEqual(option.pageSize.getWidth(), pageSize.getWidth())
                && almostEqual(option.pageSize.getHeight(), pageSize.getHeight())) {
                return option;
            }
        }
        return null;
    }

    private static boolean almostEqual(float a, float b) {
        return Math.abs(a - b) < 0.01f;
    }

    private static double parsePositive(String text, double fallback) {
        try {
            double value = Double.parseDouble(text.trim());
            if (value > 0) {
                return value;
            }
        } catch (RuntimeException ignored) {
            // Fallback below
        }
        return fallback;
    }

    private static String formatDouble(double value) {
        return String.format(java.util.Locale.US, "%.3f", value);
    }

    private static double pointsToMm(double points) {
        return points / POINTS_PER_MM;
    }

    @SuppressWarnings("unused")
    private static double pointsToInch(double points) {
        return points / POINTS_PER_INCH;
    }

    private static double mmToPoints(double mm) {
        return mm * POINTS_PER_MM;
    }

    private static class PageSizeOption {

        private final String name;
        private final PDRectangle pageSize;
        private final boolean custom;

        private PageSizeOption(String name, PDRectangle pageSize) {
            this.name = name;
            this.pageSize = pageSize;
            this.custom = false;
        }

        private PageSizeOption(String name, boolean custom) {
            this.name = name;
            this.pageSize = null;
            this.custom = custom;
        }

        public static PageSizeOption custom() {
            return new PageSizeOption("Custom", true);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

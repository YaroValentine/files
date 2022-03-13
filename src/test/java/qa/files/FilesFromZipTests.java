package qa.files;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

public class FilesFromZipTests {

    @Test
    void zipFilesAndStoreInResources() throws Exception {
        ZipFile zipFile = new ZipFile("src/test/resources/files/my-zip.zip");
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String fileName = entry.getName();

            if (fileName.contains(".pdf")) verifyAuthorsName(zipFile.getInputStream(entry), "Marc Philipp");
            if (fileName.contains(".xls")) verifyCompanyAddress(zipFile.getInputStream(entry), "693010");
            if (fileName.contains(".csv")) verifyFirstCell(zipFile.getInputStream(entry), "Month");
        }
    }

    private void verifyFirstCell(InputStream is, String value) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            List<String[]> content = reader.readAll();
            assertThat(content.get(0)[0]).isEqualTo(value);
        }
    }

    private void verifyCompanyAddress(InputStream is, String companyAddress) throws IOException {
        XLS xls = new XLS(is);
        String cellText = xls.excel.getSheetAt(0).getRow(11).getCell(1).toString().split(",")[0];
        assertThat(cellText).isEqualTo(companyAddress);
    }

    private void verifyAuthorsName(InputStream is, String authorsName) throws IOException {
        PDF pdf = new PDF(is);
        assertThat(pdf.author).contains(authorsName);
    }

}

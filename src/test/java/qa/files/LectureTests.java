package qa.files;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;
import qa.files.domain.Student;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.open;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;

public class LectureTests {

    ClassLoader classLoader = getClass().getClassLoader();

    @Test
    void downloadFileTest() throws IOException {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File downloadedFile = Selenide.$("#raw-url").download();
        try (InputStream is = new FileInputStream(downloadedFile)) {
            assertThat(new String(is.readAllBytes(), UTF_8))
                    .contains("This repository is the home of the next generation of JUnit");
        }

        //nio
        String readString = Files.readString(downloadedFile.toPath(), UTF_8);
        assertThat(readString).contains("This repository is the home of the next generation of JUnit");
    }

    @Test
    void uploadFileTest() {
        Selenide.open("https://the-internet.herokuapp.com/upload");
        Selenide.$("#file-upload").uploadFromClasspath("files/1.txt");
        Selenide.$("#file-submit").click();
        Selenide.$("div.example").shouldHave(text("File Uploaded!"));
        Selenide.$("#uploaded-files").shouldHave(text("1.txt"));
    }

    @Test
    void parsePdfTest() throws IOException {
        open("https://junit.org/junit5/docs/current/user-guide/");
        File pdfDownload = Selenide.$(Selectors.byText("PDF download")).download();
        PDF pdf = new PDF(pdfDownload);
        assertThat(pdf.author).contains("Marc Philipp");
        assertThat(pdf.numberOfPages).isEqualTo(166);
    }

    @Test
    void parseXlsTest() throws FileNotFoundException {
        Selenide.open("http://romashka2008.ru/price");
        File xlsDownload = Selenide.$(".site-main__inner a[href*='prajs_ot']").download();
        XLS xls = new XLS(xlsDownload);
        assertThat(xls.excel
                .getSheetAt(0)
                .getRow(32)
                .getCell(2)
                .getStringCellValue().contains("Бумага для широкоформатных принтеров и чертежных работ")
        );
    }

    @Test
    void parseCsvTest() throws Exception {
        //can't call following from @BeforeAll because not static methods
        ClassLoader classLoader = getClass().getClassLoader();
        //Almost the same
        ClassLoader classLoader2 = LectureTests.class.getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream("files/to_zip/sample1.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            List<String[]> content = reader.readAll();
            assertThat(content.get(0)).contains(
                    "Month",
                    "Average",
                    "2005",
                    "2006",
                    "2007",
                    "2008"
            );
        }
    }


    @Test
    void parseZipTest() throws Exception {
        try (InputStream is = classLoader.getResourceAsStream("files/zip-with-files.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
//                Assertions.assertThat(entry.getName()).isEqualTo("file 1.txt");
                System.out.println(entry.getName());
            }
        }
    }

    @Test
    void parseJsonCommonTest() throws Exception {
        Gson gson = new Gson();
        try (InputStream is = classLoader.getResourceAsStream("files/simple.json")) {
            String json = new String(is.readAllBytes(), UTF_8);
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            //JSON > "name": "Yaro"
            assertThat(jsonObject.get("name").getAsString()).isEqualTo("Yaro");
            //JSON > "address": {"street": "Mira"}
            assertThat(jsonObject.get("address").getAsJsonObject().get("street").getAsString()).isEqualTo("Mira");
            //JSON >  "favorite_music": ["Metallica"]
            assertThat(jsonObject.get("favorite_music").getAsJsonArray().get(0).getAsString()).isEqualTo("Metallica");
        }
    }

    @Test
    void parseJsonTypeTest() throws Exception {
        Gson gson = new Gson();
        try (InputStream is = classLoader.getResourceAsStream("files/simple.json")) {
            String json = new String(is.readAllBytes(), UTF_8);
            Student jsonObject = gson.fromJson(json, Student.class);
            assertThat(jsonObject.name).isEqualTo("Yaro");
            assertThat(jsonObject.address.street).isEqualTo("Mira");
            assertThat(jsonObject.favoriteMusic.get(0)).isEqualTo("Metallica");
        }
    }
}

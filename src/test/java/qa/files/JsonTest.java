package qa.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import qa.files.domain.Student;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTest {

    @Test
    void parseJsonTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is =  getClass().getClassLoader().getResourceAsStream("files/simple.json")) {
            Student student = mapper.readValue(is, Student.class);

            assertThat(student.name).isEqualTo("Yaro");
            assertThat(student.surname).isEqualTo("V");
            assertThat(student.favoriteMusic).contains("KoRn");
            assertThat(student.address.street).contains("Mira");

        }
    }
}

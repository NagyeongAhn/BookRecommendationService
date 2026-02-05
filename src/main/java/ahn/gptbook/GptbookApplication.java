package ahn.gptbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(modifyOnCreate = true)
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "ahn.gptbook.libraryapi")
public class GptbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(GptbookApplication.class, args);
    }

}

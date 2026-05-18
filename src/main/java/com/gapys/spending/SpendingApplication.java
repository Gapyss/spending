package com.gapys.spending;

import com.gapys.spending.pdf.OcrProperties;
import com.gapys.spending.pdf.PdfProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({PdfProperties.class, OcrProperties.class})
public class SpendingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpendingApplication.class, args);
	}

}

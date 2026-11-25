package com.lukmn.fundamental;

import com.lukmn.fundamental.domain.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan 
public class FundamentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundamentalApplication.class, args);
		System.out.println(User.register("  LukMAN ", "{bcrypt}secret"));

	}

}

/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.heroku;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Controller
@SpringBootApplication
public class HerokuApplication {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  public static void main(String[] args) {
    SpringApplication.run(HerokuApplication.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource().getConnection()) {

      Statement stmt = connection.createStatement();

      // Create table if it doesn't exist
      stmt.executeUpdate("DROP TABLE IF EXISTS ticks");

      stmt.executeUpdate(
        "CREATE TABLE ticks (" +
        "tick TIMESTAMP, " +
        "random_string VARCHAR(255))"
      );


      // Insert a NEW row every request
      stmt.executeUpdate(
        "INSERT INTO ticks (tick, random_string) VALUES (now(), '" +
        getRandomString() + "')"
      );

      // Read ALL rows
      ResultSet rs = stmt.executeQuery(
        "SELECT tick, random_string FROM ticks ORDER BY tick DESC"
      );

      ArrayList<String> output = new ArrayList<>();

      while (rs.next()) {
        output.add(
          "Read from DB: " +
          rs.getTimestamp("tick") + " " +
          rs.getString("random_string")
        );
      }

      model.put("records", output);
      return "db";

    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  // Generates a short random string
  private String getRandomString() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }
}


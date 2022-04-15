package com.example.demo.configuration;


import com.example.demo.DemoApplication;
import com.example.demo.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;

@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@SpringBootTest(classes = DemoApplication.class)
@Rollback
public class IntegrationBaseTest {

    @Autowired
    private TruncateDatabaseService truncateDatabaseService;

    @BeforeEach
    protected void setUp() {
        try {
            truncateDatabaseService.restartIdWith(1, true, null);
        } catch (SQLException e) {
        } finally {
            truncateDatabaseService.closeResource();
        }
    }
}

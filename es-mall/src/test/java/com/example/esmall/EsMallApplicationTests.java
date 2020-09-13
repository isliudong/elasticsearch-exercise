package com.example.esmall;

import com.example.esmall.service.MallService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@SpringBootTest
class EsMallApplicationTests {

    @Autowired
    MallService mallService;
    @Test
    void contextLoads() {

    }

    @Test
    public void test() throws IOException {
        mallService.initEs("java");
    }

}

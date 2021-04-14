package ru.magnit.demo.dto;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@Scope("prototype")
public class CodeStorage {
    private int code;

    public int getCode() {
        return code;
    }

    public void generateCode() {
        code = 10_000 + (int) ( Math.random() * 9999 );
    }

}

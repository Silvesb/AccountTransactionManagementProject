package tuum.example.tuum.exception;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorMessage {
    private int status;
    private Date timestamp;
    private String message;
}

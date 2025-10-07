package com.example.fixspotdesktop.net;

/** Excepción controlada para respuestas HTTP no 2xx. */
public class HttpResponseException extends RuntimeException {
    private final int status;     //
    private final String detail;  //

    public HttpResponseException(int status, String detail) {
        super(detail);
        this.status = status;
        this.detail = detail;
    }

    public int getStatus() { return status; }
    public String getDetail() { return detail; }
}

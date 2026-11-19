package br.ufc.libraryupd.messaging;

import java.io.Serializable;

public class LibraryEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tipo;
    private final String bookId;
    private final String titulo;
    private final String userId;
    private final String mensagem;
    private final long timestamp = System.currentTimeMillis();

    public LibraryEvent(String tipo, String bookId, String titulo, String userId, String mensagem) {
        this.tipo = tipo;
        this.bookId = bookId;
        this.titulo = titulo;
        this.userId = userId;
        this.mensagem = mensagem;
    }

    public String getTipo() { return tipo; }
    public String getBookId() { return bookId; }
    public String getTitulo() { return titulo; }
    public String getUserId() { return userId; }
    public String getMensagem() { return mensagem; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() { return "[" + tipo + "] " + mensagem; }
}
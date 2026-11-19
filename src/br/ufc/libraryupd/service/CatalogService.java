package br.ufc.libraryupd.service;

import br.ufc.libraryupd.messaging.GroupBus;
import br.ufc.libraryupd.messaging.LibraryEvent;
import br.ufc.libraryupd.model.Book;

import java.util.ArrayList;
import java.util.List;

public class CatalogService {
    private final List<Book> catalogo = new ArrayList<>();
    private final GroupBus bus;

    public CatalogService(GroupBus bus) {
        this.bus = bus;
        bus.registrar((e, recuperado) -> {
            String sufixo = recuperado ? " (recuperado)" : "";
            if (e.getTipo().equals("livro.emprestado")) {
                System.out.println("[Catálogo] Livro emprestado: " + e.getTitulo() + sufixo);
            } else if (e.getTipo().equals("livro.devolvido")) {
                System.out.println("[Catálogo] Livro devolvido: " + e.getTitulo() + sufixo);
            }
        });
    }

    public void addLivro(Book livro) {
        catalogo.add(livro);
        try {
            bus.publicar(new LibraryEvent("livro.novo", livro.getId(), livro.getTitle(), null,
                    "Novo lançamento no catálogo: " + livro.getTitle() + " está disponível!"));
        } catch (Exception e) {
            System.out.println("Erro ao enviar ao grupo: " + e.getMessage());
        }
        System.out.println("ok, livro cadastrado");
    }

    public List<Book> listLivros() { return catalogo; }

    public Book buscarPorId(String id) {
        for (Book l : catalogo) if (l.getId().equals(id)) return l;
        return null;
    }
}
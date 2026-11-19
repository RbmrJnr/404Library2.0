package br.ufc.libraryupd.service;

import br.ufc.libraryupd.messaging.GroupBus;
import br.ufc.libraryupd.messaging.LibraryEvent;
import br.ufc.libraryupd.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final List<User> usuarios = new ArrayList<>();
    private final Map<String, String> livrosConhecidos = new ConcurrentHashMap<>(); // idLivro -> título
    private final Map<String, String> emprestimos = new ConcurrentHashMap<>();      // idLivro -> idUsuario
    private final GroupBus bus;

    public UserService(GroupBus bus) {
        this.bus = bus;
        bus.registrar((e, recuperado) -> {
            String sufixo = recuperado ? " (recuperado)" : "";
            switch (e.getTipo()) {
                case "livro.novo":
                    livrosConhecidos.put(e.getBookId(), e.getTitulo());
                    System.out.println("[Usuários] Catálogo atualizado: " + e.getTitulo() + sufixo);
                    break;
                case "livro.emprestado":
                    emprestimos.put(e.getBookId(), e.getUserId());
                    System.out.println("[Usuários] Livro indisponível: " + e.getTitulo() + sufixo);
                    break;
                case "livro.devolvido":
                    emprestimos.remove(e.getBookId());
                    System.out.println("[Usuários] Livro disponível novamente: " + e.getTitulo() + sufixo);
                    break;
                default:
                    break;
            }
        });
    }

    public void cadastrarUsuario(User usuario) {
        for (User u : usuarios) {
            if (u.getId().equals(usuario.getId())) {
                System.out.println("Já existe um usuário cadastrado com o ID " + usuario.getId());
                return;
            }
        }
        usuarios.add(usuario);
        System.out.println("Usuário cadastrado com sucesso: " + usuario.getUsername());
    }

    public User buscarPorId(String id) {
        for (User u : usuarios) if (u.getId().equals(id)) return u;
        System.out.println("ID de usuário " + id + " não encontrado.");
        return null;
    }

    public List<User> listarUsuarios() { return usuarios; }

    public void emprestarLivro(String idUsuario, String idLivro) {
        User usuario = buscarPorId(idUsuario);
        String titulo = livrosConhecidos.get(idLivro);
        if (usuario == null) { System.out.println("Usuário não encontrado no sistema."); return; }
        if (titulo == null) { System.out.println("Livro não disponível no catálogo."); return; }
        if (emprestimos.containsKey(idLivro)) {
            System.out.println("O livro " + titulo + " já está emprestado.");
            return;
        }

        emprestimos.put(idLivro, idUsuario); // atualiza o estado local
        System.out.println("O usuário " + usuario.getUsername() + " pegou o livro " + titulo + " emprestado.");
        try {
            bus.publicar(new LibraryEvent("livro.emprestado", idLivro, titulo, idUsuario,
                    "O livro " + titulo + " acabou de ser alugado e não está mais disponível!"));
        } catch (Exception e) {
            System.out.println("Erro ao enviar ao grupo: " + e.getMessage());
        }
    }

    public void devolverLivro(String idUsuario, String idLivro) {
        User usuario = buscarPorId(idUsuario);
        String titulo = livrosConhecidos.get(idLivro);
        if (usuario == null) { System.out.println("Usuário não encontrado no sistema."); return; }
        if (titulo == null) { System.out.println("Livro não existe no catálogo."); return; }

        String comQuem = emprestimos.get(idLivro);
        if (comQuem == null) {
            System.out.println("O livro " + titulo + " não está emprestado.");
            return;
        }
        if (!comQuem.equals(idUsuario)) {
            System.out.println("Este livro não está emprestado para o usuário " + usuario.getUsername() + ".");
            return;
        }

        emprestimos.remove(idLivro);
        System.out.println("O usuário " + usuario.getUsername() + " devolveu o livro " + titulo + ".");
        try {
            bus.publicar(new LibraryEvent("livro.devolvido", idLivro, titulo, idUsuario,
                    "O livro " + titulo + " foi devolvido e está disponível novamente!"));
        } catch (Exception e) {
            System.out.println("Erro ao enviar ao grupo: " + e.getMessage());
        }
    }
}
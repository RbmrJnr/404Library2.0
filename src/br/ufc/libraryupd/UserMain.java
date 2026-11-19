package br.ufc.libraryupd;

import br.ufc.libraryupd.messaging.GroupBus;
import br.ufc.libraryupd.model.User;
import br.ufc.libraryupd.service.UserService;
import java.util.Scanner;

public class UserMain {
    public static void main(String[] args) throws Exception {
        GroupBus bus = new GroupBus("usuarios");
        UserService service = new UserService(bus);
        bus.iniciar();

        Scanner in = new Scanner(System.in);
        System.out.println("Usuários no ar. Comandos: 'u;id;nome;matricula' | 'e;idUsuario;idLivro' | 'd;idUsuario;idLivro' | 'sair'");
        while (true) {
            String linha = in.nextLine();
            if (linha.equalsIgnoreCase("sair")) break;
            String[] p = linha.split(";");
            if (p[0].equals("u") && p.length == 4) service.cadastrarUsuario(new User(p[1], p[2], p[3]));
            else if (p[0].equals("e") && p.length == 3) service.emprestarLivro(p[1], p[2]);
            else if (p[0].equals("d") && p.length == 3) service.devolverLivro(p[1], p[2]);
        }
        bus.close();
    }
}
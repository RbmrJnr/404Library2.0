package br.ufc.libraryupd;

import br.ufc.libraryupd.messaging.GroupBus;
import br.ufc.libraryupd.model.Book;
import br.ufc.libraryupd.service.CatalogService;
import java.util.Scanner;

public class CatalogMain {
    public static void main(String[] args) throws Exception {
        GroupBus bus = new GroupBus("catalogo");
        CatalogService catalogo = new CatalogService(bus);
        bus.iniciar();

        Scanner in = new Scanner(System.in);
        System.out.println("Catálogo no ar. Digite: id;título;autor (ou 'sair')");
        while (true) {
            String linha = in.nextLine();
            if (linha.equalsIgnoreCase("sair")) break;
            String[] p = linha.split(";");
            if (p.length == 3) catalogo.addLivro(new Book(p[0], p[1], p[2]));
        }
        bus.close();
    }
}
package br.ufc.libraryupd;

import br.ufc.libraryupd.messaging.GroupBus;

public class NotificadorMain {
    public static void main(String[] args) throws Exception {
        GroupBus bus = new GroupBus("notificador-" + System.currentTimeMillis() % 1000);
        bus.registrar((e, recuperado) ->
                System.out.println((recuperado ? "📜 [perdida enquanto offline] " : "📢 ") + e));
        bus.iniciar();
        System.out.println("Notificador no grupo. Aguardando eventos... (Ctrl+C para sair)");
        Thread.currentThread().join();
    }
}
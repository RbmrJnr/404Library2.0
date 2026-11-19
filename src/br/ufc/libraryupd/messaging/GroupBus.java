package br.ufc.libraryupd.messaging;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GroupBus implements Receiver, AutoCloseable {

    public static final String GRUPO = "biblioteca";

    public interface EventHandler {
        void onEvent(LibraryEvent evento, boolean recuperado);
    }

    private final JChannel channel;
    private final List<EventHandler> handlers = new CopyOnWriteArrayList<>();
    private final List<LibraryEvent> historico = new ArrayList<>();

    public GroupBus(String nomeDoNo) throws Exception {
        channel = new JChannel();
        channel.setName(nomeDoNo);
        channel.setReceiver(this);
        channel.setDiscardOwnMessages(true);
    }

    public void registrar(EventHandler handler) {
        handlers.add(handler);
    }

    public void iniciar() throws Exception {
        channel.connect(GRUPO);
        channel.getState(null, 10_000);
    }

    public void publicar(LibraryEvent evento) throws Exception {
        synchronized (historico) {
            historico.add(evento);
        }
        channel.send(null, evento);
    }

    @Override
    public void receive(Message msg) {
        LibraryEvent evento = msg.getObject();
        synchronized (historico) {
            historico.add(evento);
        }
        for (EventHandler h : handlers) h.onEvent(evento, false);
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("👥 Visão do grupo: " + view.getMembers().size()
                + " membro(s) -> " + view.getMembers());
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (historico) {
            Util.objectToStream(new ArrayList<>(historico), new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        List<LibraryEvent> recebidos = Util.objectFromStream(new DataInputStream(input));
        synchronized (historico) {
            historico.clear();
            historico.addAll(recebidos);
        }
        System.out.println("♻️ Estado recuperado: " + recebidos.size() + " evento(s) do histórico");
        for (LibraryEvent e : recebidos) {
            for (EventHandler h : handlers) h.onEvent(e, true);
        }
    }

    @Override
    public void close() {
        channel.close();
    }
}
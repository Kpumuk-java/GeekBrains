package java2.lesson6.server.service;

import java2.lesson6.server.handler.ClientHandler;
import java2.lesson6.server.inter.AuthService;
import java2.lesson6.server.inter.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;



public class ServerImpl implements Server {


    private static final Logger LOGGER = LogManager.getLogger(ServerImpl.class);
    private List<ClientHandler> clients;
    private AuthService authService;

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            authService = new AuthServiceImpl();
            authService.start();
            clients = new LinkedList<>();
            while (true) {
                System.out.println("Wait join clients");
                Socket socket = serverSocket.accept();
                System.out.println("Client join");
                new ClientHandler(this, socket);
                LOGGER.info("Connect client");
            }
        } catch (IOException e) {
            LOGGER.warn("Problem in server", e);
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    @Override
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNick() != null && c.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    @Override
    public AuthService getAuthService() {
        return authService;
    }

    /**
     *
     * @param nick ник который ищет метод
     * @return возвращает при совпадении конкретный ClientHandler
     */
    @Override
    public synchronized ClientHandler getClientHandler(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(nick)) {
                return c;
            }
        }
        return null;
    }

    /**
     *
     * @return Строку содержащую ники ClientHandler кто в сети
     */
    @Override
    public synchronized String broadcastClientList() {
        StringBuilder builder = new StringBuilder("Онлайн: ");
        for (ClientHandler c : clients) {
            builder.append(c.getNick() + " ");
        }
        return builder.toString();
    }

    /**
     * Заменяет старый ник beforeNick в базе данных и ClientHandler на новый afterNick
     * @param beforeNick
     * @param afterNick
     */
    @Override
    public synchronized void changeNickOnServer(String beforeNick, String afterNick) {
        if (getClientHandler(beforeNick) != null) {
            authService.changeNick(beforeNick, afterNick);
            getClientHandler(beforeNick).setNick(afterNick);
        }
    }


}
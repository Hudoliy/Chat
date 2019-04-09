/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author Худолий Евгений
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
		// порт, який буде прослуховувати наш сервер
    static final int PORT = 3443;
		// список клієнтів, які будуть підключатися до сервера
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    public Server() {   	// сокет клієнта, це якийсь потік, який буде підключатися до сервера
				// за адресою і порту
        Socket clientSocket = null;	// серверний сокет
        ServerSocket serverSocket = null;
        try {	// створюємо серверний сокет на певному порту
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер старт!");	// запускаємо нескінченний цикл
            while (true) {		// таким чином чекаємо підключень від сервера
                clientSocket = serverSocket.accept();	// створюємо обробник клієнта, який підключився до сервера
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);	// кожне підключення клієнта обробляємо в новому потоці
                new Thread(client).start();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {   // закриваємо підключення
                clientSocket.close();
                System.out.println("Сервер зупинено");
                serverSocket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }		
		// відправляємо повідомлення всім клієнтам
    public void sendMessageToAllClients(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }

    }
		// видаляємо клієнта з колекції при виході з чату
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

}

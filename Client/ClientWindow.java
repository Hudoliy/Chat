package client;

/**
 *
 * @author Худолий Евгений
 */

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientWindow extends JFrame {
    // адреса серверу
    private static final String SERVER_HOST = "localhost";
    // порт
    private static final int SERVER_PORT = 3443;
    // клієнтський сокет
    private Socket clientSocket;
    // вхідні повідомлення
    private Scanner inMessage;
    // вихідні повідомлення
    private PrintWriter outMessage;
    // наступні поля відповідають за елементи форми
    private JTextField jtfMessage;
    private JTextField jtfName;
    private JTextArea jtaTextAreaMessage;
    // ім'я клієнта
    private String clientName = "";
    // отримуємо ім'я клієнта
    public String getClientName() {
        return this.clientName;
    }

    // конструктор
    public ClientWindow() {
        try {
            // підключаємося до сервера
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Задаємо настройки елементів на формі
        setBounds(600, 300, 600, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jtaTextAreaMessage = new JTextArea();
        jtaTextAreaMessage.setEditable(false);
        jtaTextAreaMessage.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
        add(jsp, BorderLayout.CENTER);
        // label,який буде відображати кількість клієнтів в чаті
        JLabel jlNumberOfClients = new JLabel("Кількість користувачів в чаті: ");
        add(jlNumberOfClients, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSendMessage = new JButton("Надіслати");
        bottomPanel.add(jbSendMessage, BorderLayout.EAST);
        jtfMessage = new JTextField("Ваше повідомлення: ");
        bottomPanel.add(jtfMessage, BorderLayout.CENTER);
        jtfName = new JTextField("Ваше ім*я: ");
        bottomPanel.add(jtfName, BorderLayout.WEST);
        // обробник події натискання кнопки відправки повідомлення
        jbSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // якщо ім'я клієнта, і повідомлення непусті, то відправляємо повідомлення
                if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
                    clientName = jtfName.getText();
                    sendMsg();
                    // фокус на текстове поле з повідомленням
                    jtfMessage.grabFocus();
                }
            }
        });
        // при фокусі поле повідомлення очищається
        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfMessage.setText("");
            }
        });
        // при фокусі поле ім'я очищається
        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfName.setText("");
            }
        });
        // в окремому потоці починаємо роботу з сервером
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // цикл
                    while (true) {
                        // якщо є вхідне повідомлення
                        if (inMessage.hasNext()) {
                            // зчитується
                            String inMes = inMessage.nextLine();
                            String clientsInChat = "Кількість користувачів = ";
                            if (inMes.indexOf(clientsInChat) == 0) {
                                jlNumberOfClients.setText(inMes);
                            } else {
                                // відображення повідомлення
                                jtaTextAreaMessage.append(inMes);
                                // перехід но новий рядок
                                jtaTextAreaMessage.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }).start();
        // додаємо обробник події закриття вікна клієнтської програми
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    // тут перевіряємо, що ім'я клієнта непорожнє і не дорівнює значенню за замовчуванням
                    if (!clientName.isEmpty() && clientName != "Введите ваше имя: ") {
                        outMessage.println(clientName + " вышел из чата!");
                    } else {
                        outMessage.println("Участник вышел из чата, так и не представившись!");
                    }
                    // відправляємо службове повідомлення, яке є ознакою того, що клієнт вийшов з чату
                    outMessage.println("##session##end##");
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                } catch (IOException exc) {

                }
            }
        });
        // відображаємо форму
        setVisible(true);
    }

    // відправка повідомлення
    public void sendMsg() {
        // формуємо повідомлення для відправки на сервер
        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
        // відправляємо
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }
}

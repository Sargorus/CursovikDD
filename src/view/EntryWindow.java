package view;
import java.awt.*;
import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class EntryWindow extends JFrame {
    private JLabel helloWindow=new JLabel("Добро пожаловать", SwingConstants.CENTER);

    private JTextField newLogin=new JTextField("Введите логин");
    private JPasswordField newPassword=new JPasswordField("Введите пароль");
    private JButton entryButton=new JButton("Войти");

    private final String loginHint = "Введите логин";
    private final String passwordHint = "Введите пароль";

    public JTextField getNewLogin() {
        return newLogin;
    }

    public JPasswordField getNewPassword() {
        return newPassword;
    }

    public JButton getEntryButton() {
        return entryButton;
    }

    public String getLoginHint() {
        return loginHint;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    public EntryWindow(String s){
        super(s);
        setSize(300,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4,1));
        add(helloWindow);
        add(newLogin);
        add(newPassword);
        add(entryButton);

        helloWindow.setFont(new Font("Arial", Font.BOLD, 15));
        helloWindow.setForeground(new Color(75, 0, 130));

        newLogin.setFont(new Font("Arial", Font.BOLD, 10));
        newLogin.setForeground(new Color(75, 0, 130));

        newPassword.setFont(new Font("Arial", Font.BOLD, 10));
        newPassword.setForeground(new Color(75, 0, 130));

        newPassword.setEchoChar((char) 0);

        addFocusListeners();

    }


    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addFocusListeners() {
        newLogin.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // При получении фокуса: если текст равен подсказке, очищаем поле
                if (newLogin.getText().equals(loginHint)) {
                    newLogin.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // При потере фокуса: если поле пустое, возвращаем подсказку
                if (newLogin.getText().isEmpty()) {
                    newLogin.setText(loginHint);
                }
            }
        });

        newPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // При получении фокуса: если текст равен подсказке, очищаем поле
                newPassword.setEchoChar('*');
                String passwordText = new String(newPassword.getPassword());
                if (passwordText.equals(passwordHint)) {
                    newPassword.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // При потере фокуса: если поле пустое, возвращаем подсказку
                if (newPassword.getPassword().length == 0) {
                    newPassword.setText(passwordHint);
                    newPassword.setEchoChar((char) 0);
                }
            }
        });
    }

    public void clearFields() {
        newLogin.setText(loginHint);
        newPassword.setText(passwordHint);
        newPassword.setEchoChar((char) 0);
    }

    public void close() {
        this.dispose();
    }
}

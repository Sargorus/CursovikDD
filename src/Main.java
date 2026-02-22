//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.awt.*;

public class Main
{
    public static void main(String[] args)
    {
        Frame awtFrame = new Frame("Просто окно Jast");

        Label labelFrame = new Label("Перывый текст");

        awtFrame.add(labelFrame);
        awtFrame.setSize(300,200);
        awtFrame.setVisible(true);
    }
}
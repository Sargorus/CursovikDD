import java.awt.*;

public class Guitest {

    public void Gui() {
        Frame awtFrame = new Frame("Просто окно Jast");

        Label labelFrame = new Label("Перывый текст");

        awtFrame.add(labelFrame);
        awtFrame.setSize(500, 600);
        awtFrame.setVisible(true);

        
    }

}

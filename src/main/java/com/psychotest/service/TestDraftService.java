package main.java.com.psychotest.service;

import main.java.com.psychotest.model.TestState;
import java.io.*;

public class TestDraftService {
    private static final String DRAFT_DIR = "drafts";

    public TestDraftService() {
        // Создаём папку для черновиков, если её нет
        File dir = new File(DRAFT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // Сохранить черновик в файл
    public boolean saveDraft(int teacherId, TestState state) {
        String filename = DRAFT_DIR + "/draft_" + teacherId + ".ser";

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(state);
            System.out.println("Черновик сохранён: " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении черновика: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Загрузить последний черновик
    public TestState loadLastDraft(int teacherId) {
        String filename = DRAFT_DIR + "/draft_" + teacherId + ".ser";
        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("Черновик не найден, создаём новый тест");
            return new TestState();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            TestState state = (TestState) ois.readObject();
            System.out.println("Черновик загружен: " + filename);
            return state;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке черновика: " + e.getMessage());
            e.printStackTrace();
            return new TestState();
        }
    }

    // Очистить черновик после сохранения теста
    public boolean clearDraft(int teacherId) {
        String filename = DRAFT_DIR + "/draft_" + teacherId + ".ser";
        File file = new File(filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("Черновик удалён: " + filename);
            }
            return deleted;
        }
        return true;
    }

    // Проверить, есть ли черновик
    public boolean hasDraft(int teacherId) {
        String filename = DRAFT_DIR + "/draft_" + teacherId + ".ser";
        File file = new File(filename);
        return file.exists();
    }
}
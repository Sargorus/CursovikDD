package main.java.com.psychotest.dao;

import main.java.com.psychotest.model.*;
import main.java.com.psychotest.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class TestDAO {

    // ========== СОХРАНЕНИЕ ==========

    /**
     * Сохраняет тест в базу данных
     * @param state состояние теста
     * @param teacherId ID преподавателя-создателя
     * @return ID сохранённого теста
     * @throws SQLException при ошибке БД
     */
    public int save(TestState state, int teacherId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Сохраняем тест
            int testId = saveTest(conn, state, teacherId);
            System.out.println("Сохранён тест: ID=" + testId + ", name=" + state.getTestName());

            // 2. Сохраняем параметры
            Map<String, Integer> paramIdMap = new HashMap<>();
            for (Parameter param : state.getParameters()) {
                int paramId = saveParameter(conn, testId, param);
                paramIdMap.put(param.getName(), paramId);
            }

            // 3. Сохраняем вопросы и ответы
            for (Question question : state.getQuestions()) {
                int questionId = saveQuestion(conn, testId, question);
                for (AnswerOption option : question.getAnswerOptions()) {
                    int optionId = saveAnswerOption(conn, questionId, option);
                    saveImpacts(conn, optionId, option.getParameterImpacts(), state.getParameters());
                }
            }

            conn.commit();
            System.out.println("Тест успешно сохранён в БД!");
            return testId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Транзакция откачена: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int saveTest(Connection conn, TestState state, int teacherId) throws SQLException {
        String sql = "INSERT INTO tests (name, description, created_by, questions_per_session, created_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, state.getTestName());
            pstmt.setString(2, state.getTestDescription());
            pstmt.setInt(3, teacherId);
            pstmt.setInt(4, state.getQuestionsPerSession());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    private int saveParameter(Connection conn, int testId, Parameter param) throws SQLException {
        String sql = "INSERT INTO parameters (test_id, name, scale_type, min_value, max_value) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            pstmt.setString(2, param.getName());
            pstmt.setString(3, param.getScaleType());
            pstmt.setInt(4, param.getMinValue());
            pstmt.setInt(5, param.getMaxValue());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int paramId = rs.getInt(1);

            // Сохраняем интерпретации
            saveInterpretations(conn, paramId, param.getInterpretations(), param.getScaleType());
            return paramId;
        }
    }

    private void saveInterpretations(Connection conn, int paramId, List<ParameterInterpretation> interpretations, String scaleType) throws SQLException {
        String sql = "INSERT INTO parameter_interpretations (parameter_id, range_start, range_end, binary_value, interpretation_text) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (ParameterInterpretation interp : interpretations) {
                pstmt.setInt(1, paramId);
                if (scaleType.equals("RANGE")) {
                    pstmt.setObject(2, interp.getRangeStart());
                    pstmt.setObject(3, interp.getRangeEnd());
                    pstmt.setNull(4, Types.VARCHAR);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                    pstmt.setNull(3, Types.INTEGER);
                    pstmt.setString(4, interp.getBinaryValue());
                }
                pstmt.setString(5, interp.getInterpretationText());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private int saveQuestion(Connection conn, int testId, Question question) throws SQLException {
        String sql = "INSERT INTO questions (test_id, text, order_num) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            pstmt.setString(2, question.getText());
            pstmt.setInt(3, question.getOrderNum());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    private int saveAnswerOption(Connection conn, int questionId, AnswerOption option) throws SQLException {
        String sql = "INSERT INTO answer_options (question_id, text, order_num) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            pstmt.setString(2, option.getText());
            pstmt.setInt(3, option.getOrderNum());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    private void saveImpacts(Connection conn, int optionId, Map<Integer, Integer> impacts, List<Parameter> parameters) throws SQLException {
        String sql = "INSERT INTO answer_parameter_impact (answer_option_id, parameter_id, delta) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, Integer> entry : impacts.entrySet()) {
                int paramIndex = entry.getKey();
                int delta = entry.getValue();
                if (paramIndex >= 0 && paramIndex < parameters.size()) {
                    // Нужно получить реальный ID параметра из БД
                    // В текущей реализации мы не храним маппинг индексов, поэтому пока пропускаем
                    // TODO: нужен маппинг индекс -> реальный ID параметра
                    System.out.println("  Влияние: параметр индекс=" + paramIndex + ", delta=" + delta);
                }
            }
        }
    }

    // ========== ЗАГРУЗКА ==========

    /**
     * Загружает тест по ID
     * @param testId ID теста
     * @return объект Test или null
     */
    public Test findById(int testId) throws SQLException {
        String sql = "SELECT * FROM tests WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Test test = new Test();
                test.setId(rs.getInt("id"));
                test.setName(rs.getString("name"));
                test.setDescription(rs.getString("description"));
                test.setCreatedBy(rs.getInt("created_by"));
                test.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                test.setQuestionsPerSession(rs.getInt("questions_per_session"));
                return test;
            }
        }
        return null;
    }

    /**
     * Загружает полное состояние теста (с параметрами, вопросами, ответами)
     */
    public TestState loadFullTestState(int testId) throws SQLException {
        TestState state = new TestState();

        // 1. Загружаем основную информацию
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String testSql = "SELECT name, description, questions_per_session FROM tests WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(testSql)) {
                pstmt.setInt(1, testId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    state.setTestName(rs.getString("name"));
                    state.setTestDescription(rs.getString("description"));
                    state.setQuestionsPerSession(rs.getInt("questions_per_session"));
                }
            }

            // 2. Загружаем параметры
            String paramSql = "SELECT * FROM parameters WHERE test_id = ? ORDER BY id";
            List<Parameter> parameters = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(paramSql)) {
                pstmt.setInt(1, testId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Parameter param = new Parameter();
                    param.setId(rs.getInt("id"));
                    param.setTestId(rs.getInt("test_id"));
                    param.setName(rs.getString("name"));
                    param.setScaleType(rs.getString("scale_type"));
                    param.setMinValue(rs.getInt("min_value"));
                    param.setMaxValue(rs.getInt("max_value"));

                    // Загружаем интерпретации
                    param.setInterpretations(loadInterpretations(conn, param.getId(), param.getScaleType()));
                    parameters.add(param);
                }
            }
            state.setParameters(parameters);

            // 3. Загружаем вопросы, ответы и влияния
            List<Question> questions = new ArrayList<>();
            String questionSql = "SELECT * FROM questions WHERE test_id = ? ORDER BY order_num";
            try (PreparedStatement pstmt = conn.prepareStatement(questionSql)) {
                pstmt.setInt(1, testId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Question question = new Question();
                    question.setId(rs.getInt("id"));
                    question.setTestId(rs.getInt("test_id"));
                    question.setText(rs.getString("text"));
                    question.setOrderNum(rs.getInt("order_num"));

                    // Загружаем ответы
                    question.setAnswerOptions(loadAnswerOptions(conn, question.getId(), parameters));
                    questions.add(question);
                }
            }
            state.setQuestions(questions);
        }

        return state;
    }

    private List<ParameterInterpretation> loadInterpretations(Connection conn, int paramId, String scaleType) throws SQLException {
        List<ParameterInterpretation> interpretations = new ArrayList<>();
        String sql = "SELECT * FROM parameter_interpretations WHERE parameter_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paramId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ParameterInterpretation interp = new ParameterInterpretation();
                interp.setId(rs.getInt("id"));
                interp.setParameterId(rs.getInt("parameter_id"));
                if (scaleType.equals("RANGE")) {
                    interp.setRangeStart(rs.getInt("range_start"));
                    interp.setRangeEnd(rs.getInt("range_end"));
                } else {
                    interp.setBinaryValue(rs.getString("binary_value"));
                }
                interp.setInterpretationText(rs.getString("interpretation_text"));
                interpretations.add(interp);
            }
        }
        return interpretations;
    }

    private List<AnswerOption> loadAnswerOptions(Connection conn, int questionId, List<Parameter> parameters) throws SQLException {
        List<AnswerOption> options = new ArrayList<>();
        String sql = "SELECT * FROM answer_options WHERE question_id = ? ORDER BY order_num";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AnswerOption option = new AnswerOption();
                option.setId(rs.getInt("id"));
                option.setQuestionId(rs.getInt("question_id"));
                option.setText(rs.getString("text"));
                option.setOrderNum(rs.getInt("order_num"));

                // Загружаем влияния
                Map<Integer, Integer> impacts = loadImpacts(conn, option.getId(), parameters);
                option.setParameterImpacts(impacts);
                options.add(option);
            }
        }
        return options;
    }

    private Map<Integer, Integer> loadImpacts(Connection conn, int optionId, List<Parameter> parameters) throws SQLException {
        Map<Integer, Integer> impacts = new HashMap<>();
        String sql = "SELECT parameter_id, delta FROM answer_parameter_impact WHERE answer_option_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, optionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int paramId = rs.getInt("parameter_id");
                int delta = rs.getInt("delta");
                // Находим индекс параметра по его ID
                for (int i = 0; i < parameters.size(); i++) {
                    if (parameters.get(i).getId() == paramId) {
                        impacts.put(i, delta);
                        break;
                    }
                }
            }
        }
        return impacts;
    }

    // ========== ОБНОВЛЕНИЕ ==========

    public boolean update(Test test) throws SQLException {
        String sql = "UPDATE tests SET name = ?, description = ?, questions_per_session = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, test.getName());
            pstmt.setString(2, test.getDescription());
            pstmt.setInt(3, test.getQuestionsPerSession());
            pstmt.setInt(4, test.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // ========== УДАЛЕНИЕ ==========

    public boolean delete(int testId) throws SQLException {
        String sql = "DELETE FROM tests WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ========== ПОИСК И СПИСКИ ==========

    public List<Test> findByTeacher(int teacherId) throws SQLException {
        List<Test> tests = new ArrayList<>();
        String sql = "SELECT * FROM tests WHERE created_by = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Test test = new Test();
                test.setId(rs.getInt("id"));
                test.setName(rs.getString("name"));
                test.setDescription(rs.getString("description"));
                test.setCreatedBy(rs.getInt("created_by"));
                test.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                test.setQuestionsPerSession(rs.getInt("questions_per_session"));
                tests.add(test);
            }
        }
        return tests;
    }

    public List<Test> findAll() throws SQLException {
        List<Test> tests = new ArrayList<>();
        String sql = "SELECT * FROM tests ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Test test = new Test();
                test.setId(rs.getInt("id"));
                test.setName(rs.getString("name"));
                test.setDescription(rs.getString("description"));
                test.setCreatedBy(rs.getInt("created_by"));
                test.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                test.setQuestionsPerSession(rs.getInt("questions_per_session"));
                tests.add(test);
            }
        }
        return tests;
    }

    // ========== НАЗНАЧЕНИЯ ==========

    public boolean assignToUser(int testId, int assignedBy, int userId, java.util.Date dueDate) throws SQLException {
        String sql = "INSERT INTO test_assignments (test_id, assigned_by, assigned_to_user, due_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            pstmt.setInt(2, assignedBy);
            pstmt.setInt(3, userId);
            if (dueDate != null) {
                pstmt.setDate(4, new java.sql.Date(dueDate.getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean assignToGroup(int testId, int assignedBy, int groupId, java.util.Date dueDate) throws SQLException {
        String sql = "INSERT INTO test_assignments (test_id, assigned_by, assigned_to_group, due_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            pstmt.setInt(2, assignedBy);
            pstmt.setInt(3, groupId);
            pstmt.setDate(4, new java.sql.Date(dueDate.getTime()));
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Integer> getAvailableTestsForUser(int userId) throws SQLException {
        List<Integer> testIds = new ArrayList<>();
        String sql = "SELECT DISTINCT a.test_id FROM test_assignments a " +
                "LEFT JOIN user_groups ug ON a.assigned_to_group = ug.group_id AND ug.user_id = ? " +
                "WHERE a.assigned_to_user = ? OR ug.user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                testIds.add(rs.getInt(1));
            }
        }
        return testIds;
    }

    /**
     * Загружает все вопросы для теста с их ответами
     */
    public List<Question> loadQuestionsForTest(int testId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE test_id = ? ORDER BY order_num";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setTestId(rs.getInt("test_id"));
                question.setText(rs.getString("text"));
                question.setOrderNum(rs.getInt("order_num"));

                // Загружаем ответы для вопроса
                question.setAnswerOptions(loadAnswerOptionsForQuestion(question.getId()));
                questions.add(question);
            }
        }
        return questions;
    }

    /**
     * Загружает варианты ответов для вопроса
     */
    private List<AnswerOption> loadAnswerOptionsForQuestion(int questionId) throws SQLException {
        List<AnswerOption> options = new ArrayList<>();
        String sql = "SELECT * FROM answer_options WHERE question_id = ? ORDER BY order_num";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                AnswerOption option = new AnswerOption();
                option.setId(rs.getInt("id"));
                option.setQuestionId(rs.getInt("question_id"));
                option.setText(rs.getString("text"));
                option.setOrderNum(rs.getInt("order_num"));

                // Загружаем влияния ответа на параметры
                option.setParameterImpacts(loadImpactsForOption(option.getId()));
                options.add(option);
            }
        }
        return options;
    }

    /**
     * Загружает влияния ответа на параметры
     */
    private Map<Integer, Integer> loadImpactsForOption(int optionId) throws SQLException {
        Map<Integer, Integer> impacts = new HashMap<>();
        String sql = "SELECT parameter_id, delta FROM answer_parameter_impact WHERE answer_option_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, optionId);
            ResultSet rs = pstmt.executeQuery();

            // Нам нужен индекс параметра, а не ID
            // Пока сохраняем как есть, потом преобразуем
            while (rs.next()) {
                impacts.put(rs.getInt("parameter_id"), rs.getInt("delta"));
            }
        }
        return impacts;
    }

    /**
     * Получить количество назначенных тестов для пользователя
     */
    public int getAssignedTestCountForUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_assignments WHERE assigned_to_user = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Получить количество пройденных тестов для пользователя
     */
    public int getCompletedTestCountForUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_sessions WHERE user_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Получить ID назначенных тестов для пользователя
     */
    public List<Integer> getAssignedTestIdsForUser(int userId) throws SQLException {
        List<Integer> testIds = new ArrayList<>();
        String sql = "SELECT test_id FROM test_assignments WHERE assigned_to_user = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                testIds.add(rs.getInt(1));
            }
        }
        return testIds;
    }

    /**
     * Удалить назначение теста пользователю
     */
    public boolean unassignTestFromUser(int testId, int userId) throws SQLException {
        String sql = "DELETE FROM test_assignments WHERE test_id = ? AND assigned_to_user = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
}

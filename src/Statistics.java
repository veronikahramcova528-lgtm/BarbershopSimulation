import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

@SuppressWarnings({"ClassCanBeRecord", "SameParameterValue", "ExtractMethodRecommender"})
final class Statistics {
    private final Barbershop shop;
    private final int totalMinutes;

    Statistics(Barbershop shop, int totalMinutes) {
        this.shop = shop;
        this.totalMinutes = totalMinutes;
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Статистика парикмахерской - " + shop.name);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Общая статистика", createGeneralPanel());
        tabbedPane.addTab("Парикмахеры", createBarbersPanel());
        tabbedPane.addTab("Клиенты", createClientsPanel());
        tabbedPane.addTab("Выручка по услугам", createRevenuePanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private Font getRegularFont() {
        return new Font("Arial", Font.PLAIN, 12);
    }

    private Font getBoldFont() {
        return new Font("Arial", Font.BOLD, 12);
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int beginnerRevenue = 0;
        int experiencedRevenue = 0;
        int masterRevenue = 0;
        int beginnerIdle = 0;
        int experiencedIdle = 0;
        int masterIdle = 0;
        int beginnerCount = 0;
        int experiencedCount = 0;
        int masterCount = 0;

        for (int i = 0; i < shop.barbers.size(); i++) {
            Barber b = shop.barbers.get(i);
            if (b.level == 1) {
                beginnerRevenue = beginnerRevenue + b.totalRevenue;
                beginnerIdle = beginnerIdle + b.totalIdleTime;
                beginnerCount = beginnerCount + 1;
            } else if (b.level == 2) {
                experiencedRevenue = experiencedRevenue + b.totalRevenue;
                experiencedIdle = experiencedIdle + b.totalIdleTime;
                experiencedCount = experiencedCount + 1;
            } else if (b.level == 3) {
                masterRevenue = masterRevenue + b.totalRevenue;
                masterIdle = masterIdle + b.totalIdleTime;
                masterCount = masterCount + 1;
            }
        }

        int totalWaitTime = 0;
        for (int i = 0; i < shop.servedClients.size(); i++) {
            Client c = shop.servedClients.get(i);
            totalWaitTime = totalWaitTime + c.waitTime;
        }

        double avgWait = 0;
        if (!shop.servedClients.isEmpty()) {
            avgWait = (double) totalWaitTime / shop.servedClients.size();
        }

        int regularLeft = 0;
        int randomLeft = 0;
        for (int i = 0; i < shop.leftClients.size(); i++) {
            Client c = shop.leftClients.get(i);
            if (c.isRegular) {
                regularLeft = regularLeft + 1;
            } else {
                randomLeft = randomLeft + 1;
            }
        }

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(2, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Ключевые показатели"));

        statsPanel.add(createStatCard("Общая выручка", shop.totalRevenue + " руб"));
        statsPanel.add(createStatCard("Обслужено клиентов", String.valueOf(shop.servedClients.size())));
        statsPanel.add(createStatCard("Ушло клиентов", shop.leftClients.size() + ""));
        statsPanel.add(createStatCard("Постоянных ушло", regularLeft + ""));
        statsPanel.add(createStatCard("Случайных ушло", randomLeft + ""));
        statsPanel.add(createStatCard("Ср. время ожидания", String.format("%.1f мин", avgWait)));

        String[][] revenueData = new String[3][4];
        revenueData[0][0] = "Начинающие";
        revenueData[0][1] = beginnerCount + "";
        revenueData[0][2] = beginnerRevenue + " руб";
        revenueData[0][3] = formatPercent(beginnerIdle, beginnerCount);

        revenueData[1][0] = "Опытные";
        revenueData[1][1] = experiencedCount + "";
        revenueData[1][2] = experiencedRevenue + " руб";
        revenueData[1][3] = formatPercent(experiencedIdle, experiencedCount);

        revenueData[2][0] = "Мастера";
        revenueData[2][1] = masterCount + "";
        revenueData[2][2] = masterRevenue + " руб";
        revenueData[2][3] = formatPercent(masterIdle, masterCount);

        String[] revenueColumns = {"Уровень", "Кол-во", "Выручка", "Простой"};
        JTable revenueTable = new JTable(new StatisticsTableModel(revenueData, revenueColumns));
        revenueTable.setFont(getRegularFont());
        revenueTable.setRowHeight(25);
        revenueTable.getTableHeader().setFont(getBoldFont());

        JScrollPane revenueScroll = new JScrollPane(revenueTable);
        revenueScroll.setBorder(BorderFactory.createTitledBorder("Статистика по уровням"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 2, 10, 10));
        bottomPanel.add(revenueScroll);

        int[] valuesForChart = new int[3];
        valuesForChart[0] = beginnerRevenue;
        valuesForChart[1] = experiencedRevenue;
        valuesForChart[2] = masterRevenue;
        bottomPanel.add(createPieChart(valuesForChart));

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPieChart(int[] values) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Выручка по уровням"));

        int total = values[0] + values[1] + values[2];

        if (total == 0) {
            JLabel label = new JLabel("Нет данных", SwingConstants.CENTER);
            panel.add(label, BorderLayout.CENTER);
            return panel;
        }

        final int totalFinal = total;
        final int[] valuesFinal = values.clone();

        JPanel chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth() - 60, getHeight() - 60);
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                Color[] colors = {new Color(70, 130, 180), new Color(60, 179, 113), new Color(218, 165, 32)};

                int startAngle = 0;
                for (int i = 0; i < 3; i++) {
                    if (valuesFinal[i] > 0) {
                        int angle = (int) (360.0 * valuesFinal[i] / totalFinal);
                        g2d.setColor(colors[i]);
                        g2d.fillArc(x, y, size, size, startAngle, angle);
                        startAngle = startAngle + angle;
                    }
                }
                g2d.setColor(Color.BLACK);
                g2d.drawOval(x, y, size, size);
            }
        };
        chartPanel.setPreferredSize(new Dimension(250, 250));
        chartPanel.setBackground(Color.WHITE);

        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new GridLayout(3, 1, 5, 5));
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] labels = {"Начинающие", "Опытные", "Мастера"};
        Color[] colors = {new Color(70, 130, 180), new Color(60, 179, 113), new Color(218, 165, 32)};

        for (int i = 0; i < 3; i++) {
            if (values[i] > 0) {
                JPanel legendItem = new JPanel();
                legendItem.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

                JPanel colorBox = new JPanel();
                colorBox.setBackground(colors[i]);
                colorBox.setPreferredSize(new Dimension(15, 15));

                double percent = (double) values[i] / total * 100;
                int percentInt = (int) (percent * 10);
                int whole = percentInt / 10;
                int fraction = percentInt % 10;
                if (fraction < 0) {
                    fraction = -fraction;
                }

                JLabel label = new JLabel();
                label.setText(labels[i] + ": " + values[i] + " руб (" + whole + "." + fraction + "%)");
                label.setFont(getRegularFont());

                legendItem.add(colorBox);
                legendItem.add(label);
                legendPanel.add(legendItem);
            }
        }

        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(legendPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(250, 250, 250));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(getRegularFont());

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBarbersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[][] barberData = new String[shop.barbers.size()][6];

        for (int i = 0; i < shop.barbers.size(); i++) {
            Barber b = shop.barbers.get(i);

            String level;
            if (b.level == 1) {
                level = "Начинающий";
            } else if (b.level == 2) {
                level = "Опытный";
            } else {
                level = "Мастер";
            }

            double avgWait = b.getAverageWaitTime();
            String avgWaitStr;
            int avgWaitInt = (int) (avgWait * 10);
            int whole = avgWaitInt / 10;
            int fraction = avgWaitInt % 10;
            if (fraction < 0) {
                fraction = -fraction;
            }
            avgWaitStr = whole + "." + fraction + " мин";

            barberData[i][0] = String.valueOf(b.id);
            barberData[i][1] = b.name;
            barberData[i][2] = level;
            barberData[i][3] = b.totalRevenue + " руб";
            barberData[i][4] = b.totalIdleTime + " мин";
            barberData[i][5] = avgWaitStr;
        }

        String[] barberColumns = {"ID", "Имя", "Уровень", "Выручка", "Простой", "Ср. ожидание"};
        JTable barberTable = new JTable(new StatisticsTableModel(barberData, barberColumns));
        barberTable.setFont(getRegularFont());
        barberTable.setRowHeight(25);
        barberTable.getTableHeader().setFont(getBoldFont());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < barberTable.getColumnCount(); i++) {
            barberTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(barberTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список парикмахеров"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[][] servedData = new String[shop.servedClients.size()][5];
        for (int i = 0; i < shop.servedClients.size(); i++) {
            Client c = shop.servedClients.get(i);
            String type;
            if (c.isRegular) {
                type = "Постоянный";
            } else {
                type = "Случайный";
            }
            servedData[i][0] = String.valueOf(c.id);
            servedData[i][1] = c.name;
            servedData[i][2] = type;
            servedData[i][3] = c.wantedService.name;
            servedData[i][4] = c.waitTime + " мин";
        }

        String[] clientColumns = {"ID", "Имя", "Тип", "Услуга", "Ожидание"};
        JTable servedTable = new JTable(new StatisticsTableModel(servedData, clientColumns));
        servedTable.setFont(getRegularFont());
        servedTable.setRowHeight(25);
        servedTable.getTableHeader().setFont(getBoldFont());

        String[][] leftData = new String[shop.leftClients.size()][5];
        for (int i = 0; i < shop.leftClients.size(); i++) {
            Client c = shop.leftClients.get(i);
            String type;
            if (c.isRegular) {
                type = "Постоянный";
            } else {
                type = "Случайный";
            }
            leftData[i][0] = String.valueOf(c.id);
            leftData[i][1] = c.name;
            leftData[i][2] = type;
            leftData[i][3] = c.wantedService.name;
            leftData[i][4] = c.waitTime + " мин";
        }

        JTable leftTable = new JTable(new StatisticsTableModel(leftData, clientColumns));
        leftTable.setFont(getRegularFont());
        leftTable.setRowHeight(25);
        leftTable.getTableHeader().setFont(getBoldFont());

        JScrollPane servedScroll = new JScrollPane(servedTable);
        servedScroll.setBorder(BorderFactory.createTitledBorder("Обслуженные клиенты (" + shop.servedClients.size() + ")"));

        JScrollPane leftScroll = new JScrollPane(leftTable);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Ушедшие клиенты (" + shop.leftClients.size() + ")"));

        panel.add(servedScroll);
        panel.add(leftScroll);

        return panel;
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] serviceNames = {"Базовая", "Обычная", "Сложная", "Нестандартная"};

        String[][] revenueData = new String[4][2];
        int totalRevenue = 0;
        for (int i = 1; i <= 4; i++) {
            totalRevenue = totalRevenue + shop.revenueByCategory[i];
        }

        for (int i = 0; i < 4; i++) {
            revenueData[i][0] = serviceNames[i];
            revenueData[i][1] = shop.revenueByCategory[i + 1] + " руб";
        }

        String[] revenueColumns = {"Услуга", "Выручка"};
        JTable revenueTable = new JTable(new StatisticsTableModel(revenueData, revenueColumns));
        revenueTable.setFont(getRegularFont());
        revenueTable.setRowHeight(30);
        revenueTable.getTableHeader().setFont(getBoldFont());

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        revenueTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        JScrollPane tableScroll = new JScrollPane(revenueTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Выручка по типам услуг"));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Аналитика"));

        int maxRevenue = 0;
        String bestService = "";
        for (int i = 0; i < 4; i++) {
            if (shop.revenueByCategory[i + 1] > maxRevenue) {
                maxRevenue = shop.revenueByCategory[i + 1];
                bestService = serviceNames[i];
            }
        }

        JLabel totalLabel = new JLabel("Общая выручка: " + totalRevenue + " руб");
        totalLabel.setFont(getRegularFont());

        JLabel bestLabel = new JLabel("Самая прибыльная: " + bestService);
        bestLabel.setFont(getRegularFont());

        JLabel revenueLabel = new JLabel("Выручка от неё: " + maxRevenue + " руб");
        revenueLabel.setFont(getRegularFont());

        double percent = 0;
        if (totalRevenue > 0) {
            percent = (double) maxRevenue / totalRevenue * 100;
        }
        int percentInt = (int) (percent * 10);
        int whole = percentInt / 10;
        int fraction = percentInt % 10;
        if (fraction < 0) {
            fraction = -fraction;
        }

        JLabel percentLabel = new JLabel("Доля в общей: " + whole + "." + fraction + "%");
        percentLabel.setFont(getRegularFont());

        infoPanel.add(totalLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(bestLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(revenueLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(percentLabel);

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.EAST);

        return panel;
    }

    private String formatPercent(int idleTime, int count) {
        if (count == 0) {
            return "0 мин (0.0%)";
        }
        double percent = (double) idleTime / (count * totalMinutes) * 100;
        int percentInt = (int) (percent * 10);
        int whole = percentInt / 10;
        int fraction = percentInt % 10;
        if (fraction < 0) {
            fraction = -fraction;
        }
        return idleTime + " мин (" + whole + "." + fraction + "%)";
    }

    static class StatisticsTableModel extends AbstractTableModel {
        private final String[][] data;
        private final String[] columns;

        public StatisticsTableModel(String[][] data, String[] columns) {
            this.data = data;
            this.columns = columns;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }
    }
}
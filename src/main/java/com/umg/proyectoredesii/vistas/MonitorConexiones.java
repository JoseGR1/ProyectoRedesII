package com.umg.proyectoredesii.vistas;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;

public class MonitorConexiones extends JPanel {
    private boolean conexion1 = false;
    private boolean conexion2 = false;
    private String ip1 = "0.0.0.0";
    private String ip2 = "0.0.0.0";

    private JTextField ipField1, ipField2;
    private JLabel estadoLabel1, estadoLabel2;
    private ImageIcon pcIcon;

    private DefaultListModel<String> listaIpsModel;
    private JList<String> listaIps;
    private JButton btnAsignarA1, btnAsignarA2;

    public MonitorConexiones() {
        // Cargar imagen de PC
        pcIcon = new ImageIcon("C:/Users/jose5/Documents/NetBeansProjects/ProyectoRedesII/src/main/java/img/pc.png");
        Image img = pcIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        pcIcon = new ImageIcon(img);

        setLayout(new BorderLayout());

        // ================= PANEL SUPERIOR =================
        JPanel topPanel = new JPanel(new FlowLayout());
        ipField1 = new JTextField(ip1, 12);
        ipField2 = new JTextField(ip2, 12);
        JButton btnMonitorear = new JButton("Monitorear");

        topPanel.add(new JLabel("IP 1:"));
        topPanel.add(ipField1);
        estadoLabel1 = new JLabel("Estado: ?");
        topPanel.add(estadoLabel1);

        topPanel.add(new JLabel("   IP 2:"));
        topPanel.add(ipField2);
        estadoLabel2 = new JLabel("Estado: ?");
        topPanel.add(estadoLabel2);

        topPanel.add(btnMonitorear);
        add(topPanel, BorderLayout.NORTH);

        // ================= PANEL DE DIBUJO =================
        JPanel dibujoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                pcIcon.paintIcon(this, g, 50, 100);   // PC1
                pcIcon.paintIcon(this, g, 370, 100);  // PC2

                g.setColor(Color.BLACK);
                g.drawString("PC1", 80, 190);
                g.drawString("PC2", 400, 190);

                // Línea para IP1
                g.setColor(conexion1 ? Color.GREEN : Color.RED);
                g.drawLine(130, 120, 370, 120);
                g.setColor(Color.BLACK);
                g.drawString(ip1, 230, 110);

                // Línea para IP2
                g.setColor(conexion2 ? Color.GREEN : Color.RED);
                g.drawLine(130, 170, 370, 170);
                g.setColor(Color.BLACK);
                g.drawString(ip2, 230, 190);
            }
        };
        add(dibujoPanel, BorderLayout.CENTER);

        // ================= PANEL DERECHO - ESCANEO =================
        JPanel lateralPanel = new JPanel(new BorderLayout());
        JButton btnEscanear = new JButton("Escanear Red");
        listaIpsModel = new DefaultListModel<>();
        listaIps = new JList<>(listaIpsModel);
        JScrollPane scrollPane = new JScrollPane(listaIps);

        JPanel botonesAsignar = new JPanel(new GridLayout(2, 1));
        btnAsignarA1 = new JButton("Asignar a IP1");
        btnAsignarA2 = new JButton("Asignar a IP2");
        botonesAsignar.add(btnAsignarA1);
        botonesAsignar.add(btnAsignarA2);

        lateralPanel.add(btnEscanear, BorderLayout.NORTH);
        lateralPanel.add(scrollPane, BorderLayout.CENTER);
        lateralPanel.add(botonesAsignar, BorderLayout.SOUTH);
        add(lateralPanel, BorderLayout.EAST);

        // ================= ACCIONES =================

        btnMonitorear.addActionListener(e -> {
            ip1 = ipField1.getText().trim();
            ip2 = ipField2.getText().trim();

            new Timer(3000, ev -> {
                conexion1 = verificarConexion(ip1);
                conexion2 = verificarConexion(ip2);

                estadoLabel1.setText("Estado: " + (conexion1 ? "Conectado" : "Desconectado"));
                estadoLabel2.setText("Estado: " + (conexion2 ? "Conectado" : "Desconectado"));
                dibujoPanel.repaint();
            }).start();
        });

        btnEscanear.addActionListener(e -> {
            try {
                String ipLocal = InetAddress.getLocalHost().getHostAddress(); // Ej: 192.168.0.12
                String ipBase = ipLocal.substring(0, ipLocal.lastIndexOf('.') + 1); // Queda: 192.168.0.
                escanearRed(ipBase);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo detectar IP local.");
            }
        });
        btnAsignarA1.addActionListener(e -> {
            String seleccionada = listaIps.getSelectedValue();
            if (seleccionada != null) {
                String ipSeleccionada = seleccionada.split(" ")[0]; // Tomar solo la IP
                ipField1.setText(ipSeleccionada);
            }
        });

        btnAsignarA2.addActionListener(e -> {
            String seleccionada = listaIps.getSelectedValue();
            if (seleccionada != null) {
                String ipSeleccionada = seleccionada.split(" ")[0];
                ipField2.setText(ipSeleccionada);
            }
        });
    }

    private boolean verificarConexion(String ip) {
        try {
            return InetAddress.getByName(ip).isReachable(1000);
        } catch (IOException e) {
            return false;
        }
    }

private void escanearRed(String ipBase) {
    listaIpsModel.clear();

    // Spinner/Loader modal
    JDialog loadingDialog = new JDialog((Frame) null, "Escaneando red...", true);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel("Buscando dispositivos en la red..."), BorderLayout.NORTH);

    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    panel.add(progressBar, BorderLayout.CENTER);

    loadingDialog.getContentPane().add(panel);
    loadingDialog.setSize(300, 100);
    loadingDialog.setLocationRelativeTo(null);

    new Thread(() -> {
        for (int i = 1; i <= 254; i++) {
            String ip = ipBase + i;
            try {
                InetAddress address = InetAddress.getByName(ip);
                if (address.isReachable(300)) {
                    String hostname = address.getCanonicalHostName(); // Obtener el nombre del dispositivo
                    String resultado = ip + " - " + hostname;
                    SwingUtilities.invokeLater(() -> listaIpsModel.addElement(resultado));
                }
            } catch (IOException ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            loadingDialog.dispose();
            JOptionPane.showMessageDialog(this, "Escaneo finalizado.");
        });
    }).start();

    // Mostrar la ventana mientras escanea
    loadingDialog.setVisible(true);
}


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Error al aplicar FlatLaf.");
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Monitor de Conexiones - Doble Red con Escáner");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 450);
            frame.setContentPane(new MonitorConexiones());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

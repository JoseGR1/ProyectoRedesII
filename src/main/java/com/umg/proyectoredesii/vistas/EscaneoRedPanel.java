package com.umg.proyectoredesii.vistas;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class EscaneoRedPanel extends JPanel {
    private DefaultListModel<String> listModel;
    private JList<String> ipList;
    private JButton btnEscanear, btnSeleccionar;
    private JTextField rangoInicioField, rangoFinField;

    public EscaneoRedPanel() {
        setLayout(new BorderLayout());

        // Panel de control para especificar rango de IP
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("Rango Inicio (último octeto):"));
        rangoInicioField = new JTextField("1", 3);
        controlPanel.add(rangoInicioField);
        controlPanel.add(new JLabel("Rango Fin (último octeto):"));
        rangoFinField = new JTextField("254", 3);
        controlPanel.add(rangoFinField);
        btnEscanear = new JButton("Escanear Red");
        controlPanel.add(btnEscanear);
        add(controlPanel, BorderLayout.NORTH);

        // Lista para mostrar las IPs encontradas
        listModel = new DefaultListModel<>();
        ipList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(ipList);
        add(scrollPane, BorderLayout.CENTER);

        btnSeleccionar = new JButton("Seleccionar IP para Monitoreo");
        add(btnSeleccionar, BorderLayout.SOUTH);

        // Acción para iniciar el escaneo
        btnEscanear.addActionListener(e -> iniciarEscaneo());

        // Acción para seleccionar una IP (puedes integrarla en tu monitor)
        btnSeleccionar.addActionListener(e -> {
            String ipSeleccionada = ipList.getSelectedValue();
            if (ipSeleccionada != null) {
                JOptionPane.showMessageDialog(this, "IP seleccionada: " + ipSeleccionada);
                // Aquí puedes lanzar el monitoreo de la IP seleccionada o guardar el valor para usarlo en tu panel de monitoreo.
            }
        });
    }

    private void iniciarEscaneo() {
        listModel.clear();
        String ipBase = "192.168.1."; // Ajusta esta base de IP según tu red

        int inicio, fin;
        try {
            inicio = Integer.parseInt(rangoInicioField.getText().trim());
            fin = Integer.parseInt(rangoFinField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Rangos inválidos");
            return;
        }

        // SwingWorker para no congelar la UI
        SwingWorker<List<String>, String> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                List<String> ipsActivas = new ArrayList<>();
                for (int i = inicio; i <= fin; i++) {
                    String ip = ipBase + i;
                    try {
                        InetAddress address = InetAddress.getByName(ip);
                        if (address.isReachable(500)) {  // Timeout de 500 ms
                            ipsActivas.add(ip);
                            publish(ip);  // Publica a la lista para actualizar la UI
                        }
                    } catch (Exception ex) {
                        // Se ignoran excepciones en este ejemplo
                    }
                }
                return ipsActivas;
            }

            @Override
            protected void process(List<String> chunks) {
                // Se llaman conforme se encuentren IPs activas
                for (String ip : chunks) {
                    listModel.addElement(ip);
                }
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(EscaneoRedPanel.this, "Escaneo finalizado");
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        // Si usas FlatLaf, asegúrate de aplicarlo aquí
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Escaneo de Red");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setContentPane(new EscaneoRedPanel());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

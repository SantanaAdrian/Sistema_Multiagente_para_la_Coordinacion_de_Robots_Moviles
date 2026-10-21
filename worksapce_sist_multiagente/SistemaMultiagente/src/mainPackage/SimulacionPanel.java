package mainPackage;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class SimulacionPanel extends JFrame {

    public  static final int FILAS = 12;
    public  static final int COLS  = 18;
    private static final int CELDA = 50;

    private final Map<String, int[]> posiciones = new HashMap<>();
    private final Map<String, Color> colores    = new HashMap<>();
    private final Set<String>        bloqueados = new HashSet<>();
    private JPanel grid;

    private SimulacionPanel() {
        setTitle("Simulación Multiagente — FIN");
        setSize(COLS * CELDA + 20, FILAS * CELDA + 60);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(20, 20, 20));

        grid = new JPanel(new GridLayout(FILAS, COLS, 2, 2));
        grid.setBackground(new Color(15, 15, 15));
        grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        add(grid, BorderLayout.CENTER);
        setVisible(true);
    }

    public synchronized void registrarRobot(String nombre, Color color,
                                             int fila, int col) {
        colores.put(nombre, color);
        posiciones.put(nombre, new int[]{fila, col});
        SwingUtilities.invokeLater(this::dibujar);
    }

    public synchronized boolean intentarMover(String nombre, int nf, int nc) {
        if (bloqueados.contains(nombre)) return false;
        for (Map.Entry<String, int[]> e : posiciones.entrySet()) {
            if (!e.getKey().equals(nombre)) {
                int[] pos = e.getValue();
                if (pos[0] == nf && pos[1] == nc) return false;
            }
        }
        posiciones.put(nombre, new int[]{nf, nc});
        SwingUtilities.invokeLater(this::dibujar);
        return true;
    }

    public synchronized String quienEsta(int fila, int col) {
        for (Map.Entry<String, int[]> e : posiciones.entrySet()) {
            int[] pos = e.getValue();
            if (pos[0] == fila && pos[1] == col) return e.getKey();
        }
        return null;
    }

    public synchronized int[] getPosicion(String nombre) {
        int[] pos = posiciones.get(nombre);
        return pos != null ? new int[]{pos[0], pos[1]} : null;
    }

    public synchronized void bloquearRobot(String nombre) {
        if (!bloqueados.contains(nombre)) {
            bloqueados.add(nombre);
            System.out.println("🔒 " + nombre + " FIJO");
        }
    }

    public synchronized boolean esBloqueado(String nombre) {
        return nombre != null && bloqueados.contains(nombre);
    }

    private void dibujar() {
        grid.removeAll();
        Map<String, int[]> snap;
        synchronized (this) { snap = new HashMap<>(posiciones); }
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLS; c++) {
                JPanel celda = new JPanel();
                celda.setBackground(new Color(40, 40, 40));
                for (Map.Entry<String, int[]> e : snap.entrySet()) {
                    int[] pos = e.getValue();
                    if (pos[0] == f && pos[1] == c) {
                        celda.setBackground(
                            colores.getOrDefault(e.getKey(), Color.WHITE));
                        break;
                    }
                }
                grid.add(celda);
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private static SimulacionPanel instancia;
    public static synchronized SimulacionPanel getInstancia() {
        if (instancia == null) instancia = new SimulacionPanel();
        return instancia;
    }
}
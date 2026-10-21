package mainPackage;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Main {

    // {formFila, formCol, r, g, b}
    // La prioridad se calcula automáticamente por columna:
    // min(col, 13-col) + 1  →  extremos=1, hacia dentro suma 1
    public static final int[][] FORMACION = {
        // ── F rojo ──────────────────────────────────────
        {2,0, 210,60,60},   // col 0 → prior 1
        {2,1, 210,60,60},   // col 1 → prior 2
        {2,2, 210,60,60},   // col 2 → prior 3
        {3,0, 210,60,60},   // col 0 → prior 1
        {4,0, 210,60,60},   // col 0 → prior 1
        {4,1, 210,60,60},   // col 1 → prior 2
        {5,0, 210,60,60},   // col 0 → prior 1
        {6,0, 210,60,60},   // col 0 → prior 1
        // ── I blanco ────────────────────────────────────
        {2,5, 220,220,220}, // col 5 → prior 6
        {3,5, 220,220,220}, // col 5 → prior 6
        {4,5, 220,220,220}, // col 5 → prior 6
        {5,5, 220,220,220}, // col 5 → prior 6
        {6,5, 220,220,220}, // col 5 → prior 6
        // ── N verde ─────────────────────────────────────
        {2,9,  60,200,100}, // col 9  → prior 5
        {2,13, 60,200,100}, // col 13 → prior 1
        {3,9,  60,200,100}, // col 9  → prior 5
        {3,10, 60,200,100}, // col 10 → prior 4
        {3,13, 60,200,100}, // col 13 → prior 1
        {4,9,  60,200,100}, // col 9  → prior 5
        {4,11, 60,200,100}, // col 11 → prior 3
        {4,13, 60,200,100}, // col 13 → prior 1
        {5,9,  60,200,100}, // col 9  → prior 5
        {5,12, 60,200,100}, // col 12 → prior 2
        {5,13, 60,200,100}, // col 13 → prior 1
        {6,9,  60,200,100}, // col 9  → prior 5
        {6,13, 60,200,100}, // col 13 → prior 1
    };

    /** Calcula la prioridad por columna: extremos=1, hacia dentro +1 */
    public static int calcularPrioridad(int col) {
        return Math.min(col, 13 - col) + 1;
    }

    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        AgentContainer container = rt.createMainContainer(profile);

        SimulacionPanel.getInstancia();

        int total = FORMACION.length;

        for (int i = 0; i < total; i++) {
            int initFila = (i < 13) ? 0 : 11;
            int initCol  = i % 13;
            int[] f      = FORMACION[i];
            int   prior  = calcularPrioridad(f[1]); // prioridad por columna destino
            Object[] agentArgs = {
                initFila, initCol,
                f[0], f[1],
                f[2], f[3], f[4],
                prior
            };
            System.out.println("Robot" + (i+1)
                + " dest=(" + f[0] + "," + f[1] + ")"
                + " prior=" + prior);
            AgentController robot = container.createNewAgent(
                "Robot" + (i + 1), "mainPackage.RobotAgent", agentArgs);
            robot.start();
        }

        AgentController coord = container.createNewAgent(
            "Coordinador", "mainPackage.CoordinadorAgent",
            new Object[]{total});
        coord.start();

        System.out.println("✅ Sistema arrancado con " + total + " robots");
    }
}
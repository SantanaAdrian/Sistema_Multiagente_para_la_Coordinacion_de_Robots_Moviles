package mainPackage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import java.util.*;

public class RobotAgent extends Agent {

    private int filaActual, colActual;
    private int filaFormacion, colFormacion;
    private int prioridad;           // 1=máxima, nunca cede
    private boolean modoFormacion   = false;
    private boolean enPosicionFinal = false;
    private int     ticksBloqueado  = 0;
    private List<int[]>  ruta         = new ArrayList<>();
    private Set<String>  celdasEvitar = new HashSet<>();
    private final Random rand         = new Random();

    private static final int FILAS = SimulacionPanel.FILAS;
    private static final int COLS  = SimulacionPanel.COLS;
    private static final int TOTAL = 26;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        filaActual    = (int) args[0];
        colActual     = (int) args[1];
        filaFormacion = (int) args[2];
        colFormacion  = (int) args[3];
        Color color   = new Color((int)args[4], (int)args[5], (int)args[6]);
        prioridad     = (int) args[7];

        // Velocidad ligeramente distinta por robot para romper sincronía
        int velocidad = 350 + rand.nextInt(250);

        SimulacionPanel.getInstancia()
            .registrarRobot(getLocalName(), color, filaActual, colActual);
        System.out.println("🤖 " + getLocalName()
            + " prior=" + prioridad
            + " dest=(" + filaFormacion + "," + colFormacion + ")");

        // ── Comportamiento 1: recibir mensajes ─────────────────────────────
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content   = msg.getContent();
                    String remitente = msg.getSender().getLocalName();
                    int    perf      = msg.getPerformative();

                    if (perf == ACLMessage.REQUEST
                            && content.startsWith("FORMACION:")) {
                        // ── Orden del coordinador ──────────────────────
                        modoFormacion = true;
                        celdasEvitar.clear();
                        ruta = calcularRuta();
                        System.out.println("🎯 " + getLocalName()
                            + " prior=" + prioridad
                            + " → (" + filaFormacion + "," + colFormacion
                            + ") " + ruta.size() + " pasos");

                    } else if (perf == ACLMessage.REQUEST
                            && content.startsWith("CEDEPASO:")) {
                        // ── Otro robot pide que cedamos ────────────────
                        int prioSolic = Integer.parseInt(content.split(":")[1]);
                        boolean fijo  = SimulacionPanel.getInstancia()
                                            .esBloqueado(getLocalName());

                        if (!fijo && prioSolic < prioridad) {
                            // Cedo: el solicitante tiene más prioridad
                            ACLMessage resp = msg.createReply();
                            resp.setPerformative(ACLMessage.AGREE);
                            resp.setContent("ACEPTO:" + prioridad);
                            send(resp);

                            boolean estabaEnFinal = enPosicionFinal;
                            enPosicionFinal = false;
                            apartarme();

                            celdasEvitar.clear();
                            ruta = calcularRuta();

                            System.out.println("✅ " + getLocalName()
                                + " (p=" + prioridad + ") cede a "
                                + remitente + " (p=" + prioSolic + ")"
                                + (estabaEnFinal ? " [salió de posición]" : ""));

                        } else {
                            // No cedo
                            ACLMessage resp = msg.createReply();
                            resp.setPerformative(ACLMessage.REFUSE);
                            resp.setContent("RECHAZO:" + prioridad);
                            send(resp);
                            System.out.println("❌ " + getLocalName()
                                + " (p=" + prioridad + ") rechaza a "
                                + remitente + " (p=" + prioSolic + ")");
                        }

                    } else if (perf == ACLMessage.AGREE
                            && content.startsWith("ACEPTO:")) {
                        // ── El bloqueador acepta apartarse ─────────────
                        ticksBloqueado = 0;
                        System.out.println("🤝 " + getLocalName()
                            + " ACEPTO de " + remitente);

                    } else if (perf == ACLMessage.REFUSE
                            && content.startsWith("RECHAZO:")) {
                        // ── El bloqueador tiene más prioridad ──────────
                        // Acumular celda a evitar y recalcular ruta
                        if (modoFormacion && !ruta.isEmpty()) {
                            int[] cel = ruta.get(0);
                            celdasEvitar.add(cel[0] + "," + cel[1]);
                            ruta = calcularRuta();
                            // Si no hay ruta alternativa, limpiar y reintentar
                            if (ruta.isEmpty()) {
                                celdasEvitar.clear();
                                ruta = calcularRuta();
                            }
                            System.out.println("🔀 " + getLocalName()
                                + " rodeo por RECHAZO de " + remitente
                                + " (evitando " + celdasEvitar.size()
                                + " celdas)");
                        }
                    }
                } else {
                    block();
                }
            }
        });

        // ── Comportamiento 2: moverse ──────────────────────────────────────
        addBehaviour(new TickerBehaviour(this, velocidad) {
            @Override
            protected void onTick() {
                if (SimulacionPanel.getInstancia().esBloqueado(getLocalName()))
                    return;
                if (!modoFormacion) moverRandom();
                else                moverHaciaFormacion();
            }
        });
    }

    // ── Movimiento en fase formación ─────────────────────────────────────────
    private void moverHaciaFormacion() {
        if (filaActual == filaFormacion && colActual == colFormacion) {
            if (!enPosicionFinal) {
                enPosicionFinal = true;
                // Solo prioridad 1 se bloquea permanentemente
                if (prioridad == 1) {
                    SimulacionPanel.getInstancia()
                        .bloquearRobot(getLocalName());
                    System.out.println("🔒 " + getLocalName()
                        + " FIJO (prior=1)");
                } else {
                    System.out.println("📍 " + getLocalName()
                        + " en posición (p=" + prioridad + ", puede ceder)");
                }
            }
            return;
        }

        if (enPosicionFinal) enPosicionFinal = false;

        if (ruta.isEmpty()) ruta = calcularRuta();
        if (ruta.isEmpty()) return;

        int[] sig = ruta.get(0);

        if (SimulacionPanel.getInstancia()
                .intentarMover(getLocalName(), sig[0], sig[1])) {
            filaActual = sig[0];
            colActual  = sig[1];
            ruta.remove(0);
            ticksBloqueado = 0;
        } else {
            ticksBloqueado++;

            if (ticksBloqueado == 2) {
                // Negociar con el robot bloqueador
                String bloq = SimulacionPanel.getInstancia()
                    .quienEsta(sig[0], sig[1]);
                if (bloq != null && !SimulacionPanel.getInstancia()
                        .esBloqueado(bloq)) {
                    ACLMessage neg = new ACLMessage(ACLMessage.REQUEST);
                    neg.addReceiver(new AID(bloq, AID.ISLOCALNAME));
                    neg.setContent("CEDEPASO:" + prioridad);
                    send(neg);
                    System.out.println("📨 " + getLocalName()
                        + " (p=" + prioridad + ") negocia con " + bloq);
                }
            }

            if (ticksBloqueado >= 10) {
                // Timeout: reubicarse
                ticksBloqueado = 0;
                celdasEvitar.clear();
                apartarme();
                ruta = calcularRuta();
                System.out.println("🔄 " + getLocalName() + " reubicando...");
            }
        }
    }

    // ── Apartarse a la mejor celda libre adyacente ───────────────────────────
    private void apartarme() {
        int[][] dirs = {{0,-1},{-1,0},{0,1},{1,0},{-1,-1},{-1,1},{1,-1},{1,1}};
        barajar(dirs);
        for (int[] d : dirs) {
            int nf = filaActual + d[0];
            int nc = colActual  + d[1];
            if (!ok(nf, nc)) continue;
            if (SimulacionPanel.getInstancia().quienEsta(nf, nc) != null)
                continue;
            if (SimulacionPanel.getInstancia()
                    .intentarMover(getLocalName(), nf, nc)) {
                filaActual = nf;
                colActual  = nc;
                return;
            }
        }
    }

    // ── BFS evitando bloqueados permanentes y celdas rechazadas ─────────────
    private List<int[]> calcularRuta() {
        int fO = filaActual,    cO = colActual;
        int fD = filaFormacion, cD = colFormacion;
        if (fO == fD && cO == cD) return new ArrayList<>();

        Queue<int[]>        queue  = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        int[][]             dirs   = {{-1,0},{1,0},{0,-1},{0,1}};

        queue.add(new int[]{fO, cO});
        parent.put(fO + "," + cO, "");

        while (!queue.isEmpty()) {
            int[]  curr = queue.poll();
            String cKey = curr[0] + "," + curr[1];

            if (curr[0] == fD && curr[1] == cD) {
                List<int[]> path = new ArrayList<>();
                String key = cKey;
                while (!parent.get(key).isEmpty()) {
                    String[] p = key.split(",");
                    path.add(0, new int[]{
                        Integer.parseInt(p[0]),
                        Integer.parseInt(p[1])
                    });
                    key = parent.get(key);
                }
                return path;
            }

            for (int[] d : dirs) {
                int    nf   = curr[0] + d[0];
                int    nc   = curr[1] + d[1];
                String nKey = nf + "," + nc;
                if (!ok(nf, nc) || parent.containsKey(nKey)) continue;
                if (celdasEvitar.contains(nKey)) continue;
                String ocupante = SimulacionPanel.getInstancia()
                                                 .quienEsta(nf, nc);
                if (ocupante != null && SimulacionPanel.getInstancia()
                        .esBloqueado(ocupante)) continue;
                parent.put(nKey, cKey);
                queue.add(new int[]{nf, nc});
            }
        }
        return new ArrayList<>();
    }

    // ── Fase random ──────────────────────────────────────────────────────────
    private void moverRandom() {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        barajar(dirs);
        for (int[] d : dirs) {
            int nf = filaActual + d[0];
            int nc = colActual  + d[1];
            if (ok(nf, nc) && SimulacionPanel.getInstancia()
                    .intentarMover(getLocalName(), nf, nc)) {
                filaActual = nf;
                colActual  = nc;
                return;
            }
        }
    }

    private boolean ok(int f, int c) {
        return f >= 0 && f < FILAS && c >= 0 && c < COLS;
    }

    private void barajar(int[][] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int[] tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("❌ " + getLocalName() + " apagado");
    }
}
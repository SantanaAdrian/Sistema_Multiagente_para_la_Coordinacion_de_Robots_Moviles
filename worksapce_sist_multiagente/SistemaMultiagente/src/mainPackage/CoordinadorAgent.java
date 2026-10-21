package mainPackage;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class CoordinadorAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("🧠 Coordinador — formación en 10 segundos");
        int total = (int) getArguments()[0];

        addBehaviour(new WakerBehaviour(this, 10000) {
            @Override
            protected void onWake() {
                System.out.println("🧠 ¡FORMAR FIN!");
                for (int i = 0; i < total; i++) {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(new AID("Robot" + (i + 1), AID.ISLOCALNAME));
                    msg.setContent("FORMACION:"
                        + Main.FORMACION[i][0] + ","
                        + Main.FORMACION[i][1]);
                    send(msg);
                }
                System.out.println("🧠 Posiciones enviadas a " + total + " robots");
            }
        });
    }
}
package br.edu.icev.aed.forense;

public class Alerta implements Comparable<Alerta> {

        public String actionType;
        public int timestamp;
        public int severityLevel;

        public Alerta(int timestamp, String actionType, int severityLevel) {
            this.timestamp = timestamp;
            this.actionType = actionType;
            this.severityLevel = severityLevel;
        }
        @Override
        public int compareTo(Alerta o) {
            return severityLevel - o.severityLevel;
        }

        public String toString() {
        return "Alerta Severidade=" + severityLevel + "]";
    }
}
